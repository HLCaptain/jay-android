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

package illyan.jay.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.jay.data.room.model.RoomSensorEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorEventDao {
    @Insert
    fun insertSensorEvent(acceleration: RoomSensorEvent): Long

    @Insert
    fun insertSensorEvents(accelerations: List<RoomSensorEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertSensorEvent(acceleration: RoomSensorEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertSensorEvents(accelerations: List<RoomSensorEvent>)

    @Update
    fun updateSensorEvent(acceleration: RoomSensorEvent): Int

    @Update
    fun updateSensorEvents(acceleration: List<RoomSensorEvent>): Int

    @Delete
    fun deleteSensorEvent(acceleration: RoomSensorEvent)

    @Delete
    fun deleteSensorEvents(accelerations: List<RoomSensorEvent>)

    @Query("DELETE FROM sensor_events")
    fun deleteSensorEvents()

    @Query("DELETE FROM sensor_events WHERE sessionUUID = :sessionUUID")
    fun deleteSensorEventsForSession(sessionUUID: String)

    @Query("SELECT * FROM sensor_events WHERE sessionUUID = :sessionUUID")
    fun getSensorEvents(sessionUUID: String): Flow<List<RoomSensorEvent>>

    @Query("SELECT * FROM sensor_events WHERE sessionUUID IN(:sessionUUIDs)")
    fun getSensorEvents(sessionUUIDs: List<String>): Flow<List<RoomSensorEvent>>
}
