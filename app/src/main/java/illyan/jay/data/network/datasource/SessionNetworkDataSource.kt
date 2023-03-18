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
import com.google.firebase.firestore.WriteBatch
import illyan.jay.data.network.model.FirestoreUser
import illyan.jay.data.network.toDomainModel
import illyan.jay.data.network.toFirestoreModel
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val userNetworkDataSource: UserNetworkDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val sessions: StateFlow<List<DomainSession>?> by lazy {
        combine(
            userNetworkDataSource.user,
            userNetworkDataSource.isLoading
        ) { user, loading ->
            if (user != null) {
                val domainSessions = user.sessions.map { it.toDomainModel(user.uuid) }
                Timber.d("Firebase got sessions with IDs: ${domainSessions.map { it.uuid.take(4) }}")
                domainSessions
            } else if (loading) {
                null
            } else {
                emptyList()
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    // FIXME: may user `lazy` more often or change SharingStarted to Lazily instead of Eagerly

    fun deleteSession(
        sessionUUID: String,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while deleting session: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Deleting session canceled") },
        onSuccess: () -> Unit = { Timber.i("Deleted session") },
    ) = deleteSessions(
        sessionUUIDs = listOf(sessionUUID),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess,
    )

    fun deleteAllSessions(
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while deleting sessions: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Deleting sessions canceled") },
        onSuccess: () -> Unit = { Timber.i("Deleted sessions") }
    ) = deleteSessions(
        sessionUUIDs = userNetworkDataSource.user.value?.sessions?.map { it.uuid } ?: emptyList(),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess,
    )

    fun deleteAllSessions(
        batch: WriteBatch,
        onWriteFinished: () -> Unit = {}
    ) = deleteSessions(
        batch = batch,
        sessionUUIDs = userNetworkDataSource.user.value?.sessions?.map { it.uuid } ?: emptyList(),
        onWriteFinished = onWriteFinished,
    )

    @JvmName("deleteSessionsByUUIDs")
    fun deleteSessions(
        batch: WriteBatch,
        sessionUUIDs: List<String>,
        userUUID: String = authInteractor.userUUID.toString(),
        onWriteFinished: () -> Unit = {}
    ) {
        if (!authInteractor.isUserSignedIn || sessionUUIDs.isEmpty()) return
        coroutineScopeIO.launch {
            userNetworkDataSource.user.first { user ->
                user?.let {
                    val domainSessions = user.sessions.map { it.toDomainModel(userUUID) }
                    val sessionsToDelete = domainSessions.filter { sessionUUIDs.contains(it.uuid) }
                    deleteSessions(
                        batch = batch,
                        domainSessions = sessionsToDelete,
                        userUUID = userUUID,
                        onWriteFinished = onWriteFinished
                    )
                }
                user != null
            }
        }
    }

    @JvmName("deleteSessionsByUUIDs")
    fun deleteSessions(
        sessionUUIDs: List<String>,
        userUUID: String = authInteractor.userUUID.toString(),
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while deleting sessions: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Deleting sessions canceled") },
        onSuccess: () -> Unit = { Timber.i("Deleted sessions") }
    ) {
        firestore.runBatch {
            deleteSessions(
                batch = it,
                sessionUUIDs = sessionUUIDs,
                userUUID = userUUID,
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun deleteSessions(
        batch: WriteBatch,
        domainSessions: List<DomainSession>,
        userUUID: String = authInteractor.userUUID.toString(),
        onWriteFinished: () -> Unit = {}
    ) {
        if (!authInteractor.isUserSignedIn || domainSessions.isEmpty()) return
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(userUUID)
        Timber.i("Batch remove ${domainSessions.size} sessions for user ${userUUID.take(4)} from the cloud")
        batch.set(
            userRef,
            mapOf(FirestoreUser.FieldSessions to FieldValue.arrayRemove(*domainSessions.map { it.toFirestoreModel() }.toTypedArray())),
            SetOptions.merge()
        )
        onWriteFinished()
    }

    fun deleteSessions(
        domainSessions: List<DomainSession>,
        userUUID: String = authInteractor.userUUID.toString(),
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while deleting sessions: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Deleting ${domainSessions.size} sessions canceled") },
        onSuccess: () -> Unit = { Timber.i("Deleted ${domainSessions.size} sessions") }
    ) {
        if (!authInteractor.isUserSignedIn || domainSessions.isEmpty()) return
        firestore.runBatch { batch ->
            deleteSessions(
                batch = batch,
                domainSessions = domainSessions,
                userUUID = userUUID,
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
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting session: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Inserting session canceled") },
        onSuccess: (List<DomainSession>) -> Unit
    ) = insertSessions(
        domainSessions = listOf(domainSession),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess,
    )

    fun insertSessions(
        domainSessions: List<DomainSession>,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting ${domainSessions.size} sessions: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Inserting ${domainSessions.size} sessions canceled") },
        onSuccess: (List<DomainSession>) -> Unit
    ) {
        firestore.runBatch { batch ->
            insertSessions(batch, domainSessions)
        }.addOnSuccessListener {
            onSuccess(domainSessions)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun insertSessions(
        batch: WriteBatch,
        domainSessions: List<DomainSession>,
    ) {
        if (!authInteractor.isUserSignedIn || domainSessions.isEmpty()) return
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(authInteractor.userUUID!!)
        batch.set(
            userRef,
            mapOf(FirestoreUser.FieldSessions to FieldValue.arrayUnion(*domainSessions.map { it.toFirestoreModel() }.toTypedArray())),
            SetOptions.merge()
        )
    }
}