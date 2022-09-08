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
import illyan.jay.data.disk.model.RoomRotation
import kotlinx.coroutines.flow.Flow

@Dao
interface RotationDao {
    @Insert
    fun insertRotation(rotation: RoomRotation): Long

    @Insert
    fun insertRotations(rotations: List<RoomRotation>)

    @Upsert
    fun upsertRotation(rotation: RoomRotation): Long

    @Upsert
    fun upsertRotations(rotations: List<RoomRotation>)

    @Update
    fun updateRotation(rotation: RoomRotation): Int

    @Update
    fun updateRotations(rotation: List<RoomRotation>): Int

    @Delete
    fun deleteRotation(rotation: RoomRotation)

    @Delete
    fun deleteRotations(rotations: List<RoomRotation>)

    @Query("DELETE FROM rotation")
    fun deleteRotations()

    @Query("DELETE FROM rotation WHERE sessionId = :sessionId")
    fun deleteRotationsForSession(sessionId: Long)

    @Query("SELECT * FROM rotation WHERE id = :id")
    fun getRotation(id: Long): Flow<RoomRotation?>

    @Query("SELECT * FROM rotation WHERE sessionId = :sessionId")
    fun getRotations(sessionId: Long): Flow<List<RoomRotation>>
}
