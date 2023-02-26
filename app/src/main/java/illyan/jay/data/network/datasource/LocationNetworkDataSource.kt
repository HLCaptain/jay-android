/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.network.datasource

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.network.model.FirestorePath
import illyan.jay.data.network.toDomainLocations
import illyan.jay.data.network.toPaths
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    fun getLocations(
        sessionUUID: String = authInteractor.userUUID.toString(),
        listener: (List<DomainLocation>) -> Unit
    ) {
        if (authInteractor.isUserSignedIn) {
            firestore
                .collection(FirestorePath.CollectionName)
                .whereEqualTo("sessionUUID", sessionUUID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error while getting path for session $sessionUUID: ${error.message}")
                    } else {
                        listener(snapshot!!.documents.toDomainLocations())
                    }
                }
        } else {
            listener(emptyList())
        }
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
        if (!authInteractor.isUserSignedIn || paths.isEmpty()) return
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
            val parcel = Parcel.obtain()
            it.second.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
            val dataSizeInBytes = parcel.dataSize()
            Timber.i("Path ${it.second.uuid.take(4)} for session ${it.second.sessionUUID.take(4)} size is around $dataSizeInBytes bytes")
            // TODO: make size calculations more reliable and easier to implement
            val maxSizeInBytes = 1_048_576
            if (dataSizeInBytes < maxSizeInBytes) {
                batch.set(it.first, it.second)
            } else {
                Timber.d("Not uploading this path, as $dataSizeInBytes bytes exceeds the $maxSizeInBytes byte limit")
            }
            parcel.recycle()
        }
    }

    fun deleteLocationsForUser(
        userUUID: String = authInteractor.userUUID.toString()
    ) {
        if (!authInteractor.isUserSignedIn) return
        val arePathsDeleted = MutableStateFlow(false)
        val pathSnapshotListener = firestore
            .collection(FirestorePath.CollectionName)
            .whereEqualTo(FirestorePath.FieldOwnerUUID, userUUID)
            .addSnapshotListener { pathSnapshot, pathError ->
                if (pathError != null) {
                    Timber.e(pathError, "Error while deleting user data: ${pathError.message}")
                } else if (!arePathsDeleted.value) {
                    Timber.d("Delete ${pathSnapshot!!.documents.size} path data for user ${userUUID.take(4)}")
                    pathSnapshot.documents.forEach {
                        it.reference.delete()
                    }
                    arePathsDeleted.value = true
                }
            }
        coroutineScopeIO.launch {
            arePathsDeleted.first {
                if (it) {
                    Timber.d("Removing path listener from Firestore")
                    pathSnapshotListener.remove()
                }
                it
            }
        }
    }

    fun deleteLocationsForSession(
        sessionUUID: String,
        onDelete: () -> Unit = {}
    ) {
        val arePathsDeleted = MutableStateFlow(false)
        val pathSnapshotListener = firestore
            .collection(FirestorePath.CollectionName)
            .whereEqualTo(FirestorePath.FieldSessionUUID, sessionUUID)
            .addSnapshotListener { pathSnapshot, pathError ->
                if (pathError != null) {
                    Timber.e(pathError, "Error while deleting path data: ${pathError.message}")
                } else {
                    Timber.d("Delete ${pathSnapshot!!.documents.size} path data for user ${authInteractor.userUUID?.take(4)}")
                    pathSnapshot.documents.forEach {
                        it.reference.delete()
                    }
                    arePathsDeleted.value = true
                    onDelete()
                }
            }
        coroutineScopeIO.launch {
            arePathsDeleted.first { arePathsDeleted ->
                if (arePathsDeleted) {
                    Timber.d("Removing path listener from Firestore")
                    pathSnapshotListener.remove()
                }
                arePathsDeleted
            }
        }
    }

    fun deleteLocationsForSessions(
        sessionUUIDs: List<String>,
        onDelete: (String) -> Unit = {}
    ) {
        if (sessionUUIDs.isEmpty()) {
            Timber.d("No sessions given to delete paths for!")
            return
        }
        sessionUUIDs.forEach { deleteLocationsForSession(it) { onDelete(it) } }
    }
}