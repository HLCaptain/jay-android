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

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.ktx.utils.sphericalPathLength
import illyan.jay.data.network.model.FirestorePath
import illyan.jay.data.network.model.FirestoreUser
import illyan.jay.data.network.toDomainModel
import illyan.jay.data.network.toFirestoreModel
import illyan.jay.data.network.toPaths
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val locationNetworkDataSource: LocationNetworkDataSource,
    private val userNetworkDataSource: UserNetworkDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val sessions: StateFlow<List<DomainSession>?> get() = userNetworkDataSource.user
        .map { user ->
            if (user != null) {
                val domainSessions = user.firebaseUser.sessions.map { it.toDomainModel(authInteractor.userUUID!!) }
                Timber.d("Firebase got sessions with IDs: ${domainSessions.map { it.uuid.take(4) }}")
                domainSessions
            } else {
                null
            }
        }
        .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

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
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while deleting sessions for user: ${it.message}") },
        onSuccess: () -> Unit = { Timber.i("Deleted ${sessionUUIDs.size} sessions") }
    ) {
        if (!authInteractor.isUserSignedIn || sessionUUIDs.isEmpty()) return
        Timber.i("Deleting ${sessionUUIDs.size} sessions for user ${userUUID.take(4)} from the cloud")
        coroutineScopeIO.launch {
            userNetworkDataSource.user.first { user ->
                user?.let {
                    val domainSessions = user.firebaseUser.sessions.map { it.toDomainModel(userUUID) }
                    val sessionsToDelete = domainSessions.filter { sessionUUIDs.contains(it.uuid) }
                    deleteSessions(
                        domainSessions = sessionsToDelete,
                        userUUID = userUUID,
                        onFailure = onFailure,
                        onSuccess = onSuccess
                    )
                }
                user != null
            }
        }
    }

    fun deleteSessions(
        domainSessions: List<DomainSession>,
        userUUID: String = authInteractor.userUUID.toString(),
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Operation canceled") },
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
                mapOf(FirestoreUser.FieldSessions to FieldValue.arrayRemove(*domainSessions.map { it.toFirestoreModel() }.toTypedArray())),
                SetOptions.merge()
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
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
                mapOf(FirestoreUser.FieldSessions to FieldValue.arrayUnion(*domainSessions.map { it.toFirestoreModel() }.toTypedArray())),
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
}