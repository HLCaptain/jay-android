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

package illyan.jay.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.jay.data.room.model.RoomAggression
import illyan.jay.data.room.model.RoomLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    fun insertLocation(location: RoomLocation): Long

    @Insert
    fun insertLocations(locations: List<RoomLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertLocation(location: RoomLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertLocations(locations: List<RoomLocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAggression(aggression: RoomAggression): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAggressions(aggressions: List<RoomAggression>)

    @Update
    fun updateLocation(location: RoomLocation): Int

    @Update
    fun updateLocations(location: List<RoomLocation>): Int

    @Delete
    fun deleteLocation(location: RoomLocation)

    @Delete
    fun deleteLocations(locations: List<RoomLocation>)

    @Query("DELETE FROM locations")
    fun deleteLocations()

    @Query("DELETE FROM locations WHERE sessionUUID = :sessionUUID")
    fun deleteLocations(sessionUUID: String)

    @Query("DELETE FROM aggressions")
    fun deleteAggressions()

    @Query("DELETE FROM aggressions WHERE sessionUUID = :sessionUUID")
    fun deleteAggressions(sessionUUID: String)

    @Query("SELECT * FROM locations WHERE sessionUUID = :sessionUUID")
    fun getLocations(sessionUUID: String): Flow<List<RoomLocation>>

    @Query("SELECT * FROM locations WHERE sessionUUID IN(:sessionUUIDs)")
    fun getLocations(sessionUUIDs: List<String>): Flow<List<RoomLocation>>

    @Query("SELECT * FROM aggressions WHERE sessionUUID = :sessionUUID")
    fun getAggressions(sessionUUID: String): Flow<List<RoomAggression>>

    @Query("SELECT * FROM aggressions WHERE sessionUUID IN(:sessionUUIDs)")
    fun getAggressions(sessionUUIDs: List<String>): Flow<List<RoomAggression>>

    @Query("SELECT * FROM locations ORDER BY time DESC LIMIT :limit")
    fun getLatestLocations(limit: Long): Flow<List<RoomLocation>>

    @Query("SELECT * FROM locations WHERE sessionUUID = :sessionUUID ORDER BY time DESC LIMIT :limit")
    fun getLatestLocations(sessionUUID: String, limit: Long): Flow<List<RoomLocation>>
}
