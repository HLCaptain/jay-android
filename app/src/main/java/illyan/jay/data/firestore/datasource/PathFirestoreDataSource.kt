package illyan.jay.data.firestore.datasource

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.firestore.model.FirestorePath
import illyan.jay.data.firestore.toDomainLocations
import illyan.jay.data.firestore.toPaths
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val appLifecycle: Lifecycle,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    fun getLocations(
        sessionUUID: String,
    ): Flow<List<DomainLocation>?> = flow {
        emitAll(
            object : FirestoreDataFlow<List<FirestorePath>, List<DomainLocation>>(
                firestore = firestore,
                coroutineScopeIO = coroutineScopeIO,
                toDomainModel = { it?.toDomainLocations() },
                appLifecycle = appLifecycle,
                snapshotHandler = FirestoreQuerySnapshotHandler(
                    snapshotToObject = { it.toObjects() },
                    snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                        if (uuid != null) {
                            firestore
                                .collection(FirestorePath.CollectionName)
                                .whereEqualTo(FirestorePath.FieldSessionUUID, sessionUUID)
                                .snapshots(MetadataChanges.INCLUDE)
                        } else {
                            null
                        }
                    }
                )
            ) {}.data
        )
    }

    fun insertLocations(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting locations for ${domainSessions.size} sessions: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted locations for ${domainSessions.size} sessions") }
    ) {
        insertPaths(
            paths = getPathsFromSessions(domainSessions, domainLocations),
            onFailure = onFailure,
            onCancel = onCancel,
            onSuccess = onSuccess,
        )
    }

    fun insertLocations(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
        batch: WriteBatch,
    ) {
        insertPaths(
            paths = getPathsFromSessions(domainSessions, domainLocations),
            batch = batch,
        )
    }

    private fun getPathsFromSessions(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
    ): List<FirestorePath> {
        val paths = mutableListOf<FirestorePath>()
        domainSessions.forEach { session ->
            val locationsForThisSession = domainLocations.filter { it.sessionUUID.contentEquals(session.uuid) }
            if (session.distance == null) {
                session.distance = locationsForThisSession
                    .sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
                    .map { it.latLng }.sphericalPathLength().toFloat()
            }
            paths.addAll(locationsForThisSession.toPaths(session.uuid, session.ownerUUID!!))
        }
        return paths
    }

    fun insertPath(
        path: FirestorePath,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting path ${path.uuid.take(4)} for session ${path.sessionUUID.take(4)}: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted path ${path.uuid.take(4)}") }
    ) = insertPaths(
        paths = listOf(path),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess
    )

    fun insertPaths(
        paths: List<FirestorePath>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting paths: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted ${paths.size} paths") },
    ) {
        if (!authInteractor.isUserSignedIn) {
            Timber.e("User not signed in, canceling operation")
            onCancel()
            return
        }
        if (paths.isEmpty()) {
            Timber.v("Path list was empty, no path to upload")
            onCancel()
            return
        }
        firestore.runBatch { batch ->
            insertPaths(paths, batch)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun insertPaths(
        paths: List<FirestorePath>,
        batch: WriteBatch,
    ) {
        if (!authInteractor.isUserSignedIn || paths.isEmpty()) return
        val pathRefs = paths.map {
            firestore
                .collection(FirestorePath.CollectionName)
                .document(it.uuid) to it
        }
        pathRefs.forEach {
            batch.set(it.first, it.second)
        }
    }

    suspend fun deleteLocationsForUser(
        userUUID: String = authInteractor.userUUID.toString(),
        onDelete: () -> Unit = {}
    ) {
        val batch = firestore.batch()
        deleteLocationsForUser(
            batch = batch,
            userUUID = userUUID,
            onWriteFinished = {
                batch.commit()
                onDelete()
            }
        )
    }

    suspend fun deleteLocationsForUser(
        batch: WriteBatch,
        userUUID: String = authInteractor.userUUID.toString(),
        onWriteFinished: () -> Unit = {}
    ) {
        if (!authInteractor.isUserSignedIn) return
        firestore
            .collection(FirestorePath.CollectionName)
            .whereEqualTo(FirestorePath.FieldOwnerUUID, userUUID)
            .snapshots().first { pathSnapshot ->
                Timber.d("Batch delete ${pathSnapshot.documents.size} path data for user ${userUUID.take(4)}")
                pathSnapshot.documents.forEach {
                    batch.delete(it.reference)
                }
                onWriteFinished()
                true
            }
    }

    suspend fun deleteLocationsForSession(
        sessionUUID: String,
        onDelete: () -> Unit = {}
    ) {
        val batch = firestore.batch()
        deleteLocationsForSessions(
            batch = batch,
            sessionUUIDs = listOf(sessionUUID),
            onWriteFinished = {
                batch.commit()
                onDelete()
            }
        )
    }

    suspend fun deleteLocationsForSessions(
        sessionUUIDs: List<String>,
        onDelete: () -> Unit = {}
    ) {
        val batch = firestore.batch()
        deleteLocationsForSessions(
            batch = batch,
            sessionUUIDs = sessionUUIDs,
            onWriteFinished = {
                batch.commit()
                onDelete()
            }
        )
    }

    suspend fun deleteLocationsForSessions(
        batch: WriteBatch,
        sessionUUIDs: List<String>,
        onWriteFinished: () -> Unit = {}
    ) {
        if (sessionUUIDs.isEmpty()) {
            Timber.d("No sessions given to delete paths for")
            return
        }
        // [Query.whereIn] can only take in at most 10 objects to compare
        sessionUUIDs.chunked(10).forEach { chunk ->
            val completableDeferred = CompletableDeferred<Unit>()
            Timber.v("Adding snapshot listener to batch delete ${chunk.size} path data")
            val pathSnapshotListener = firestore
                .collection(FirestorePath.CollectionName)
                .whereIn(FirestorePath.FieldSessionUUID, chunk)
                .addSnapshotListener { pathSnapshot, pathError ->
                    if (pathError != null) {
                        Timber.e(pathError, "Error while deleting path data: ${pathError.message}")
                    } else {
                        Timber.d("Batch delete ${pathSnapshot!!.documents.size} path data for user ${authInteractor.userUUID?.take(4)}")
                        pathSnapshot.documents.forEach {
                            batch.delete(it.reference)
                        }
                        completableDeferred.complete(Unit)
                        onWriteFinished()
                    }
                }
            completableDeferred.await()
            Timber.v("Removing snapshot listener from Firestore")
            pathSnapshotListener.remove()
        }
    }
}