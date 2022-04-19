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
import illyan.jay.data.disk.model.RoomSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertSession(session: RoomSession): Long

	@Query("SELECT * FROM session")
	fun getSessions(): Flow<List<RoomSession>>

	@Query("SELECT id FROM session")
	fun getSessionIds(): Flow<List<Long>>

	@Update
	fun updateSession(session: RoomSession): Int

	@Update
	fun updateSession(sessions: List<RoomSession>): Int

	@Delete
	fun deleteSession(session: RoomSession)

	@Query("SELECT * FROM session WHERE id = :id LIMIT 1")
	fun getSession(id: Long): Flow<RoomSession?>

	@Query("SELECT * FROM session WHERE endTime is NULL")
	fun getOngoingSessions(): Flow<List<RoomSession>>

	@Query("SELECT id FROM session WHERE endTime is NULL")
	fun getOngoingSessionIds(): Flow<List<Long>>

	@Delete
	fun deleteSessions(sessions: List<RoomSession>)

	@Query("DELETE FROM session")
	fun deleteSessions()
}