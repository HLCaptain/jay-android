/*
 * Copyright (c) 2022-2024 Balázs Püspök-Kiss (Illyan)
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
import com.google.firebase.firestore.WriteBatch
import com.mapbox.geojson.Point
import com.mapbox.search.ReverseGeoOptions
import illyan.jay.data.datastore.datasource.AppSettingsDataSource
import illyan.jay.data.firestore.datasource.PathFirestoreDataSource
import illyan.jay.data.firestore.datasource.SensorEventsFirestoreDataSource
import illyan.jay.data.firestore.datasource.SessionFirestoreDataSource
import illyan.jay.data.room.datasource.LocationRoomDataSource
import illyan.jay.data.room.datasource.SensorEventRoomDataSource
import illyan.jay.data.room.datasource.SessionRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.model.DomainAggression
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSensorEvent
import illyan.jay.domain.model.DomainSession
import illyan.jay.util.awaitOperations
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property sessionRoomDataSource local database
 * @constructor Create empty Session interactor
 */
@Singleton
class SessionInteractor @Inject constructor(
    private val sessionRoomDataSource: SessionRoomDataSource,
    private val sensorEventRoomDataSource: SensorEventRoomDataSource,
    private val locationRoomDataSource: LocationRoomDataSource,
    private val searchInteractor: SearchInteractor,
    private val sessionFirestoreDataSource: SessionFirestoreDataSource,
    private val authInteractor: AuthInteractor,
    private val appSettingsDataSource: AppSettingsDataSource,
    private val serviceInteractor: ServiceInteractor,
    private val pathFirestoreDataSource: PathFirestoreDataSource,
    private val sensorEventsFirestoreDataSource: SensorEventsFirestoreDataSource,
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
        return sessionRoomDataSource.getSession(uuid, authInteractor.userUUID).map { session ->
            session?.let { refreshSessionLocation(it) }
            session
        }
    }

    private fun getSessions(uuids: List<String>): Flow<List<DomainSession>> {
        return sessionRoomDataSource.getSessions(uuids).map { sessions ->
            sessions.forEach { refreshSessionLocation(it) }
            sessions
        }
    }

    val syncedSessions: StateFlow<List<DomainSession>?> = sessionFirestoreDataSource.sessions.map { sessions ->
        sessions?.let {
            Timber.i("Got ${sessions.size} synced sessions for user ${sessions.firstOrNull()?.ownerUUID?.take(4)}")
            sessionRoomDataSource.saveSessions(sessions)
        }
        sessions
    }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

    suspend fun uploadNotSyncedSessions() {
        if (!authInteractor.isUserSignedIn) return
        sessionRoomDataSource.getStoppedSessions(authInteractor.userUUID!!).first { sessions ->
            val localOnlySessions = sessions.filter { localSession ->
                if (syncedSessions.value != null) {
                    !syncedSessions.value!!.any { it.uuid == localSession.uuid }
                } else {
                    true
                }
            }
            Timber.i("${localOnlySessions.size} sessions are not synced with IDs: ${localOnlySessions.map { it.uuid.take(4) }}")
            val locations = locationRoomDataSource.getLocations(localOnlySessions.map { it.uuid }).first()
            val aggressions = locationRoomDataSource.getAggressions(localOnlySessions.map { it.uuid }).first()
            val sensorEvents = sensorEventRoomDataSource.getSensorEvents(localOnlySessions.map { it.uuid }).first()
            uploadSessions(
                localOnlySessions,
                locations,
                sensorEvents,
                aggressions
            )
            true
        }
    }

    suspend fun deleteSyncedSessions() {
        if (!authInteractor.isUserSignedIn) return
        Timber.d("Batch created to delete session data for user ${authInteractor.userUUID?.take(4)} from cloud")
        firestore.runBatch(3) { batch, onOperationFinished ->
            sessionFirestoreDataSource.deleteAllSessions(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
            pathFirestoreDataSource.deleteLocationsForUser(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
            sensorEventsFirestoreDataSource.deleteSensorEventsForUser(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
        }
    }

    suspend fun deleteAllSyncedData() {
        if (!authInteractor.isUserSignedIn) return
        Timber.d("Batch created to delete user ${authInteractor.userUUID?.take(4)} from cloud")
        firestore.runBatch(1) { batch, onOperationFinished ->
            deleteAllSyncedData(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
        }
    }

    suspend fun deleteAllSyncedData(
        batch: WriteBatch,
        onWriteFinished: () -> Unit = {}
    ) {
        awaitOperations(3) { onOperationFinished ->
            sessionFirestoreDataSource.deleteAllSessions(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
            pathFirestoreDataSource.deleteLocationsForUser(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
            sensorEventsFirestoreDataSource.deleteSensorEventsForUser(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
        }
        onWriteFinished()
    }

    fun uploadSessions(
        sessions: List<DomainSession>,
        locations: List<DomainLocation>,
        sensorEvents: List<DomainSensorEvent>,
        aggressions: List<DomainAggression>,
        onSuccess: (List<DomainSession>) -> Unit = { Timber.i("Uploaded locations for ${sessions.size} sessions") },
    ) {
        if (!authInteractor.isUserSignedIn || sessions.isEmpty()) return
        Timber.i("Upload ${sessions.size} sessions with location info to the cloud")
        firestore.runBatch { batch ->
            sessionFirestoreDataSource.insertSessions(
                batch = batch,
                domainSessions = sessions
            )
            pathFirestoreDataSource.insertLocations(
                batch = batch,
                domainSessions = sessions,
                domainLocations = locations,
                domainAggressions = aggressions
            )
            sensorEventsFirestoreDataSource.insertEvents(
                batch = batch,
                domainSessions = sessions,
                domainSensorEvents = sensorEvents
            )
        }.addOnSuccessListener {
            onSuccess(sessions)
        }
    }

    fun uploadSession(
        session: DomainSession,
        locations: List<DomainLocation>,
        sensorEvents: List<DomainSensorEvent>,
        aggressions: List<DomainAggression>,
        onSuccess: (List<DomainSession>) -> Unit = { Timber.i("Uploaded locations session ${session.uuid}") },
    ) = uploadSessions(listOf(session), locations, sensorEvents, aggressions, onSuccess)

    /**
     * Get all session as a Flow.
     *
     * @return all session as a flow.
     */
    fun getSessions() = sessionRoomDataSource.getSessions(authInteractor.userUUID)

    fun getSessionUUIDs() = sessionRoomDataSource.getSessionUUIDs(authInteractor.userUUID)

    fun getNotOwnedSessions() = sessionRoomDataSource.getAllNotOwnedSessions()

    fun getOwnSessions(): Flow<List<DomainSession>> {
        return if (!authInteractor.isUserSignedIn) {
            flowOf(emptyList())
        } else {
            sessionRoomDataSource.getSessionsByOwner(authInteractor.userUUID)
        }
    }

    /**
     * Get ongoing sessions, which have no end date.
     *
     * @return a flow of ongoing sessions.
     */
    fun getOngoingSessions() = sessionRoomDataSource.getOngoingSessions(authInteractor.userUUID)

    /**
     * Get ongoing sessions' IDs in a quicker way than getting all
     * the information about the ongoing sessions. Cool behaviour based design :)
     *
     * @return a flow of ongoing sessions' IDs.
     */
    fun getOngoingSessionUUIDs() =
        sessionRoomDataSource.getOngoingSessionIds(authInteractor.userUUID)

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
        sessionRoomDataSource.saveSessions(sessions)
    }

    /**
     * Creates a session with the current time as
     * the starting date, end date as null and distance = 0.
     *
     * @return ID of the newly started session.
     */
    suspend fun startSession(): String {
        val sessionUUID = sessionRoomDataSource.startSession(
            ownerUUID = authInteractor.userUUID,
        )
        Timber.i("Starting a session with ID: $sessionUUID")
        coroutineScopeIO.launch {
            getSession(sessionUUID).first { session ->
                session?.let {
                    Timber.i("Trying to assign client to session $sessionUUID")
                    appSettingsDataSource.appSettings.first { settings ->
                        Timber.d("Client UUID = ${settings.clientUUID}")
                        if (settings.clientUUID == null) {
                            val clientUUID = UUID.randomUUID().toString()
                            Timber.d("Generating new client UUID: $clientUUID")
                            appSettingsDataSource.updateAppSettings {
                                it.copy(clientUUID = clientUUID)
                            }
                            it.clientUUID = clientUUID
                        } else {
                            it.clientUUID = settings.clientUUID
                        }
                        Timber.d("Assigned client ${it.clientUUID?.take(4)} to session: $sessionUUID")
                        coroutineScopeIO.launch {
                            sessionRoomDataSource.assignClientToSession(sessionUUID, session.clientUUID)
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
        val sessionId = sessionRoomDataSource.stopSession(session)
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
        locationRoomDataSource.getLocations(session.uuid).first { locations ->
            val startLocationLatLng = locations.minByOrNull {
                it.zonedDateTime.toInstant().toEpochMilli()
            }?.latLng
            session.startLocation = startLocationLatLng
            startLocationLatLng?.let {
                coroutineScopeIO.launch {
                    sessionRoomDataSource.saveStartLocationForSession(
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
                                sessionRoomDataSource.saveStartLocationNameForSession(
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
        locationRoomDataSource.getLocations(session.uuid).first { locations ->
            val endLocation = locations.maxByOrNull {
                it.zonedDateTime.toInstant().toEpochMilli()
            }
            val endLocationLatLng = endLocation?.latLng
            session.endLocation = endLocationLatLng
            if (session.endDateTime == null) session.endDateTime = endLocation?.zonedDateTime
            endLocationLatLng?.let {
                coroutineScopeIO.launch {
                    sessionRoomDataSource.saveEndLocationForSession(
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
                                sessionRoomDataSource.saveEndLocationNameForSession(
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
            stopOngoingSessions()
        }
    }

    suspend fun refreshDistanceForSessions(sessions: List<DomainSession>) {
        locationRoomDataSource.getLocations(sessions.map { it.uuid }).first { locations ->
            sessions.forEach {
                it.distance = locations.sphericalPathLength().toFloat()
            }
            saveSessions(sessions)
            true
        }
    }

    suspend fun refreshDistanceForSession(session: DomainSession) {
        locationRoomDataSource.getLocations(session.uuid).first { locations ->
            coroutineScopeIO.launch {
                sessionRoomDataSource.saveDistanceForSession(
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
        sessionRoomDataSource.ownSessions(sessionUUIDs, authInteractor.userUUID!!)
    }

    fun ownAllNotOwnedSessions() {
        if (!authInteractor.isUserSignedIn) return
        sessionRoomDataSource.ownAllNotOwnedSessions(authInteractor.userUUID!!)
    }

    fun deleteSessionLocally(sessionUUID: String) = deleteSessionsLocally(listOf(sessionUUID))

    @JvmName("deleteSessionsLocallyByUUIDs")
    fun deleteSessionsLocally(sessionUUIDs: List<String>) {
        coroutineScopeIO.launch {
            getSessions(sessionUUIDs).first {  sessions ->
                val stoppedSessions = sessions.filter { session -> session.endDateTime != null }
                stoppedSessions.forEach { session ->
                    sensorEventRoomDataSource.deleteSensorEventsForSession(session.uuid)
                    locationRoomDataSource.deleteLocationForSession(session.uuid)
                }
                sessionRoomDataSource.deleteSessions(stoppedSessions)
                true
            }
        }
    }

    fun deleteSessionLocally(domainSession: DomainSession) = deleteSessionsLocally(listOf(domainSession))

    fun deleteSessionsLocally(domainSessions: List<DomainSession>) {
        val stoppedSessions = domainSessions.filter { session -> session.endDateTime != null }
        stoppedSessions.forEach { session ->
            sensorEventRoomDataSource.deleteSensorEventsForSession(session.uuid)
            locationRoomDataSource.deleteLocationForSession(session.uuid)
            locationRoomDataSource.deleteAggressionsForSession(session.uuid)
        }
        sessionRoomDataSource.deleteSessions(stoppedSessions)
    }

    suspend fun deleteSessionFromCloud(sessionUUID: String) = deleteSessionsFromCloud(listOf(sessionUUID))

    @JvmName("deleteSessionsFromCloudByUUIDs")
    suspend fun deleteSessionsFromCloud(sessionUUIDs: List<String>) {
        Timber.d("Batch created to delete ${sessionUUIDs.size} sessions from cloud")
        firestore.runBatch(2) { batch, onOperationFinished ->
            sessionFirestoreDataSource.deleteSessions(
                batch = batch,
                sessionUUIDs = sessionUUIDs,
                onWriteFinished = onOperationFinished
            )
            pathFirestoreDataSource.deleteLocationsForSessions(
                batch = batch,
                sessionUUIDs = sessionUUIDs,
                onWriteFinished = onOperationFinished
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
        sessionRoomDataSource.getStoppedSessions(authInteractor.userUUID).first { sessions ->
            Timber.i("${authInteractor.userUUID?.take(4)} deleting stopped sessions: ${sessions.map { it.uuid.take(4) }}")
            deleteSessionsLocally(sessions)
            true
        }
    }

    suspend fun deleteOwnedSessions() {
        authInteractor.userUUID?.let { ownerUUID ->
            sessionRoomDataSource.getSessionsByOwner(ownerUUID).first { sessions ->
                Timber.i("${authInteractor.userUUID} deleting (all of its) owned sessions: ${sessions.map { it.uuid.take(4) }}")
                deleteSessionsLocally(sessions)
                true
            }
        }
    }

    suspend fun deleteNotOwnedSessions() {
        sessionRoomDataSource.getAllNotOwnedSessions().first { sessions ->
            Timber.i("Deleting all not owned sessions: ${sessions.map { it.uuid.take(4) }}")
            deleteSessionsLocally(sessions)
            true
        }
    }

    suspend fun uploadSessionAggressions(aggressions: List<DomainAggression>) {
        if (!authInteractor.isUserSignedIn) {
            Timber.i("User is not signed in, not uploading aggressions")
            return
        }
        if (aggressions.isEmpty()) {
            Timber.i("No aggressions to upload")
            return
        }
        val sessionUUID = aggressions.first().sessionUUID
        if (syncedSessions.value?.map { it.uuid }?.contains(sessionUUID) == true) {
            Timber.i("Uploading ${aggressions.size} aggressions to the cloud")
            val location = locationRoomDataSource.getLocations(sessionUUID).first()
            val sessions = listOf(getSession(sessionUUID).first()!!)
            firestore.runBatch { batch ->
                pathFirestoreDataSource.insertLocations(
                    batch = batch,
                    domainSessions = sessions,
                    domainLocations = location,
                    domainAggressions = aggressions
                )
            }
        } else {
            Timber.i("Session $sessionUUID is not synced, not uploading aggressions")
        }
    }
}

