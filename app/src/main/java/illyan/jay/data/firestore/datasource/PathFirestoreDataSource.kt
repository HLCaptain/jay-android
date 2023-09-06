/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.firestore.datasource

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.DocumentReference
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
import illyan.jay.util.awaitOperations
import illyan.jay.util.delete
import illyan.jay.util.runBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val appLifecycle: Lifecycle,
    private val userPathsDataFlowBuilder: () -> UserPathsFirestoreDataFlow,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) : FirestoreDataSource<FirestorePath>(
    firestore = firestore
) {
    override fun getReference(data: FirestorePath): DocumentReference {
        return firestore
            .collection(FirestorePath.CollectionName)
            .document(data.uuid)
    }

    // FIXME: remove or use
    private val locationsByUser = lazy { userPathsDataFlowBuilder().data }

    fun getLocationsBySession(sessionUUID: String) =
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

    fun insertLocations(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting locations for ${domainSessions.size} sessions: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
        onSuccess: () -> Unit = { Timber.d("Successfully inserted locations for ${domainSessions.size} sessions") }
    ) {
        setData(
            data = getPathsFromSessions(domainSessions, domainLocations),
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
        setData(
            data = getPathsFromSessions(domainSessions, domainLocations),
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

    suspend fun deleteLocationsForUser(
        userUUID: String = authInteractor.userUUID.toString(),
        onDelete: () -> Unit = {}
    ) {
        firestore.runBatch(1) { batch, onOperationFinished ->
            deleteLocationsForUser(
                batch = batch,
                userUUID = userUUID,
                onWriteFinished = onOperationFinished
            )
        }.addOnSuccessListener { onDelete() }
    }

    suspend fun deleteLocationsForUser(
        batch: WriteBatch,
        userUUID: String = authInteractor.userUUID.toString(),
        onWriteFinished: () -> Unit = {}
    ) {
        batch.delete(
            query = firestore
                .collection(FirestorePath.CollectionName)
                .whereEqualTo(FirestorePath.FieldOwnerUUID, userUUID),
            onOperationFinished = onWriteFinished
        )
    }

    suspend fun deleteLocationsForSession(
        sessionUUID: String,
        onDelete: () -> Unit = {}
    ) {
        firestore.runBatch(1) { batch, onOperationFinished ->
            deleteLocationsForSessions(
                batch = batch,
                sessionUUIDs = listOf(sessionUUID),
                onWriteFinished = onOperationFinished
            )
        }.addOnSuccessListener { onDelete() }
    }

    suspend fun deleteLocationsForSessions(
        sessionUUIDs: List<String>,
        onDelete: () -> Unit = {}
    ) {
        firestore.runBatch(1) { batch, onOperationFinished ->
            deleteLocationsForSessions(
                batch = batch,
                sessionUUIDs = sessionUUIDs,
                onWriteFinished = onOperationFinished
            )
        }.addOnSuccessListener { onDelete() }
    }

    suspend fun deleteLocationsForSessions(
        batch: WriteBatch,
        sessionUUIDs: List<String>,
        onWriteFinished: () -> Unit = {}
    ) {
        // [Query.whereIn] can only take in at most 10 objects to compare
        val chunkedUUIDs = sessionUUIDs.chunked(10)
        awaitOperations(chunkedUUIDs.size) { onOperationFinished ->
            chunkedUUIDs.forEach { chunk ->
                batch.delete(
                    query = firestore
                        .collection(FirestorePath.CollectionName)
                        .whereIn(FirestorePath.FieldSessionUUID, chunk),
                    onOperationFinished = onOperationFinished
                )
            }
        }
        onWriteFinished()
    }
}