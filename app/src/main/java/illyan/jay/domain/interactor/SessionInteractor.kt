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

/**
 * Session interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property sessionDiskDataSource local database
 * @constructor Create empty Session interactor
 */
@Singleton
class SessionInteractor @Inject constructor(
	private val sessionDiskDataSource: SessionDiskDataSource
) {
	/**
	 * Get a particular session by its ID.
	 *
	 * @param id primary key of the session.
	 *
	 * @return a flow of the session if it exists in the database,
	 * otherwise a flow with null in it.
	 */
	fun getSession(id: Long) = sessionDiskDataSource.getSession(id)

	/**
	 * Get all session as a Flow.
	 *
	 * @return all session as a flow.
	 */
	fun getSessions() = sessionDiskDataSource.getSessions()

	/**
	 * Get ongoing sessions, which have no end date.
	 *
	 * @return a flow of ongoing sessions.
	 */
	fun getOngoingSessions() = sessionDiskDataSource.getOngoingSessions()

	/**
	 * Get ongoing sessions' IDs in a quicker way than getting all
	 * the information about the ongoing sessions. Cool behaviour based design :)
	 *
	 * @return a flow of ongoing sessions' IDs.
	 */
	fun getOngoingSessionIds() = sessionDiskDataSource.getOngoingSessionIds()

	/**
	 * Save session data.
	 *
	 * @param session updates the data of the session with the same ID.
	 *
	 * @return number of sessions updated.
	 */
	fun saveSession(session: DomainSession) = sessionDiskDataSource.saveSession(session)

	/**
	 * Save multiple sessions.
	 *
	 * @param sessions updates the data of the sessions with the same ID.
	 *
	 * @return number of sessions updated.
	 */
	fun saveSessions(sessions: List<DomainSession>) = sessionDiskDataSource.saveSessions(sessions)

	/**
	 * Creates a session with the current time as
	 * the starting date, end date as null and distance = 0.
	 *
	 * @return ID of the newly started session.
	 */
	fun startSession() = sessionDiskDataSource.startSession()

	/**
	 * Stop a session.
	 * Sets an end date for the session and saves it.
	 *
	 * @param session needed to be stopped.
	 *
	 * @return number of sessions stopped.
	 */
	fun stopSession(session: DomainSession) = sessionDiskDataSource.stopSession(session)

	/**
	 * Stop all ongoing sessions.
	 *
	 * @return number of sessions stopped.
	 */
	suspend fun stopOngoingSessions(): Int {
		var sessionsStopped = 0
		getOngoingSessions().first {
			sessionsStopped = sessionDiskDataSource.stopSessions(it)
			true
		}
		return sessionsStopped
	}
}