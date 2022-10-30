/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

import com.mapbox.geojson.Point
import com.mapbox.search.ReverseGeoOptions
import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.data.disk.datasource.SensorEventDiskDataSource
import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Session interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property sessionDiskDataSource local database
 * @constructor Create empty Session interactor
 */
@Singleton
class SessionInteractor @Inject constructor(
    private val sessionDiskDataSource: SessionDiskDataSource,
    private val sensorEventDiskDataSource: SensorEventDiskDataSource,
    private val locationDiskDataSource: LocationDiskDataSource,
    private val searchInteractor: SearchInteractor,
) {
    /**
     * Get a particular session by its ID.
     *
     * @param id primary key of the session.
     *
     * @return a flow of the session if it exists in the database,
     * otherwise a flow with null in it.
     */
    fun getSession(id: Long) = sessionDiskDataSource.getSession(id)

    /**
     * Get all session as a Flow.
     *
     * @return all session as a flow.
     */
    fun getSessions() = sessionDiskDataSource.getSessions()

    fun getSessionIds() = sessionDiskDataSource.getSessionIds()

    /**
     * Get ongoing sessions, which have no end date.
     *
     * @return a flow of ongoing sessions.
     */
    fun getOngoingSessions() = sessionDiskDataSource.getOngoingSessions()

    /**
     * Get ongoing sessions' IDs in a quicker way than getting all
     * the information about the ongoing sessions. Cool behaviour based design :)
     *
     * @return a flow of ongoing sessions' IDs.
     */
    fun getOngoingSessionIds() = sessionDiskDataSource.getOngoingSessionIds()

    /**
     * Save session data.
     *
     * @param session updates the data of the session with the same ID.
     *
     * @return id of session updated.
     */
    fun saveSession(session: DomainSession) = sessionDiskDataSource.saveSession(session)

    /**
     * Save multiple sessions.
     *
     * @param sessions updates the data of the sessions with the same ID.
     */
    fun saveSessions(sessions: List<DomainSession>) = sessionDiskDataSource.saveSessions(sessions)

    /**
     * Creates a session with the current time as
     * the starting date, end date as null and distance = 0.
     *
     * @return ID of the newly started session.
     */
    suspend fun startSession(coroutineScope: CoroutineScope): Long {
        val sessionId = sessionDiskDataSource.startSession()
        getSession(sessionId).first { session ->
            session?.let {
                coroutineScope.launch {
                    refreshSessionStartLocation(
                        session,
                        coroutineScope
                    )
                }
            }
            session != null
        }
        return sessionId
    }

    /**
     * Stop a session.
     * Sets an end date for the session and saves it.
     *
     * @param session needed to be stopped.
     *
     * @return id of session stopped.
     */
    fun stopSession(session: DomainSession) = sessionDiskDataSource.stopSession(session)

    suspend fun refreshSessionStartLocation(
        session: DomainSession,
        coroutineScope: CoroutineScope
    ) {
        locationDiskDataSource.getLocations(session.id).first { locations ->
            val sortedLocations = locations.sortedBy { location ->
                location.zonedDateTime.toInstant().toEpochMilli()
            }
            val startLocation = sortedLocations.firstOrNull()?.latLng
            session.startLocation = startLocation
            startLocation?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    saveSession(session)
                }
            }

            // Reverse geocoding locations to get names for them.
            if (startLocation != null && session.startLocationName == null) {
                searchInteractor.search(
                    reverseGeoOptions = ReverseGeoOptions(
                        center = Point.fromLngLat(
                            startLocation.longitude,
                            startLocation.latitude
                        )
                    )
                ) { results, _ ->
                    results.firstOrNull()?.let {
                        session.startLocationName = it.address?.place
                        session.startLocationName?.let {
                            coroutineScope.launch(Dispatchers.IO) {
                                saveSession(session)
                            }
                        }
                    }
                }
            }
            locations.isNotEmpty()
        }
    }

    suspend fun refreshSessionEndLocation(
        session: DomainSession,
        coroutineScope: CoroutineScope
    ) {
        locationDiskDataSource.getLocations(session.id).first { locations ->
            val sortedLocations = locations.sortedBy { location ->
                location.zonedDateTime.toInstant().toEpochMilli()
            }
            val endLocation = sortedLocations.lastOrNull()?.latLng
            session.endLocation = endLocation
            endLocation?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    saveSession(session)
                }
            }

            // Reverse geocoding locations to get names for them.
            if (endLocation != null && session.endLocationName == null) {
                searchInteractor.search(
                    reverseGeoOptions = ReverseGeoOptions(
                        center = Point.fromLngLat(
                            endLocation.longitude,
                            endLocation.latitude
                        )
                    )
                ) { results, _ ->
                    results.firstOrNull()?.let {
                        session.endLocationName = it.address?.place
                        session.endLocationName?.let {
                            coroutineScope.launch(Dispatchers.IO) {
                                saveSession(session)
                            }
                        }
                    }
                }
            }
            locations.isNotEmpty()
        }
    }

    /**
     * Stop all ongoing sessions.
     */
    suspend fun stopOngoingSessions(coroutineScope: CoroutineScope) {
        getOngoingSessions().first { sessions ->
            sessionDiskDataSource.stopSessions(sessions)
            sessions.forEach { session ->
                coroutineScope.launch {
                    refreshSessionStartLocation(
                        session,
                        coroutineScope
                    )
                    refreshSessionEndLocation(
                        session,
                        coroutineScope
                    )
                }
            }
            true
        }
    }

    /**
     * Delete stopped sessions, whom have
     * their endTime properties not null.
     */
    suspend fun deleteStoppedSessions() {
        sessionDiskDataSource.getSessions().first {
            val stoppedSessions = it.filter { session -> session.endDateTime != null }
            stoppedSessions.forEach { session ->
                sensorEventDiskDataSource.deleteSensorEventsForSession(session.id)
                locationDiskDataSource.deleteLocationForSession(session.id)
            }
            sessionDiskDataSource.deleteSessions(stoppedSessions)
            true
        }
    }
}

