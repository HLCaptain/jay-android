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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.jay.data.disk.model.RoomSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    fun insertSession(session: RoomSession): Long

    @Insert
    fun insertSessions(sessions: List<RoomSession>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertSession(session: RoomSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertSessions(sessions: List<RoomSession>)

    @Update
    fun updateSession(session: RoomSession): Int

    @Update
    fun updateSessions(sessions: List<RoomSession>): Int

    @Delete
    fun deleteSession(session: RoomSession)

    @Delete
    fun deleteSessions(sessions: List<RoomSession>)

    @Query("DELETE FROM session")
    fun deleteSessions()

    @Query("SELECT * FROM session")
    fun getSessions(): Flow<List<RoomSession>>

    @Query("SELECT id FROM session")
    fun getSessionIds(): Flow<List<Long>>

    @Query("SELECT * FROM session WHERE id = :id LIMIT 1")
    fun getSession(id: Long): Flow<RoomSession?>

    @Query("SELECT * FROM session WHERE endDateTime is NULL")
    fun getOngoingSessions(): Flow<List<RoomSession>>

    @Query("SELECT id FROM session WHERE endDateTime is NULL")
    fun getOngoingSessionIds(): Flow<List<Long>>

    @Query("SELECT id FROM session WHERE uuid is NULL")
    fun getLocalOnlySessionIds(): Flow<List<Long>>

    @Query("SELECT id FROM session WHERE uuid is NOT NULL")
    fun getSyncedSessionIds(): Flow<List<Long>>

    @Query("SELECT * FROM session WHERE uuid is NULL")
    fun getLocalOnlySessions(): Flow<List<RoomSession>>

    @Query("SELECT * FROM session WHERE uuid is NOT NULL")
    fun getSyncedSessions(): Flow<List<RoomSession>>

    @Query("UPDATE session SET uuid = NULL")
    fun removeUUIDsFromSessions()
}
