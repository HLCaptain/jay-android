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
import illyan.jay.data.room.model.RoomSession
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

    @Query("DELETE FROM sessions WHERE ownerUUID IS :ownerUUID OR ownerUUID IS NULL")
    fun deleteSessions(ownerUUID: String? = null)

    @Query("DELETE FROM sessions WHERE ownerUUID IS NULL")
    fun deleteNotOwnedSessions()

    @Query("DELETE FROM sessions WHERE ownerUUID IS :ownerUUID")
    fun deleteSessionsByOwner(ownerUUID: String? = null)

    @Query("DELETE FROM sessions WHERE ownerUUID IS :ownerUUID AND endDateTime IS NOT NULL")
    fun deleteStoppedSessionsByOwner(ownerUUID: String? = null)

    @Query("SELECT * FROM sessions WHERE ownerUUID IS :ownerUUID OR ownerUUID IS NULL")
    fun getSessions(ownerUUID: String? = null): Flow<List<RoomSession>>

    @Query("SELECT * FROM sessions WHERE uuid IN (:sessionUUIDs)")
    fun getSessions(sessionUUIDs: List<String>): Flow<List<RoomSession>>

    @Query("SELECT uuid FROM sessions WHERE ownerUUID IS :ownerUUID OR ownerUUID IS NULL")
    fun getSessionUUIDs(ownerUUID: String? = null): Flow<List<String>>

    @Query("SELECT * FROM sessions WHERE uuid = :uuid AND (ownerUUID IS :ownerUUID OR ownerUUID IS NULL) LIMIT 1")
    fun getSession(uuid: String, ownerUUID: String? = null): Flow<RoomSession?>

    @Query("SELECT * FROM sessions WHERE endDateTime IS NOT NULL AND ownerUUID IS :ownerUUID")
    fun getStoppedSessions(ownerUUID: String? = null): Flow<List<RoomSession>>

    @Query("SELECT * FROM sessions WHERE endDateTime IS NULL AND (ownerUUID IS :ownerUUID OR ownerUUID IS NULL) ORDER BY startDateTime DESC")
    fun getOngoingSessions(ownerUUID: String? = null): Flow<List<RoomSession>>

    @Query("SELECT uuid FROM sessions WHERE endDateTime IS NULL AND (ownerUUID IS :ownerUUID OR ownerUUID IS NULL) ORDER BY startDateTime DESC")
    fun getOngoingSessionUUIDs(ownerUUID: String? = null): Flow<List<String>>

    @Query("SELECT * FROM sessions WHERE ownerUUID IS NULL ORDER BY startDateTime DESC")
    fun getAllNotOwnedSessions(): Flow<List<RoomSession>>

    @Query("SELECT * FROM sessions WHERE ownerUUID IS :ownerUUID ORDER BY startDateTime DESC")
    fun getSessionsByOwner(ownerUUID: String? = null): Flow<List<RoomSession>>

    @Query("UPDATE sessions SET ownerUUID = :ownerUUID WHERE ownerUUID IS NULL")
    fun ownAllNotOwnedSessions(ownerUUID: String): Int

    @Query("UPDATE sessions SET ownerUUID = :ownerUUID WHERE ownerUUID IS NULL AND uuid = :uuid")
    fun ownNotOwnedSession(uuid: String, ownerUUID: String): Int

    @Query("UPDATE sessions SET ownerUUID = :ownerUUID WHERE uuid IN(:uuids)")
    fun ownSessions(uuids: List<String>, ownerUUID: String)

    @Query("UPDATE sessions SET ownerUUID = NULL WHERE uuid IN(:uuids)")
    fun disownSessions(uuids: List<String>)

    @Query("UPDATE sessions SET ownerUUID = NULL WHERE ownerUUID IS :ownerUUID")
    fun disownSessions(ownerUUID: String)

    @Query("UPDATE sessions SET startLocationLatitude = :latitude, startLocationLongitude = :longitude WHERE uuid IS :sessionUUID")
    fun saveStartLocationForSession(sessionUUID: String, latitude: Double, longitude: Double)

    @Query("UPDATE sessions SET endLocationLatitude = :latitude, endLocationLongitude = :longitude WHERE uuid IS :sessionUUID")
    fun saveEndLocationForSession(sessionUUID: String, latitude: Double, longitude: Double)

    @Query("UPDATE sessions SET startLocationName = :name WHERE uuid IS :sessionUUID")
    fun saveStartLocationNameForSession(sessionUUID: String, name: String)

    @Query("UPDATE sessions SET endLocationName = :name WHERE uuid IS :sessionUUID")
    fun saveEndLocationNameForSession(sessionUUID: String, name: String)

    @Query("UPDATE sessions SET clientUUID = :clientUUID WHERE uuid IS :sessionUUID")
    fun assignClientToSession(sessionUUID: String, clientUUID: String?)

    @Query("UPDATE sessions SET distance = :distance WHERE uuid IS :sessionUUID")
    fun saveDistanceForSession(sessionUUID: String, distance: Float?)
}
