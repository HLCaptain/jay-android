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

package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.SessionDao
import illyan.jay.data.disk.model.RoomSession
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session disk data source using Room to communicate with the SQLite database.
 *
 * @property sessionDao used to insert, update, delete and query commands using Room.
 * @constructor Create empty Session disk data source
 */
@Singleton
class SessionDiskDataSource @Inject constructor(
    private val sessionDao: SessionDao
) {
    /**
     * Get all session as a Flow.
     *
     * @return all session as a flow.
     */
    fun getSessions() = sessionDao.getSessions().map { it.map(RoomSession::toDomainModel) }

    fun getLocalOnlySessions() = sessionDao.getLocalOnlySessions()
        .map { it.map(RoomSession::toDomainModel) }

    fun getSyncedSessions() = sessionDao.getSyncedSessions()
        .map { it.map(RoomSession::toDomainModel) }

    fun getSessionIds() = sessionDao.getSessionIds()

    fun getLocalOnlySessionIds() = sessionDao.getLocalOnlySessionIds()

    fun getSyncedSessionIds() = sessionDao.getSyncedSessionIds()

    /**
     * Get a particular session by its ID.
     *
     * @param id primary key of the session.
     *
     * @return a flow of the session if it exists in the database,
     * otherwise a flow with null in it.
     */
    fun getSession(id: Long) = sessionDao.getSession(id).map { it?.toDomainModel() }

    /**
     * Get ongoing sessions, which have no end date.
     *
     * @return a flow of ongoing sessions.
     */
    fun getOngoingSessions() =
        sessionDao.getOngoingSessions().map { it.map(RoomSession::toDomainModel) }

    /**
     * Get ongoing sessions' IDs in a quicker way than getting all
     * the information about the ongoing sessions.
     *
     * @return a flow of ongoing sessions' IDs.
     */
    fun getOngoingSessionIds() = sessionDao.getOngoingSessionIds()

    /**
     * Creates a session in the database with the current time as
     * the starting date, end date as null.
     *
     * @return ID of the newly started session.
     */
    fun startSession(): Long {
        val id = sessionDao.insertSession(
            RoomSession(
                id = 0,
                startDateTime = Instant.now()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli(),
                endDateTime = null
            )
        )
        Timber.d("Starting a session with ID = $id!")
        return id
    }

    /**
     * Save session data in the database.
     *
     * @param session updates the data of the session with the same ID.
     *
     * @return id of session updated.
     */
    fun saveSession(session: DomainSession) = sessionDao.upsertSession(session.toRoomModel())

    /**
     * Save multiple sessions in the database.
     *
     * @param sessions updates the data of the sessions with the same ID.
     */
    fun saveSessions(sessions: List<DomainSession>) =
        sessionDao.upsertSessions(sessions.map(DomainSession::toRoomModel))

    /**
     * Stop a session.
     * Sets an end date if not yet set for the session and saves it into the database.
     *
     * @param session setting its endTime to now,
     * then saving the modified value to the database.
     *
     * @return id of the stopped session.
     */
    fun stopSession(session: DomainSession): Long {
        if (session.endDateTime == null) session.endDateTime = Instant.now().atZone(ZoneId.systemDefault())
        return saveSession(session)
    }

    /**
     * Stop multiple sessions.
     * Sets an end date if not yet set for the session and saves it into the database.
     *
     * @param sessions setting sessions' endTime to now,
     * then saving the modified values to the database.
     */
    fun stopSessions(sessions: List<DomainSession>) {
        val endTime = Instant.now().atZone(ZoneId.systemDefault())
        sessions.forEach { if (it.endDateTime == null) it.endDateTime = endTime }
        saveSessions(sessions)
    }

    fun deleteSessions(sessions: List<DomainSession>) =
        sessionDao.deleteSessions(sessions.map(DomainSession::toRoomModel))

    fun removeUUIDsFromSessions() = sessionDao.removeUUIDsFromSessions()
}
