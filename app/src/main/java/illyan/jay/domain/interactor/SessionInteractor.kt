/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionInteractor @Inject constructor(
	private val sessionDiskDataSource: SessionDiskDataSource
) {
	fun getSession(id: Long) = sessionDiskDataSource.getSession(id)
	fun getSessions() = sessionDiskDataSource.getSessions()
	fun getOngoingSessions() = sessionDiskDataSource.getOngoingSessions()
	fun getOngoingSessionIds() = sessionDiskDataSource.getOngoingSessionIds()
	fun saveSession(session: DomainSession) = sessionDiskDataSource.saveSession(session)
	fun saveSessions(sessions: List<DomainSession>) = sessionDiskDataSource.saveSessions(sessions)
	fun startSession() = sessionDiskDataSource.startSession()
	fun stopSession(session: DomainSession) = sessionDiskDataSource.stopSession(session)
	suspend fun stopOngoingSessions() {
		getOngoingSessions().first {
			it.forEach { session -> stopSession(session) }
			it.isNotEmpty()
		}
	}
}