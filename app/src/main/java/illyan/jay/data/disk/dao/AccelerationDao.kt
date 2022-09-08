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

package illyan.jay.data.disk.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import illyan.jay.data.disk.model.RoomAcceleration
import kotlinx.coroutines.flow.Flow

@Dao
interface AccelerationDao {
    @Insert
    fun insertAcceleration(acceleration: RoomAcceleration): Long

    @Insert
    fun insertAccelerations(accelerations: List<RoomAcceleration>)

    @Upsert
    fun upsertAcceleration(acceleration: RoomAcceleration): Long

    @Upsert
    fun upsertAccelerations(accelerations: List<RoomAcceleration>)

    @Update
    fun updateAcceleration(acceleration: RoomAcceleration): Int

    @Update
    fun updateAccelerations(acceleration: List<RoomAcceleration>): Int

    @Delete
    fun deleteAcceleration(acceleration: RoomAcceleration)

    @Delete
    fun deleteAccelerations(accelerations: List<RoomAcceleration>)

    @Query("DELETE FROM acceleration")
    fun deleteAccelerations()

    @Query("DELETE FROM acceleration WHERE sessionId = :sessionId")
    fun deleteAccelerationsForSession(sessionId: Long)

    @Query("SELECT * FROM acceleration WHERE id = :id")
    fun getAcceleration(id: Long): Flow<RoomAcceleration?>

    @Query("SELECT * FROM acceleration WHERE sessionId = :sessionId")
    fun getAccelerations(sessionId: Long): Flow<List<RoomAcceleration>>
}
