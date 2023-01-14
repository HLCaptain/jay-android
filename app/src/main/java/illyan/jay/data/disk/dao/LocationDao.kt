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

package illyan.jay.data.disk.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import illyan.jay.data.disk.model.RoomLocation
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

    @Update
    fun updateLocation(location: RoomLocation): Int

    @Update
    fun updateLocations(location: List<RoomLocation>): Int

    @Delete
    fun deleteLocation(location: RoomLocation)

    @Delete
    fun deleteLocations(locations: List<RoomLocation>)

    @Query("DELETE FROM location")
    fun deleteLocations()

    @Query("DELETE FROM location WHERE sessionUUID = :sessionUUID")
    fun deleteLocations(sessionUUID: String)

    @Transaction
    @Query("SELECT * FROM location WHERE uuid = :uuid")
    fun getLocation(uuid: String): Flow<RoomLocation?>

    @Transaction
    @Query("SELECT * FROM location WHERE sessionUUID = :sessionUUID")
    fun getLocations(sessionUUID: String): Flow<List<RoomLocation>>

    @Transaction
    @Query("SELECT * FROM location WHERE sessionUUID IN(:sessionUUIDs)")
    fun getLocations(sessionUUIDs: List<String>): Flow<List<RoomLocation>>

    @Transaction
    @Query("SELECT * FROM location ORDER BY time DESC LIMIT :limit")
    fun getLatestLocations(limit: Long): Flow<List<RoomLocation>>

    @Transaction
    @Query("SELECT * FROM location WHERE sessionUUID = :sessionUUID ORDER BY time DESC LIMIT :limit")
    fun getLatestLocations(sessionUUID: String, limit: Long): Flow<List<RoomLocation>>
}
