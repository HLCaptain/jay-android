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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.network.model.FirestorePath
import illyan.jay.data.network.model.FirestoreUser
import illyan.jay.data.network.toDomainModel
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
    private val locationNetworkDataSource: LocationNetworkDataSource,
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
                    val user = snapshot?.toObject<FirestoreUser>()
                    val domainSessions = user?.sessions?.map { it.toDomainModel(userUUID) } ?: emptyList()
                    Timber.d("Firebase got sessions with IDs: ${domainSessions.map { it.uuid.take(4) }}")
                    listener(domainSessions)
                }
            }
            firestore
                .collection(FirestoreUser.CollectionName)
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

    fun deleteSession(
        sessionUUID: String,
        onSuccess: () -> Unit
    ) = deleteSessions(
        sessionUUIDs = listOf(sessionUUID),
        onSuccess = onSuccess
    )

    @JvmName("deleteSessionsByUUIDs")
    fun deleteSessions(
        sessionUUIDs: List<String>,
        userUUID: String = authInteractor.userUUID.toString(),
        onSuccess: () -> Unit = { Timber.i("Deleted ${sessionUUIDs.size} sessions") }
    ) {
        if (!authInteractor.isUserSignedIn || sessionUUIDs.isEmpty()) return
        val deletedSessions = MutableStateFlow(false)
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(userUUID)
        Timber.i("Deleting ${sessionUUIDs.size} sessions for user ${userUUID.take(4)} from the cloud")
        val userListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error while deleting sessions for user: ${error.message}")
            } else {
                val user = snapshot?.toObject<FirestoreUser>()
                val domainSessions = user?.sessions?.map { it.toDomainModel(userUUID) } ?: emptyList()
                val sessionsToDelete = domainSessions.filter { sessionUUIDs.contains(it.uuid) }
                deleteSessions(sessionsToDelete) {
                    deletedSessions.value = true
                    onSuccess()
                }
            }
        }
        coroutineScopeIO.launch {
            deletedSessions.first {
                if (it) userListener.remove()
                it
            }
        }
    }

    fun deleteSessions(
        domainSessions: List<DomainSession>,
        userUUID: String = authInteractor.userUUID.toString(),
        onSuccess: () -> Unit = { Timber.i("Deleted ${domainSessions.size} sessions") }
    ) {
        if (!authInteractor.isUserSignedIn || domainSessions.isEmpty()) return
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(userUUID)
        Timber.i("Deleting ${domainSessions.size} sessions for user ${userUUID.take(4)} from the cloud")
        firestore.runBatch { batch ->
            batch.set(
                userRef,
                mapOf(FirestoreUser.FieldSessions to FieldValue.arrayRemove(*domainSessions.map { it.toHashMap() }.toTypedArray())),
                SetOptions.merge()
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error: ${exception.message}")
        }.addOnCanceledListener {
            Timber.i("Operation canceled")
        }
    }

    fun insertSession(
        domainSession: DomainSession,
        domainLocation: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit
    ) = insertSessions(
        domainSessions = listOf(domainSession),
        domainLocations = domainLocation,
        onSuccess = onSuccess
    )

    fun insertSessions(
        domainSessions: List<DomainSession>,
        domainLocations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit
    ) {
        if (!authInteractor.isUserSignedIn || domainSessions.isEmpty()) return
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
        locationNetworkDataSource.insertPaths(paths)
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(authInteractor.userUUID!!)

        firestore.runBatch { batch ->
            batch.set(
                userRef,
                mapOf(FirestoreUser.FieldSessions to FieldValue.arrayUnion(*domainSessions.map { it.toHashMap() }.toTypedArray())),
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

    fun deleteUserData() {
        if (!authInteractor.isUserSignedIn) return
        val isUserDeleted = MutableStateFlow(false)
        val userSnapshotListener = firestore
            .collection(FirestoreUser.CollectionName)
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
        coroutineScopeIO.launch {
            isUserDeleted.first {
                if (it) {
                    Timber.d("Removing user listener from Firestore")
                    userSnapshotListener.remove()
                }
                it
            }
        }
    }
}