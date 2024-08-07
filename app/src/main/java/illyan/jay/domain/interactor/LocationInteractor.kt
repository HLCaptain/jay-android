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

import illyan.jay.data.firestore.datasource.PathFirestoreDataSource
import illyan.jay.data.firestore.datasource.SessionFirestoreDataSource
import illyan.jay.data.room.datasource.LocationRoomDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.di.CoroutineScopeMain
import illyan.jay.domain.model.DomainAggression
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Location interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property locationRoomDataSource local datasource
 * @constructor Create empty Location interactor
 */
@Singleton
class LocationInteractor @Inject constructor(
    private val locationRoomDataSource: LocationRoomDataSource,
    private val pathFirestoreDataSource: PathFirestoreDataSource,
    private val authInteractor: AuthInteractor,
    private val sessionInteractor: SessionInteractor,
    private val sessionFirestoreDataSource: SessionFirestoreDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
    @CoroutineScopeMain private val coroutineScopeMain: CoroutineScope,
) {
    /**
     * Get latest (most up to date) locations as a Flow for a particular session.
     *
     * @param sessionUUID particular session's ID, which is the
     * foreign key of location data returned.
     * @param limit number of latest location data returned in order from
     * the freshest location to older location data.
     *
     * @return location data flow for a particular session in order from
     * the freshest location to older location data.
     */
    fun getLatestLocations(sessionUUID: String, limit: Long): Flow<List<DomainLocation>> {
        return locationRoomDataSource.getLatestLocations(sessionUUID, limit)
    }

    fun getLatestLocations(limit: Long) = locationRoomDataSource.getLatestLocations(limit)

    /**
     * Get locations' data as a Flow for a particular session.
     *
     * @param sessionUUID particular session's ID, which is the
     * foreign key of location data returned.
     *
     * @return location data flow for a particular session.
     */
    fun getLocations(sessionUUID: String): Flow<List<DomainLocation>> {
        Timber.d("Trying to load path for session with ID from disk: ${sessionUUID.take(4)}")
        return locationRoomDataSource.getLocations(sessionUUID)
    }

    fun getLocations(sessionUUIDs: List<String>): Flow<List<DomainLocation>> {
        Timber.d("Trying to load paths for session with ID from disk: ${sessionUUIDs.map { it.take(4) }}")
        return locationRoomDataSource.getLocations(sessionUUIDs)
    }

    fun getAggressions(sessionUUID: String): Flow<List<DomainAggression>> {
        Timber.d("Trying to load path for session with ID from disk: ${sessionUUID.take(4)}")
        return locationRoomDataSource.getAggressions(sessionUUID)
    }

    val syncedPathCollectionJobs = hashMapOf<String, Job>()

    /**
     * Get the path for a session, either it being in the cloud or in the local database.
     * Cannot react to user/session/location state changes, if the location was found in
     * the local database, gets uploaded, then gets deleted from the local database, the listener
     * would still listen to the local database's data.
     */
    suspend fun getSyncedPath(sessionUUID: String): StateFlow<List<DomainLocation>?> {
        Timber.i("Trying to load path for session with ID: $sessionUUID")
        if (syncedPathCollectionJobs[sessionUUID] != null) {
            Timber.v("Cancelling current data collection job regarding the path of $sessionUUID")
            syncedPathCollectionJobs[sessionUUID]?.cancel(CancellationException("New data collection job requested"))
            syncedPathCollectionJobs.remove(sessionUUID)
        }
        val syncedPaths = MutableStateFlow<List<DomainLocation>?>(null)
        syncedPathCollectionJobs[sessionUUID] = coroutineScopeMain.launch(start = CoroutineStart.LAZY) {
            pathFirestoreDataSource.getLocationsBySession(sessionUUID).collectLatest { remoteLocations ->
                coroutineScopeIO.launch {
                    Timber.v("Found location for session, caching it on disk")
                    remoteLocations?.let { saveLocations(it) }
                }
                syncedPaths.update { remoteLocations }
            }
        }
        val session = sessionInteractor.getSession(sessionUUID).first()
        if (session != null) {
            Timber.v("Found session on disk")
            coroutineScopeIO.launch {
                val locations = getLocations(sessionUUID).first()
                if (locations.isEmpty()) {
                    Timber.v("Not found path for session on disk, checking cloud")
                    if (!authInteractor.isUserSignedIn) {
                        Timber.i("Not authenticated to access cloud, return an empty list")
                        syncedPaths.update { emptyList() }
                    } else {
                        syncedPathCollectionJobs[sessionUUID]?.start()
                    }
                } else {
                    Timber.i("Found path on disk")
                    syncedPaths.update { locations }
                }
            }
        } else {
            Timber.v("Not found session on disk, checking cloud")
            if (!authInteractor.isUserSignedIn) {
                Timber.i("Not authenticated to access cloud, return an empty list")
                syncedPaths.update { emptyList() }
            } else {
                coroutineScopeIO.launch {
                    sessionFirestoreDataSource.sessions.first { sessions ->
                        if (sessions != null && sessions.any { it.uuid == sessionUUID }) {
                            Timber.v("Found session in cloud, caching it on disk")
                            coroutineScopeIO.launch {
                                sessionInteractor.saveSession(sessions.first { it.uuid == sessionUUID })
                                syncedPathCollectionJobs[sessionUUID]?.start()
                            }
                        }
                        sessions != null
                    }
                }
            }
        }
        return syncedPaths.asStateFlow()
    }

    val syncedPathAggressionCollectionJobs = hashMapOf<String, Job>()

    /**
     * Get the path for a session, either it being in the cloud or in the local database.
     * Cannot react to user/session/location state changes, if the location was found in
     * the local database, gets uploaded, then gets deleted from the local database, the listener
     * would still listen to the local database's data.
     */
    suspend fun getSyncedPathAggressions(sessionUUID: String): StateFlow<List<DomainAggression>?> {
        Timber.i("Trying to load path aggressions for session with ID: $sessionUUID")
        if (syncedPathAggressionCollectionJobs[sessionUUID] != null) {
            Timber.v("Cancelling current data collection job regarding the path aggressions of $sessionUUID")
            syncedPathAggressionCollectionJobs[sessionUUID]?.cancel(CancellationException("New data collection job requested"))
            syncedPathAggressionCollectionJobs.remove(sessionUUID)
        }
        val syncedPathAggressions = MutableStateFlow<List<DomainAggression>?>(null)
        syncedPathAggressionCollectionJobs[sessionUUID] = coroutineScopeMain.launch(start = CoroutineStart.LAZY) {
            pathFirestoreDataSource.getAggressionsBySession(sessionUUID).collectLatest { remoteAggressions ->
                coroutineScopeIO.launch {
                    Timber.v("Found aggressions for session, caching it on disk")
                    remoteAggressions?.let { saveAggressions(it) }
                }
                syncedPathAggressions.update { remoteAggressions }
            }
        }
        val session = sessionInteractor.getSession(sessionUUID).first()
        if (session != null) {
            Timber.v("Found session on disk")
            coroutineScopeIO.launch {
                val aggressions = getAggressions(sessionUUID).first()
                if (aggressions.isEmpty()) {
                    Timber.v("Not found path aggressions for session on disk, checking cloud")
                    if (!authInteractor.isUserSignedIn) {
                        Timber.i("Not authenticated to access cloud, return an empty list")
                        syncedPathAggressions.update { emptyList() }
                    } else {
                        syncedPathAggressionCollectionJobs[sessionUUID]?.start()
                    }
                } else {
                    Timber.i("Found path aggressions on disk")
                    syncedPathAggressions.update { aggressions }
                }
            }
        } else {
            Timber.v("Not found session on disk, checking cloud")
            if (!authInteractor.isUserSignedIn) {
                Timber.i("Not authenticated to access cloud, return an empty list")
                syncedPathAggressions.update { emptyList() }
            } else {
                coroutineScopeIO.launch {
                    sessionFirestoreDataSource.sessions.first { sessions ->
                        if (sessions != null && sessions.any { it.uuid == sessionUUID }) {
                            Timber.v("Found session in cloud, caching it on disk")
                            coroutineScopeIO.launch {
                                sessionInteractor.saveSession(sessions.first { it.uuid == sessionUUID })
                                syncedPathAggressionCollectionJobs[sessionUUID]?.start()
                            }
                        }
                        sessions != null
                    }
                }
            }
        }
        return syncedPathAggressions.asStateFlow()
    }

    /**
     * Save location's data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param location location data saved onto the Room database.
     *
     * @return id of location updated.
     */
    fun saveLocation(location: DomainLocation) = locationRoomDataSource.saveLocation(location)

    /**
     * Save locations' data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param locations list of location data saved onto the Room database.
     */
    fun saveLocations(locations: List<DomainLocation>) {
        locationRoomDataSource.saveLocations(locations)
    }

    fun saveAggressions(aggressions: List<DomainAggression>) {
        if (aggressions.isEmpty()) {
            Timber.v("No aggressions to save")
            return
        }
        locationRoomDataSource.saveAggressionsForSession(aggressions.first().sessionUUID, aggressions)
    }

    fun isPathStoredForSession(sessionUUID: String): Flow<Boolean> {
        Timber.d("Checking if a path is stored for session $sessionUUID")
        return locationRoomDataSource.getLatestLocations(sessionUUID, 1).map { it.isNotEmpty() }
    }

    companion object {
        const val LOCATION_REQUEST_INTERVAL_REALTIME = 200L
        const val LOCATION_REQUEST_INTERVAL_FREQUENT = 500L
        const val LOCATION_REQUEST_INTERVAL_DEFAULT = 2000L
        const val LOCATION_REQUEST_INTERVAL_SPARSE = 4000L

        const val LOCATION_REQUEST_DISPLACEMENT_MOST_ACCURATE = 1f
        const val LOCATION_REQUEST_DISPLACEMENT_DEFAULT = 4f
        const val LOCATION_REQUEST_DISPLACEMENT_LEAST_ACCURATE = 8f
    }
}
