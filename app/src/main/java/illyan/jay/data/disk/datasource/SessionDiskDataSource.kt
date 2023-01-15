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
import java.time.ZonedDateTime
import java.util.UUID
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
    private val sessionDao: SessionDao,
) {
    /**
     * Get all session as a Flow.
     *
     * @return all session as a flow.
     */
    fun getSessions(ownerUserUUID: String?) =
        sessionDao.getSessions(ownerUserUUID).map { it.map(RoomSession::toDomainModel) }

    fun getSessionUUIDs(ownerUserUUID: String?) = sessionDao.getSessionUUIDs(ownerUserUUID)

    fun getAllNotOwnedSessions() = sessionDao.getAllNotOwnedSessions()
        .map { it.map(RoomSession::toDomainModel) }

    fun getSessionsByOwner(ownerUserUUID: String?) = sessionDao.getSessionsByOwner(ownerUserUUID)
        .map { it.map(RoomSession::toDomainModel) }

    fun ownAllNotOwnedSessions(ownerUserUUID: String) {
        Timber.d("$ownerUserUUID owns all sessions without an owner")
        sessionDao.ownAllNotOwnedSessions(ownerUserUUID)
    }


    fun ownNotOwnedSession(sessionUUID: String, ownerUserUUID: String) {
        Timber.d("$ownerUserUUID owns session with no owner yet: $sessionUUID")
        sessionDao.ownNotOwnedSession(sessionUUID, ownerUserUUID)
    }


    /**
     * Get a particular session by its ID.
     *
     * @param uuid primary key of the session.
     *
     * @return a flow of the session if it exists in the database,
     * otherwise a flow with null in it.
     */
    fun getSession(uuid: String, ownerUserUUID: String?) =
        sessionDao.getSession(uuid, ownerUserUUID).map { it?.toDomainModel() }

    fun getStoppedSessions(ownerUserUUID: String?) =
        sessionDao.getStoppedSessions(ownerUserUUID).map { it.map(RoomSession::toDomainModel) }

    /**
     * Get ongoing sessions, which have no end date.
     *
     * @return a flow of ongoing sessions.
     */
    fun getOngoingSessions(ownerUserUUID: String?) =
        sessionDao.getOngoingSessions(ownerUserUUID).map { it.map(RoomSession::toDomainModel) }

    /**
     * Get ongoing sessions' IDs in a quicker way than getting all
     * the information about the ongoing sessions.
     *
     * @return a flow of ongoing sessions' IDs.
     */
    fun getOngoingSessionIds(ownerUserUUID: String?) =
        sessionDao.getOngoingSessionUUIDs(ownerUserUUID)

    fun ownSessions(sessionUUIDs: List<String>, ownerUserUUID: String) {
        Timber.d("ownerUserUUID owns sessions: ${sessionUUIDs.map { it.take(4) }}")
        return sessionDao.ownSessions(sessionUUIDs, ownerUserUUID)
    }

    /**
     * Creates a session in the database with the current time as
     * the starting date, end date as null.
     *
     * @return ID of the newly started session.
     */
    fun startSession(
        ownerUserUUID: String? = null,
        clientUUID: String? = null,
    ): String {
        val uuid = UUID.randomUUID().toString()
        sessionDao.insertSession(
            RoomSession(
                uuid = uuid,
                startDateTime = Instant.now()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli(),
                endDateTime = null,
                ownerUserUUID = ownerUserUUID,
                clientUUID = clientUUID
            )
        )
        Timber.d("Starting a session with ID = $uuid!")
        return uuid
    }

    /**
     * Save session data in the database.
     *
     * @param session updates the data of the session with the same ID.
     *
     * @return id of session updated.
     */
    fun saveSession(session: DomainSession): Long {
        Timber.d("Upserting session: ${session.uuid}")
        return sessionDao.upsertSession(session.toRoomModel())
    }

    /**
     * Save multiple sessions in the database.
     *
     * @param sessions updates the data of the sessions with the same ID.
     */
    fun saveSessions(sessions: List<DomainSession>) {
        Timber.d("Upserting sessions: ${sessions.map { it.uuid.take(4) }}")
        sessionDao.upsertSessions(sessions.map(DomainSession::toRoomModel))
    }


    /**
     * Stop a session.
     * Sets an end date if not yet set for the session and saves it into the database.
     *
     * @param session setting its endTime to now,
     * then saving the modified value to the database.
     *
     * @return id of the stopped session.
     */
    fun stopSession(
        session: DomainSession,
        endTime: ZonedDateTime = Instant.now().atZone(ZoneId.systemDefault())
    ): Long {
        if (session.endDateTime == null) session.endDateTime = endTime
        return saveSession(session)
    }

    /**
     * Stop multiple sessions.
     * Sets an end date if not yet set for the session and saves it into the database.
     *
     * @param sessions setting sessions' endTime to now,
     * then saving the modified values to the database.
     */
    fun stopSessions(
        sessions: List<DomainSession>,
        endTime: ZonedDateTime = Instant.now().atZone(ZoneId.systemDefault())
    ) {
        sessions.forEach { if (it.endDateTime == null) it.endDateTime = endTime }
        saveSessions(sessions)
    }

    fun deleteSessions(sessions: List<DomainSession>) {
        Timber.d("Deleting sessions: ${sessions.map { it.uuid.take(4) }}")
        sessionDao.deleteSessions(sessions.map(DomainSession::toRoomModel))
    }

    fun deleteSessionsByOwner(ownerUserUUID: String?) {
        Timber.d("Deleting sessions by owner $ownerUserUUID")
        sessionDao.deleteSessionsByOwner(ownerUserUUID)
    }

    fun deleteNotOwnedSessions() {
        Timber.d("Deleting not owned sessions")
        sessionDao.deleteNotOwnedSessions()
    }

    fun deleteStoppedSessionsByOwner(ownerUserUUID: String?) {
        sessionDao.deleteStoppedSessionsByOwner(ownerUserUUID)
    }
}

