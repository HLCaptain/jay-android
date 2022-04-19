/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.LocationDao
import illyan.jay.data.disk.model.RoomLocation
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDiskDataSource @Inject constructor(
	private val locationDao: LocationDao
) {
	fun getLocations(sessionId: Long, limit: Long) = locationDao.getLocations(sessionId, limit)
		.map { it.map(RoomLocation::toDomainModel) }

	fun getLocations(sessionId: Long) = locationDao.getLocations(sessionId)
		.map { it.map(RoomLocation::toDomainModel) }

	fun saveLocation(location: DomainLocation) = locationDao.insertLocation(location.toRoomModel())
}