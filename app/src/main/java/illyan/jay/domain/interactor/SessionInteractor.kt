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

import android.app.Activity
import com.google.firebase.firestore.ListenerRegistration
import com.mapbox.geojson.Point
import com.mapbox.search.ReverseGeoOptions
import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.data.disk.datasource.SensorEventDiskDataSource
import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.data.network.datasource.SessionNetworkDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.di.CoroutineScopeMain
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.sphericalPathLength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
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
    private val sessionNetworkDataSource: SessionNetworkDataSource,
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val serviceInteractor: ServiceInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
    @CoroutineScopeMain private val coroutineScopeMain: CoroutineScope
) {

    init {
        var previousUserUUID = authInteractor.userUUID
        authInteractor.addOnSignOutListener {
            flow {
                // Make here any changes before signing out, for
                // example, save data, stop sessions, services.
                // Sync to the cloud if set.
                if (!serviceInteractor.isJayServiceRunning()) {
                    emit(Unit)
                } else {
                    serviceInteractor.stopJayService()
                    getOngoingSessions().first { sessions ->
                        if (sessions.none { it.endDateTime == null }) {
                            emit(Unit)
                        }
                        sessions.none { it.endDateTime == null }
                    }
                }
            }
        }
        authInteractor.addAuthStateListener {
            if (previousUserUUID != it.uid) {
                serviceInteractor.stopJayService()
                previousUserUUID = it.uid
            }
        }
    }

    /**
     * Get a particular session by its ID.
     *
     * @param uuid primary key of the session.
     *
     * @return a flow of the session if it exists in the database,
     * otherwise a flow with null in it.
     */
    fun getSession(uuid: String) = sessionDiskDataSource.getSession(uuid, authInteractor.userUUID)

    private val _syncedSessionsPerUser =
        hashMapOf<String, MutableStateFlow<List<DomainSession>?>?>()

    private val _openSnapshotListeners = hashMapOf<String, ListenerRegistration?>()

    private var previousUserUUID = authInteractor.userUUID

    init {
        coroutineScopeMain.launch {
            authInteractor.currentUserStateFlow.collectLatest {
                _openSnapshotListeners[previousUserUUID]?.remove()
                _openSnapshotListeners.remove(previousUserUUID)
                previousUserUUID = it?.uid
            }
        }
    }

    private val _syncedSessions = MutableStateFlow<List<DomainSession>?>(null)
    val syncedSessions = _syncedSessions.asStateFlow()

    suspend fun loadSyncedSessions(activity: Activity) {
        if (!authInteractor.isUserSignedIn) {
            _syncedSessions.value = emptyList()
            return
        }
        val userUUID = authInteractor.userUUID!!
        if (_openSnapshotListeners[userUUID] == null) {
            Timber.d("Getting synced sessions for user $userUUID")
            _syncedSessionsPerUser[userUUID] = MutableStateFlow(null)
            coroutineScopeIO.launch {
                _syncedSessionsPerUser[userUUID]!!.collectLatest {
                    _syncedSessions.value = it
                }
            }
            if (_openSnapshotListeners[userUUID] == null) {
                _openSnapshotListeners[userUUID] = sessionNetworkDataSource.getSessions(
                    activity = activity,
                    userUUID = authInteractor.userUUID!!
                ) { sessions ->
                    Timber.d("Got ${sessions?.size} synced sessions for user $userUUID")
                    _syncedSessionsPerUser[userUUID]!!.value = sessions
                    coroutineScopeIO.launch {
                        sessionDiskDataSource.saveSessions(sessions ?: emptyList())
                    }
                }
            }
        }
    }

    suspend fun uploadNotSyncedSessions() {
        if (!authInteractor.isUserSignedIn) return
        sessionDiskDataSource.getStoppedSessions(authInteractor.userUUID!!).first { sessions ->
            val localOnlySessions = sessions.filter { localSession ->
                if (syncedSessions.value != null) {
                    !syncedSessions.value!!.any { it.uuid == localSession.uuid }
                } else {
                    true
                }
            }
            locationDiskDataSource.getLocations(localOnlySessions.map { it.uuid }).first { locations ->
                uploadSessions(
                    localOnlySessions,
                    locations,
                )
                true
            }
            true
        }
    }

    suspend fun deleteAllSyncedData() {
        if (!authInteractor.isUserSignedIn) return
        Timber.d("Trying to delete user data for user ${authInteractor.userUUID}")
        sessionNetworkDataSource.deleteUserData()
    }

    fun uploadSessions(
        sessions: List<DomainSession>,
        locations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit = {},
    ) {
        if (!authInteractor.isUserSignedIn) return
        Timber.d("Upload sessions with location info to the cloud")
        sessionNetworkDataSource.insertSessions(sessions, locations) {
            Timber.d("Upload success ${authInteractor.userUUID}")
            onSuccess(it)
        }
    }

    fun uploadSession(
        session: DomainSession,
        locations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit = {},
    ) = uploadSessions(listOf(session), locations, onSuccess)

    /**
     * Get all session as a Flow.
     *
     * @return all session as a flow.
     */
    fun getSessions() = sessionDiskDataSource.getSessions(authInteractor.userUUID)

    fun getSessionUUIDs() = sessionDiskDataSource.getSessionUUIDs(authInteractor.userUUID)

    fun getNotOwnedSessions() = sessionDiskDataSource.getAllNotOwnedSessions()

    fun getOwnSessions(): Flow<List<DomainSession>>? {
        if (!authInteractor.isUserSignedIn) return null
        return sessionDiskDataSource.getSessionsByOwner(authInteractor.userUUID)
    }

    /**
     * Get ongoing sessions, which have no end date.
     *
     * @return a flow of ongoing sessions.
     */
    fun getOngoingSessions() = sessionDiskDataSource.getOngoingSessions(authInteractor.userUUID)

    /**
     * Get ongoing sessions' IDs in a quicker way than getting all
     * the information about the ongoing sessions. Cool behaviour based design :)
     *
     * @return a flow of ongoing sessions' IDs.
     */
    fun getOngoingSessionUUIDs() =
        sessionDiskDataSource.getOngoingSessionIds(authInteractor.userUUID)

    /**
     * Save session data.
     *
     * @param session updates the data of the session with the same ID.
     *
     * @return id of session updated.
     */
    fun saveSession(session: DomainSession) = saveSessions(listOf(session))

    /**
     * Save multiple sessions.
     *
     * @param sessions updates the data of the sessions with the same ID.
     */
    fun saveSessions(sessions: List<DomainSession>) {
        Timber.d("Saving ${sessions.size} sessions with IDs: ${sessions.map { it.uuid.take(4) }}")
        sessionDiskDataSource.saveSessions(sessions)
    }

    /**
     * Creates a session with the current time as
     * the starting date, end date as null and distance = 0.
     *
     * @return ID of the newly started session.
     */
    suspend fun startSession(): String {
        val sessionUUID = sessionDiskDataSource.startSession(
            ownerUserUUID = authInteractor.userUUID,
        )
        getSession(sessionUUID).first { session ->
            session?.let {
                settingsInteractor.appSettingsFlow.first { settings ->
                    if (settings.clientUUID == null) {
                        val clientUUID = UUID.randomUUID().toString()
                        settingsInteractor.updateAppSettings {
                            it.copy(clientUUID = clientUUID)
                        }
                        it.clientUUID = clientUUID
                    }
                    it.clientUUID = settings.clientUUID
                    coroutineScopeIO.launch { refreshSessionStartLocation(session) }
                    true
                }
            }
            session != null
        }
        return sessionUUID
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
    ) {
        locationDiskDataSource.getLocations(session.uuid).first { locations ->
            val sortedLocations = locations.sortedBy { location ->
                location.zonedDateTime.toInstant().toEpochMilli()
            }
            val startLocationLatLng = sortedLocations.firstOrNull()?.latLng
            session.startLocation = startLocationLatLng
            startLocationLatLng?.let { coroutineScopeIO.launch { saveSession(session) } }

            // Reverse geocoding locations to get names for them.
            if (startLocationLatLng != null && session.startLocationName == null) {
                searchInteractor.search(
                    reverseGeoOptions = ReverseGeoOptions(
                        center = Point.fromLngLat(
                            startLocationLatLng.longitude,
                            startLocationLatLng.latitude
                        )
                    )
                ) { results, _ ->
                    results.firstOrNull()?.let {
                        session.startLocationName = it.address?.place
                        session.startLocationName?.let {
                            coroutineScopeIO.launch { saveSession(session) }
                        }
                    }
                }
            }
            locations.isNotEmpty()
        }
    }

    suspend fun refreshSessionEndLocation(
        session: DomainSession,
    ) {
        locationDiskDataSource.getLocations(session.uuid).first { locations ->
            val sortedLocations = locations.sortedBy { location ->
                location.zonedDateTime.toInstant().toEpochMilli()
            }
            val endLocation = sortedLocations.lastOrNull()
            val endLocationLatLng = endLocation?.latLng
            session.endLocation = endLocationLatLng
            if (session.endDateTime == null) session.endDateTime = endLocation?.zonedDateTime
            endLocationLatLng?.let { coroutineScopeIO.launch { saveSession(session) } }

            // Reverse geocoding locations to get names for them.
            if (endLocationLatLng != null && session.endLocationName == null) {
                searchInteractor.search(
                    reverseGeoOptions = ReverseGeoOptions(
                        center = Point.fromLngLat(
                            endLocationLatLng.longitude,
                            endLocationLatLng.latitude
                        )
                    )
                ) { results, _ ->
                    results.firstOrNull()?.let {
                        session.endLocationName = it.address?.place
                        session.endLocationName?.let {
                            coroutineScopeIO.launch { saveSession(session) }
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
    suspend fun stopOngoingSessions() {
        getOngoingSessions().first { sessions ->
            sessionDiskDataSource.stopSessions(sessions)
            refreshDistanceForSessions(sessions)
            sessions.forEach { session ->
                coroutineScopeIO.launch {
                    refreshSessionStartLocation(session)
                    refreshSessionEndLocation(session)
                }
            }
            true
        }
    }

    suspend fun stopDanglingSessions() {
        if (!serviceInteractor.isJayServiceRunning()) {
            getOngoingSessions().first { sessions ->
                refreshDistanceForSessions(sessions)
                sessions.forEach { session ->
                    coroutineScopeIO.launch {
                        refreshSessionStartLocation(session)
                        refreshSessionEndLocation(session)
                    }
                }
                true
            }
        }
    }

    suspend fun refreshDistanceForSessions(sessions: List<DomainSession>) {
        locationDiskDataSource.getLocations(sessions.map { it.uuid }).first { locations ->
            sessions.forEach {
                it.distance = locations.sphericalPathLength().toFloat()
            }
            saveSessions(sessions)
            true
        }
    }

    fun ownSession(sessionUUID: String) = ownSessions(listOf(sessionUUID))

    fun ownSessions(sessionUUIDs: List<String>) {
        if (!authInteractor.isUserSignedIn) return
        sessionDiskDataSource.ownSessions(sessionUUIDs, authInteractor.userUUID!!)
    }

    fun ownAllNotOwnedSessions() {
        if (!authInteractor.isUserSignedIn) return
        sessionDiskDataSource.ownAllNotOwnedSessions(authInteractor.userUUID!!)
    }

    private fun deleteSessions(domainSessions: List<DomainSession>) {
        val stoppedSessions = domainSessions.filter { session -> session.endDateTime != null }
        stoppedSessions.forEach { session ->
            sensorEventDiskDataSource.deleteSensorEventsForSession(session.uuid)
            locationDiskDataSource.deleteLocationForSession(session.uuid)
        }
        sessionDiskDataSource.deleteSessions(stoppedSessions)
    }

    /**
     * Delete stopped sessions, whom have
     * their endTime properties not null.
     */
    suspend fun deleteStoppedSessions() {
        sessionDiskDataSource.getStoppedSessions(authInteractor.userUUID).first {
            deleteSessions(it)
            true
        }
    }

    suspend fun deleteOwnedSessions() {
        authInteractor.userUUID?.let {
            sessionDiskDataSource.getSessionsByOwner(it).first { sessions ->
                deleteSessions(sessions)
                true
            }
        }
    }

    suspend fun deleteNotOwnedSessions() {
        sessionDiskDataSource.getAllNotOwnedSessions().first {
            deleteSessions(it)
            true
        }
    }
}

