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
import illyan.jay.data.disk.model.RoomSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    fun insertSession(session: RoomSession)

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

    @Transaction
    @Query("DELETE FROM session WHERE ownerUserUUID IS :ownerUserUUID OR ownerUserUUID IS NULL")
    fun deleteSessions(ownerUserUUID: String? = null)

    @Transaction
    @Query("DELETE FROM session WHERE ownerUserUUID IS NULL")
    fun deleteNotOwnedSessions()

    @Transaction
    @Query("DELETE FROM session WHERE ownerUserUUID IS :ownerUserUUID")
    fun deleteSessionsByOwner(ownerUserUUID: String? = null)

    @Transaction
    @Query("DELETE FROM session WHERE ownerUserUUID IS :ownerUserUUID AND endDateTime IS NOT NULL")
    fun deleteStoppedSessionsByOwner(ownerUserUUID: String? = null)

    @Transaction
    @Query("SELECT * FROM session WHERE ownerUserUUID IS :ownerUserUUID OR ownerUserUUID IS NULL")
    fun getSessions(ownerUserUUID: String? = null): Flow<List<RoomSession>>

    @Transaction
    @Query("SELECT uuid FROM session WHERE ownerUserUUID IS :ownerUserUUID OR ownerUserUUID IS NULL")
    fun getSessionUUIDs(ownerUserUUID: String? = null): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM session WHERE uuid = :uuid AND (ownerUserUUID IS :ownerUserUUID OR ownerUserUUID IS NULL) LIMIT 1")
    fun getSession(uuid: String, ownerUserUUID: String? = null): Flow<RoomSession?>

    @Transaction
    @Query("SELECT * FROM session WHERE endDateTime IS NOT NULL AND ownerUserUUID IS :ownerUserUUID")
    fun getStoppedSessions(ownerUserUUID: String? = null): Flow<List<RoomSession>>

    @Transaction
    @Query("SELECT * FROM session WHERE endDateTime IS NULL AND (ownerUserUUID IS :ownerUserUUID OR ownerUserUUID IS NULL) ORDER BY startDateTime DESC")
    fun getOngoingSessions(ownerUserUUID: String? = null): Flow<List<RoomSession>>

    @Transaction
    @Query("SELECT uuid FROM session WHERE endDateTime IS NULL AND (ownerUserUUID IS :ownerUserUUID OR ownerUserUUID IS NULL) ORDER BY startDateTime DESC")
    fun getOngoingSessionUUIDs(ownerUserUUID: String? = null): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM session WHERE ownerUserUUID IS NULL ORDER BY startDateTime DESC")
    fun getAllNotOwnedSessions(): Flow<List<RoomSession>>

    @Transaction
    @Query("SELECT * FROM session WHERE ownerUserUUID IS :ownerUserUUID ORDER BY startDateTime DESC")
    fun getSessionsByOwner(ownerUserUUID: String? = null): Flow<List<RoomSession>>

    @Transaction
    @Query("UPDATE session SET ownerUserUUID = :ownerUserUUID WHERE ownerUserUUID IS NULL")
    fun ownAllNotOwnedSessions(ownerUserUUID: String): Int

    @Transaction
    @Query("UPDATE session SET ownerUserUUID = :ownerUserUUID WHERE ownerUserUUID IS NULL AND uuid = :uuid")
    fun ownNotOwnedSession(uuid: String, ownerUserUUID: String): Int

    @Transaction
    @Query("UPDATE session SET ownerUserUUID = :ownerUserUUID WHERE uuid IN(:uuids)")
    fun ownSessions(uuids: List<String>, ownerUserUUID: String)

    @Transaction
    @Query("UPDATE session SET ownerUserUUID = NULL WHERE uuid IN(:uuids)")
    fun disownSessions(uuids: List<String>)

    @Transaction
    @Query("UPDATE session SET ownerUserUUID = NULL WHERE ownerUserUUID IS :ownerUserUUID")
    fun disownSessions(ownerUserUUID: String)
}
