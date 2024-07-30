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

package illyan.jay.data.room.datasource

import illyan.jay.data.room.dao.LocationDao
import illyan.jay.data.room.model.RoomAggression
import illyan.jay.data.room.model.RoomLocation
import illyan.jay.data.room.toDomainModel
import illyan.jay.data.room.toRoomModel
import illyan.jay.domain.model.DomainAggression
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location disk data source using Room to communicate with the SQLite database.
 *
 * @property locationDao used to insert, update, delete and query commands using Room.
 * @constructor Create empty Location disk data source
 */
@Singleton
class LocationRoomDataSource @Inject constructor(
    private val locationDao: LocationDao
) {
    /**
     * Get latest (most up to date) locations as a Flow for a particular session.
     *
     * @param sessionUUID particular session's ID, which is the
     * foreign key of the location data returned.
     * @param limit number of latest location data returned in order from
     * the freshest location to older location data.
     *
     * @return location data flow for a particular session in order from
     * the freshest location to older location data.
     */
    fun getLatestLocations(sessionUUID: String, limit: Long) =
        locationDao.getLatestLocations(sessionUUID, limit)
            .map { it.map(RoomLocation::toDomainModel) }

    fun getLatestLocations(limit: Long) =
        locationDao.getLatestLocations(limit)
            .map { it.map(RoomLocation::toDomainModel) }

    /**
     * Get locations' data as a Flow for a particular session.
     *
     * @param sessionUUID particular session's ID, which is the
     * foreign key of location data returned.
     *
     * @return location data flow for a particular session.
     */
    fun getLocations(sessionUUID: String) = locationDao.getLocations(sessionUUID)
        .map { it.map(RoomLocation::toDomainModel) }

    fun getLocations(sessionUUIDs: List<String>) = locationDao.getLocations(sessionUUIDs)
        .map { it.map(RoomLocation::toDomainModel) }

    fun getAggressions(sessionUUID: String) = locationDao.getAggressions(sessionUUID)
        .map { it.map(RoomAggression::toDomainModel) }

    fun getAggressions(sessionUUIDs: List<String>) = locationDao.getAggressions(sessionUUIDs)
        .map { it.map(RoomAggression::toDomainModel) }

    /**
     * Save location's data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param location location data saved onto the Room database.
     *
     * @return id of the saved location.
     */
    fun saveLocation(location: DomainLocation) = locationDao.upsertLocation(location.toRoomModel())

    /**
     * Save locations' data to Room database.
     * Should be linked to a session to be accessible later on.
     *
     * @param locations list of location data saved onto the Room database.
     */
    fun saveLocations(locations: List<DomainLocation>) {
        Timber.i("Saving ${locations.size} locations")
        locationDao.upsertLocations(locations.map(DomainLocation::toRoomModel))
    }

    fun deleteLocationForSession(sessionUUID: String) = locationDao.deleteLocations(sessionUUID)

    fun deleteAggressionsForSession(sessionUUID: String) = locationDao.deleteAggressions(sessionUUID)

    fun saveAggressionsForSession(sessionUUID: String, aggressions: List<DomainAggression>) {
        Timber.i("Saving ${aggressions.size} aggressions for session $sessionUUID")
        locationDao.upsertAggressions(aggressions.map { it.toRoomModel() })
    }
}
