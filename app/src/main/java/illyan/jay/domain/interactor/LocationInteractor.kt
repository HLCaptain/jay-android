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

import illyan.jay.data.disk.datasource.LocationDiskDataSource
import illyan.jay.data.network.datasource.LocationNetworkDataSource
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property locationDiskDataSource local datasource
 * @constructor Create empty Location interactor
 */
@Singleton
class LocationInteractor @Inject constructor(
    private val locationDiskDataSource: LocationDiskDataSource,
    private val locationNetworkDataSource: LocationNetworkDataSource,
    private val authInteractor: AuthInteractor,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
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
        return locationDiskDataSource.getLatestLocations(sessionUUID, limit)
    }

    fun getLatestLocations(limit: Long) = locationDiskDataSource.getLatestLocations(limit)

    /**
     * Get locations' data as a Flow for a particular session.
     *
     * @param sessionUUID particular session's ID, which is the
     * foreign key of location data returned.
     *
     * @return location data flow for a particular session.
     */
    fun getLocations(sessionUUID: String) = locationDiskDataSource.getLocations(sessionUUID)

    fun getLocations(sessionUUIDs: List<String>) = locationDiskDataSource.getLocations(sessionUUIDs)

    fun getSyncedPath(sessionUUID: String) = getSyncedPaths(listOf(sessionUUID))

    fun getSyncedPaths(sessionUUIDs: List<String>): StateFlow<List<DomainLocation>?> {
        val syncedPaths = MutableStateFlow<List<DomainLocation>?>(null)
        if (authInteractor.isUserSignedIn) {
            locationNetworkDataSource.getLocations(sessionUUIDs) { remoteLocations ->
                syncedPaths.value = remoteLocations
            }
        } else {
            syncedPaths.value = emptyList()
        }
        return syncedPaths.asStateFlow()
    }

    /**
     * Save location's data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param location location data saved onto the Room database.
     *
     * @return id of location updated.
     */
    fun saveLocation(location: DomainLocation) = locationDiskDataSource.saveLocation(location)

    /**
     * Save locations' data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param locations list of location data saved onto the Room database.
     */
    fun saveLocations(locations: List<DomainLocation>) {
        coroutineScopeIO.launch {
            locationDiskDataSource.saveLocations(locations)
        }
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
