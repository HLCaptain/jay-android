/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.SessionDao
import illyan.jay.data.disk.model.RoomSession
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDiskDataSource @Inject constructor(
	private val sessionDao: SessionDao
) {
	fun getSessions() = sessionDao.getSessions().map { it.map(RoomSession::toDomainModel) }
	fun getSession(id: Long) = sessionDao.getSession(id).map { it?.toDomainModel() }
	fun getOngoingSessions() =
		sessionDao.getOngoingSessions().map { it.map(RoomSession::toDomainModel) }

	fun getOngoingSessionIds() = sessionDao.getOngoingSessionIds()
	fun startSession(): Long {
		val id = sessionDao.insertSession(RoomSession(startTime = Instant.now().toEpochMilli()))
		Timber.d("Starting a session with ID = $id!")
		return id
	}


	fun saveSession(session: DomainSession) = sessionDao.updateSession(session.toRoomModel())
	fun saveSessions(sessions: List<DomainSession>) =
		sessionDao.updateSession(sessions.map(DomainSession::toRoomModel))

	fun stopSession(session: DomainSession): Int {
		session.endTime = Date.from(Instant.now())
		return saveSession(session)
	}
}