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

import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import com.mapbox.search.ReverseGeoOptions
import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.data.disk.datasource.SensorEventDiskDataSource
import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.data.network.datasource.LocationNetworkDataSource
import illyan.jay.data.network.datasource.SessionNetworkDataSource
import illyan.jay.data.network.datasource.UserNetworkDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.completeNext
import illyan.jay.util.runBatch
import illyan.jay.util.sphericalPathLength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
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
    private val locationNetworkDataSource: LocationNetworkDataSource,
    private val userNetworkDataSource: UserNetworkDataSource,
    private val firestore: FirebaseFirestore,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
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
    fun getSession(uuid: String): Flow<DomainSession?> {
        return sessionDiskDataSource.getSession(uuid, authInteractor.userUUID).map { session ->
            session?.let { refreshSessionLocation(it) }
            session
        }
    }

    private fun getSessions(uuids: List<String>): Flow<List<DomainSession>> {
        return sessionDiskDataSource.getSessions(uuids).map { sessions ->
            sessions.forEach { refreshSessionLocation(it) }
            sessions
        }
    }

    val syncedSessions: StateFlow<List<DomainSession>?> get() = sessionNetworkDataSource.sessions.map { sessions ->
        sessions?.let {
            Timber.i("Got ${sessions.size} synced sessions for user ${sessions.firstOrNull()?.ownerUUID?.take(4)}")
            sessionDiskDataSource.saveSessions(sessions)
        }
        sessions
    }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

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
            Timber.i("${localOnlySessions.size} sessions are not synced with IDs: ${localOnlySessions.map { it.uuid.take(4) }}")
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

    suspend fun deleteSyncedSessions() {
        if (!authInteractor.isUserSignedIn) return
        Timber.d("Batch created to delete session data for user ${authInteractor.userUUID?.take(4)} from cloud")
        firestore.runBatch(2) { batch, completableDeferred ->
            sessionNetworkDataSource.deleteAllSessions(
                batch = batch,
                onWriteFinished = { completableDeferred.completeNext() }
            )
            locationNetworkDataSource.deleteLocationsForUser(
                batch = batch,
                onWriteFinished = { completableDeferred.completeNext() }
            )
        }
    }

    suspend fun deleteAllSyncedData() {
        if (!authInteractor.isUserSignedIn) return
        Timber.d("Batch created to delete user ${authInteractor.userUUID?.take(4)} from cloud")
        firestore.runBatch(2) { batch, completableDeferred ->
            userNetworkDataSource.deleteUserData(
                batch = batch,
                onWriteFinished = { completableDeferred.completeNext() }
            )
            locationNetworkDataSource.deleteLocationsForUser(
                batch = batch,
                onWriteFinished = { completableDeferred.completeNext() }
            )
        }
    }

    fun uploadSessions(
        sessions: List<DomainSession>,
        locations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit = { Timber.i("Uploaded locations for ${sessions.size} sessions") },
    ) {
        if (!authInteractor.isUserSignedIn || sessions.isEmpty()) return
        Timber.i("Upload ${sessions.size} sessions with location info to the cloud")
        firestore.runBatch { batch ->
            sessionNetworkDataSource.insertSessions(
                batch = batch,
                domainSessions = sessions
            )
            locationNetworkDataSource.insertLocations(
                batch = batch,
                domainSessions = sessions,
                domainLocations = locations
            )
        }.addOnSuccessListener {
            onSuccess(sessions)
        }
    }

    fun uploadSession(
        session: DomainSession,
        locations: List<DomainLocation>,
        onSuccess: (List<DomainSession>) -> Unit = { Timber.i("Uploaded locations session ${session.uuid}") },
    ) = uploadSessions(listOf(session), locations, onSuccess)

    /**
     * Get all session as a Flow.
     *
     * @return all session as a flow.
     */
    fun getSessions() = sessionDiskDataSource.getSessions(authInteractor.userUUID)

    fun getSessionUUIDs() = sessionDiskDataSource.getSessionUUIDs(authInteractor.userUUID)

    fun getNotOwnedSessions() = sessionDiskDataSource.getAllNotOwnedSessions()

    fun getOwnSessions(): Flow<List<DomainSession>> {
        return if (!authInteractor.isUserSignedIn) {
            flowOf(emptyList())
        } else {
            sessionDiskDataSource.getSessionsByOwner(authInteractor.userUUID)
        }
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
        Timber.i("Saving ${sessions.size} sessions with IDs: ${sessions.map { it.uuid.take(4) }}")
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
            ownerUUID = authInteractor.userUUID,
        )
        Timber.i("Starting a session with ID: $sessionUUID")
        coroutineScopeIO.launch {
            getSession(sessionUUID).first { session ->
                session?.let {
                    Timber.i("Trying to assign client to session $sessionUUID")
                    settingsInteractor.appSettingsFlow.first { settings ->
                        Timber.d("Client UUID = ${settings.clientUUID}")
                        if (settings.clientUUID == null) {
                            val clientUUID = UUID.randomUUID().toString()
                            Timber.d("Generating new client UUID: $clientUUID")
                            settingsInteractor.updateAppSettings {
                                it.copy(clientUUID = clientUUID)
                            }
                            it.clientUUID = clientUUID
                        } else {
                            it.clientUUID = settings.clientUUID
                        }
                        Timber.d("Assigned client ${it.clientUUID?.take(4)} to session: $sessionUUID")
                        coroutineScopeIO.launch {
                            sessionDiskDataSource.assignClientToSession(sessionUUID, session.clientUUID)
                            refreshSessionStartLocation(session)
                        }
                        true
                    }
                }
                session != null
            }
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
    suspend fun stopSession(session: DomainSession): Long {
        val sessionId = sessionDiskDataSource.stopSession(session)
        refreshDistanceForSession(session)
        refreshSessionEndLocation(session)
        return sessionId
    }

    fun refreshSessionLocation(session: DomainSession) {
        if (session.startLocation == null ||
            session.startLocationName == null
        ) {
            coroutineScopeIO.launch {
                refreshSessionStartLocation(session)
            }
        }
        if (session.endDateTime != null &&
            (session.endLocation == null || session.endLocationName == null)
        ) {
            coroutineScopeIO.launch {
                refreshSessionEndLocation(session)
            }
        }
    }

    suspend fun refreshSessionStartLocation(
        session: DomainSession,
    ) {
        locationDiskDataSource.getLocations(session.uuid).first { locations ->
            val startLocationLatLng = locations.minByOrNull {
                it.zonedDateTime.toInstant().toEpochMilli()
            }?.latLng
            session.startLocation = startLocationLatLng
            startLocationLatLng?.let {
                coroutineScopeIO.launch {
                    sessionDiskDataSource.saveStartLocationForSession(
                        session.uuid,
                        startLocationLatLng
                    )
                }
            }

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
                            coroutineScopeIO.launch {
                                sessionDiskDataSource.saveStartLocationNameForSession(
                                    session.uuid,
                                    it
                                )
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
    ) {
        locationDiskDataSource.getLocations(session.uuid).first { locations ->
            val endLocation = locations.maxByOrNull {
                it.zonedDateTime.toInstant().toEpochMilli()
            }
            val endLocationLatLng = endLocation?.latLng
            session.endLocation = endLocationLatLng
            if (session.endDateTime == null) session.endDateTime = endLocation?.zonedDateTime
            endLocationLatLng?.let {
                coroutineScopeIO.launch {
                    sessionDiskDataSource.saveEndLocationForSession(
                        session.uuid,
                        endLocationLatLng
                    )
                }
            }

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
                            coroutineScopeIO.launch {
                                sessionDiskDataSource.saveEndLocationNameForSession(
                                    session.uuid,
                                    it
                                )
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
    suspend fun stopOngoingSessions() {
        getOngoingSessions().first { sessions ->
            sessionDiskDataSource.stopSessions(sessions)
            refreshDistanceForSessions(sessions)
            sessions.forEach { session ->
                coroutineScopeIO.launch {
                    stopSession(session)
                }
            }
            true
        }
    }

    suspend fun stopDanglingSessions() {
        if (!serviceInteractor.isJayServiceRunning()) {
            getOngoingSessions().first { sessions ->
                sessions.forEach { session ->
                    coroutineScopeIO.launch {
                        stopSession(session)
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

    suspend fun refreshDistanceForSession(session: DomainSession) {
        locationDiskDataSource.getLocations(session.uuid).first { locations ->
            coroutineScopeIO.launch {
                sessionDiskDataSource.saveDistanceForSession(
                    session.uuid,
                    locations.sphericalPathLength().toFloat()
                )
            }
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

    fun deleteSessionLocally(sessionUUID: String) = deleteSessionsLocally(listOf(sessionUUID))

    @JvmName("deleteSessionsLocallyByUUIDs")
    fun deleteSessionsLocally(sessionUUIDs: List<String>) {
        coroutineScopeIO.launch {
            getSessions(sessionUUIDs).first {  sessions ->
                val stoppedSessions = sessions.filter { session -> session.endDateTime != null }
                stoppedSessions.forEach { session ->
                    sensorEventDiskDataSource.deleteSensorEventsForSession(session.uuid)
                    locationDiskDataSource.deleteLocationForSession(session.uuid)
                }
                sessionDiskDataSource.deleteSessions(stoppedSessions)
                true
            }
        }
    }

    fun deleteSessionLocally(domainSession: DomainSession) = deleteSessionsLocally(listOf(domainSession))

    fun deleteSessionsLocally(domainSessions: List<DomainSession>) {
        val stoppedSessions = domainSessions.filter { session -> session.endDateTime != null }
        stoppedSessions.forEach { session ->
            sensorEventDiskDataSource.deleteSensorEventsForSession(session.uuid)
            locationDiskDataSource.deleteLocationForSession(session.uuid)
        }
        sessionDiskDataSource.deleteSessions(stoppedSessions)
    }

    suspend fun deleteSessionFromCloud(sessionUUID: String) = deleteSessionsFromCloud(listOf(sessionUUID))

    @JvmName("deleteSessionsFromCloudByUUIDs")
    suspend fun deleteSessionsFromCloud(sessionUUIDs: List<String>) {
        Timber.d("Batch created to delete ${sessionUUIDs.size} sessions from cloud")
        firestore.runBatch(2) { batch, completableDeferred ->
            sessionNetworkDataSource.deleteSessions(
                batch = batch,
                sessionUUIDs = sessionUUIDs,
                onWriteFinished = { completableDeferred.completeNext() }
            )
            locationNetworkDataSource.deleteLocationsForSessions(
                batch = batch,
                sessionUUIDs = sessionUUIDs,
                onWriteFinished = { completableDeferred.completeNext() }
            )
        }
    }

    suspend fun deleteSessionFromCloud(domainSession: DomainSession) = deleteSessionsFromCloud(listOf(domainSession))

    suspend fun deleteSessionsFromCloud(domainSessions: List<DomainSession>) {
        deleteSessionsFromCloud(domainSessions.map { it.uuid })
    }

    /**
     * Delete stopped sessions, whom have
     * their endTime properties not null.
     */
    suspend fun deleteStoppedSessions() {
        sessionDiskDataSource.getStoppedSessions(authInteractor.userUUID).first { sessions ->
            Timber.i("${authInteractor.userUUID} deleting stopped sessions: ${sessions.map { it.uuid.take(4) }}")
            deleteSessionsLocally(sessions)
            true
        }
    }

    suspend fun deleteOwnedSessions() {
        authInteractor.userUUID?.let { ownerUUID ->
            sessionDiskDataSource.getSessionsByOwner(ownerUUID).first { sessions ->
                Timber.i("${authInteractor.userUUID} deleting (all of its) owned sessions: ${sessions.map { it.uuid.take(4) }}")
                deleteSessionsLocally(sessions)
                true
            }
        }
    }

    suspend fun deleteNotOwnedSessions() {
        sessionDiskDataSource.getAllNotOwnedSessions().first { sessions ->
            Timber.i("Deleting all not owned sessions: ${sessions.map { it.uuid.take(4) }}")
            deleteSessionsLocally(sessions)
            true
        }
    }
}

