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

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.network.model.PathDocument
import illyan.jay.data.network.toDomainSession
import illyan.jay.data.network.toHashMap
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
class SessionNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    fun getSessions(
        activity: Activity? = null,
        userUUID: String = authInteractor.userUUID.toString(),
        listener: (List<DomainSession>?) -> Unit,
    ): ListenerRegistration? {
        return if (authInteractor.isUserSignedIn) {
            Timber.d("Connecting snapshot listener to Firebase to get session data for user ${userUUID.take(4)}")
            val snapshotListener = EventListener<DocumentSnapshot> { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error while getting session data: ${error.message}")
                    listener(null as List<DomainSession>?)
                } else {
                    val domainSessions = (snapshot?.get("sessions") as? List<Map<String, Any>>?)?.map {
                        it.toDomainSession(it["uuid"] as String, authInteractor.userUUID!!)
                    } ?: emptyList()
                    Timber.d("Firebase got sessions with IDs: ${
                        domainSessions.map { it.uuid.substring(0..3) }
                    }")
                    listener(domainSessions)
                }
            }
            firestore
                .collection(UsersCollectionPath)
                .document(userUUID)
                .run { if (activity != null)
                    addSnapshotListener(activity, snapshotListener)
                else
                    addSnapshotListener(snapshotListener)
                }
        } else {
            Timber.d("User not signed in, returning with null")
            listener(null as List<DomainSession>?)
            null
        }
    }

    fun insertSession(
        domainSession: DomainSession,
        domainLocations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit
    ) = insertSessions(listOf(domainSession), domainLocations, onSuccess)

    fun insertSessions(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit
    ) {
        if (!authInteractor.isUserSignedIn) return
        val paths = mutableListOf<PathDocument>()
        domainSessions.forEach { session ->
            val locationsForThisSession = domainLocations.filter { it.sessionUUID.contentEquals(session.uuid) }
            if (session.distance == null) {
                session.distance = locationsForThisSession
                    .sortedBy { it.zonedDateTime.toInstant().toEpochMilli() }
                    .map { it.latLng }.sphericalPathLength().toFloat()
            }
            paths.addAll(locationsForThisSession.toPaths(session.uuid, session.ownerUUID!!))
        }
        val pathRefs = paths.map {
            firestore
                .collection(PathsCollectionPath)
                .document(it.uuid) to it
        }
        val userRef = firestore
            .collection(UsersCollectionPath)
            .document(authInteractor.userUUID!!)

        Timber.i("Running batch to upload ${paths.size} paths for ${domainSessions.size} sessions")
        firestore.runBatch { batch ->
            pathRefs.forEach {
                val parcel = Parcel.obtain()
                it.second.writeToParcel(parcel, PARCELABLE_WRITE_RETURN_VALUE)
                val dataSizeInBytes = parcel.dataSize()
                Timber.i("Path ${it.second.uuid.take(4)} for session ${it.second.sessionUUID.take(4)} size is around $dataSizeInBytes bytes")
                // TODO: make size calculations more reliable and easier to implement
                val maxSizeInBytes = 1_048_576
                if (dataSizeInBytes < maxSizeInBytes) {
                    batch.set(it.first, it.second.toHashMap())
                } else {
                    Timber.d("Not uploading this path, as $dataSizeInBytes bytes exceeds the $maxSizeInBytes byte limit")
                }
                parcel.recycle()
            }
            batch.set(
                userRef,
                mapOf("sessions" to FieldValue.arrayUnion(*domainSessions.map { it.toHashMap() }.toTypedArray())),
                SetOptions.merge()
            )
        }.addOnSuccessListener {
            onSuccess(domainSessions)
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error: ${exception.message}")
        }.addOnCanceledListener {
            Timber.i("Operation canceled")
        }
    }

    suspend fun deleteUserData() {
        if (!authInteractor.isUserSignedIn) return
        val isUserDeleted = MutableStateFlow(false)
        val arePathsDeleted = MutableStateFlow(false)
        val userSnapshotListener = firestore
            .collection(UsersCollectionPath)
            .document(authInteractor.userUUID!!)
            .addSnapshotListener { userSnapshot, userError ->
                if (userError != null) {
                    Timber.e(userError, "Error while deleting user data: ${userError.message}")
                } else if (!isUserDeleted.value) {
                    userSnapshot?.let {
                        it.reference.delete()
                        isUserDeleted.value = true
                    }
                }
            }
        val pathSnapshotListener = firestore
            .collection(PathsCollectionPath)
            .whereEqualTo("ownerUUID", authInteractor.userUUID)
            .addSnapshotListener { pathSnapshot, pathError ->
                if (pathError != null) {
                    Timber.e(pathError, "Error while deleting user data: ${pathError.message}")
                } else if (!arePathsDeleted.value) {
                    Timber.d("Delete ${pathSnapshot!!.documents.size} path data for user ${authInteractor.userUUID?.take(4)}")
                    pathSnapshot.documents.forEach {
                        it.reference.delete()
                    }
                    arePathsDeleted.value = true
                }
            }
        coroutineScopeIO.launch {
            isUserDeleted.first {
                if (it) {
                    Timber.d("Removing user listener from Firestore")
                    userSnapshotListener.remove()
                }
                it
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

    companion object {
        const val UsersCollectionPath = "users"
        const val SessionsCollectionPath = "sessions"
        const val PathsCollectionPath = "paths"
    }
}