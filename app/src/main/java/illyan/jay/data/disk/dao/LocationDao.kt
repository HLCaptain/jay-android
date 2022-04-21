/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.disk.dao

import androidx.room.*
import illyan.jay.data.disk.model.RoomLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
	@Insert
	fun insertLocation(location: RoomLocation): Long

	@Insert
	fun insertLocations(locations: List<RoomLocation>)

	@Query("SELECT * FROM location")
	fun getLocations(): Flow<List<RoomLocation>>

	@Update
	fun updateLocation(location: RoomLocation): Int

	@Update
	fun updateLocations(location: List<RoomLocation>): Int

	@Delete
	fun deleteLocation(location: RoomLocation)

	@Query("SELECT * FROM location WHERE id = :id")
	fun getLocation(id: Long): RoomLocation?

	@Query("SELECT * FROM location WHERE sessionId = :sessionId")
	fun getLocations(sessionId: Long): Flow<List<RoomLocation>>

	@Delete
	fun deleteLocations(locations: List<RoomLocation>)

	@Query("DELETE FROM location")
	fun deleteLocations()

	@Query("DELETE FROM location WHERE sessionId = :sessionId")
	fun deleteLocations(sessionId: Long)

	@Query("SELECT * FROM location WHERE sessionId = :sessionId ORDER BY id DESC LIMIT :limit")
	fun getLatestLocations(sessionId: Long, limit: Long): Flow<List<RoomLocation>>
}