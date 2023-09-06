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
import illyan.jay.data.firestore.model.FirestoreSensorEvents
import illyan.jay.data.firestore.toChunkedFirebaseSensorEvents
import illyan.jay.data.firestore.toDomainSensorEvents
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.awaitOperations
import illyan.jay.util.delete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SensorEventsFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val appLifecycle: Lifecycle,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
//    @UserSensorEventsSnapshotHandler private val userSensorEventsDataFlowBuilder: () -> UserPathsFirestoreDataFlow, // FIXME: remove or use
) : FirestoreDataSource<FirestoreSensorEvents>(
    firestore = firestore
) {
    override fun getReference(data: FirestoreSensorEvents): DocumentReference {
        return firestore
            .collection(FirestoreSensorEvents.CollectionName)
            .document(data.uuid)
    }

    fun getEventsBySession(sessionUUID: String) =
        object : FirestoreDataFlow<List<FirestoreSensorEvents>, List<DomainSensorEvent>>(
            firestore = firestore,
            coroutineScopeIO = coroutineScopeIO,
            toDomainModel = { it?.toDomainSensorEvents() },
            appLifecycle = appLifecycle,
            snapshotHandler = FirestoreQuerySnapshotHandler(
                snapshotToObject = { it.toObjects() },
                snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                    if (uuid != null) {
                        firestore
                            .collection(FirestoreSensorEvents.CollectionName)
                            .whereEqualTo(FirestoreSensorEvents.FieldSessionUUID, sessionUUID)
                            .snapshots(MetadataChanges.INCLUDE)
                    } else {
                        null
                    }
                }
            )
        ) {}.data

    private fun getEventsFromSessions(
        domainSessions: List<DomainSession>,
        domainSensorEvents: List<DomainSensorEvent>,
    ): List<FirestoreSensorEvents> {
        val paths = mutableListOf<FirestoreSensorEvents>()
        domainSessions.forEach { session ->
            val eventsForThisSession = domainSensorEvents.filter { it.sessionUUID.contentEquals(session.uuid) }
            paths.addAll(eventsForThisSession.toChunkedFirebaseSensorEvents(session.uuid, session.ownerUUID!!))
        }
        return paths
    }

    fun insertEvents(
        domainSessions: List<DomainSession>,
        domainSensorEvents: List<DomainSensorEvent>,
        batch: WriteBatch,
    ) {
        setData(
            data = getEventsFromSessions(domainSessions, domainSensorEvents),
            batch = batch,
        )
    }

    suspend fun deleteSensorEventsForUser(
        batch: WriteBatch,
        userUUID: String = authInteractor.userUUID.toString(),
        onWriteFinished: () -> Unit = {}
    ) {
        batch.delete(
            query = firestore
                .collection(FirestoreSensorEvents.CollectionName)
                .whereEqualTo(FirestoreSensorEvents.FieldOwnerUUID, userUUID),
            onOperationFinished = onWriteFinished
        )
    }

    suspend fun deleteSensorEventsForSessions(
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
                        .collection(FirestoreSensorEvents.CollectionName)
                        .whereIn(FirestoreSensorEvents.FieldSessionUUID, chunk),
                    onOperationFinished = onOperationFinished
                )
            }
        }
        onWriteFinished()
    }
}