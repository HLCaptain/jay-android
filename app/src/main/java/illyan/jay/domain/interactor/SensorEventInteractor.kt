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

package illyan.jay.domain.interactor

import illyan.jay.data.firestore.datasource.SensorEventsFirestoreDataSource
import illyan.jay.data.firestore.datasource.SessionFirestoreDataSource
import illyan.jay.data.room.datasource.SensorEventRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.di.CoroutineScopeMain
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Acceleration interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property sensorEventRoomDataSource local datasource
 * @constructor Create empty Acceleration interactor
 */
@Singleton
class SensorEventInteractor @Inject constructor(
    private val sensorEventRoomDataSource: SensorEventRoomDataSource,
    private val sensorEventsFirestoreDataSource: SensorEventsFirestoreDataSource,
    private val authInteractor: AuthInteractor,
    private val sessionInteractor: SessionInteractor,
    private val sessionFirestoreDataSource: SessionFirestoreDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
    @CoroutineScopeMain private val coroutineScopeMain: CoroutineScope,
) {
    fun getSensorEvents(session: DomainSession) = sensorEventRoomDataSource.getSensorEvents(session)
    fun getSensorEvents(sessionUUID: String) = sensorEventRoomDataSource.getSensorEvents(sessionUUID)
    /**
     * Save an acceleration data instance.
     *
     * @param sensorEvent acceleration data to be saved.
     */
    fun saveSensorEvent(sensorEvent: DomainSensorEvent) =
        sensorEventRoomDataSource.saveSensorEvent(sensorEvent)

    /**
     * Save multiple acceleration data instances.
     *
     * @param sensorEvents multiple accelerations to be saved.
     */
    fun saveSensorEvents(sensorEvents: List<DomainSensorEvent>) =
        sensorEventRoomDataSource.saveSensorEvents(sensorEvents)

    val syncedEventsCollectionJobs = hashMapOf<String, Job>()

    /**
     * Get the path for a session, either it being in the cloud or in the local database.
     * Cannot react to user/session/location state changes, if the location was found in
     * the local database, gets uploaded, then gets deleted from the local database, the listener
     * would still listen to the local database's data.
     */
    suspend fun getSyncedEvents(sessionUUID: String): StateFlow<List<DomainSensorEvent>?> {
        Timber.i("Trying to load sensor events for session with ID: $sessionUUID")
        if (syncedEventsCollectionJobs[sessionUUID] != null) {
            Timber.v("Cancelling current data collection job regarding the sensor events of $sessionUUID")
            syncedEventsCollectionJobs[sessionUUID]?.cancel(CancellationException("New data collection job requested"))
            syncedEventsCollectionJobs.remove(sessionUUID)
        }
        val syncedEvents = MutableStateFlow<List<DomainSensorEvent>?>(null)
        syncedEventsCollectionJobs[sessionUUID] = coroutineScopeMain.launch(start = CoroutineStart.LAZY) {
            sensorEventsFirestoreDataSource.getEventsBySession(sessionUUID).collectLatest { remoteEvents ->
                coroutineScopeIO.launch {
                    Timber.v("Found sensor events for session, caching it on disk")
                    remoteEvents?.let { saveSensorEvents(it) }
                }
                syncedEvents.update { remoteEvents }
            }
        }
        val session = sessionInteractor.getSession(sessionUUID).first()
        if (session != null) {
            Timber.v("Found session on disk")
            coroutineScopeIO.launch {
                val events = getSensorEvents(sessionUUID).first()
                if (events.isEmpty()) {
                    Timber.v("Not found sensor events for session on disk, checking cloud")
                    if (!authInteractor.isUserSignedIn) {
                        Timber.i("Not authenticated to access cloud, return an empty list")
                        syncedEvents.update { emptyList() }
                    } else {
                        syncedEventsCollectionJobs[sessionUUID]?.start()
                    }
                } else {
                    Timber.i("Found path on disk")
                    syncedEvents.update { events }
                }
            }
        } else {
            Timber.v("Not found session on disk, checking cloud")
            if (!authInteractor.isUserSignedIn) {
                Timber.i("Not authenticated to access cloud, return an empty list")
                syncedEvents.update { emptyList() }
            } else {
                coroutineScopeIO.launch {
                    sessionFirestoreDataSource.sessions.first { sessions ->
                        if (sessions != null && sessions.any { it.uuid == sessionUUID }) {
                            Timber.v("Found session in cloud, caching it on disk")
                            coroutineScopeIO.launch {
                                sessionInteractor.saveSession(sessions.first { it.uuid == sessionUUID })
                                syncedEventsCollectionJobs[sessionUUID]?.start()
                            }
                        }
                        sessions != null
                    }
                }
            }
        }
        return syncedEvents.asStateFlow()
    }
}
