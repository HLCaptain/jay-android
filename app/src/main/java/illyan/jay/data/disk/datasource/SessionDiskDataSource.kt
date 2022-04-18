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
	fun getSession(id: Int) = sessionDao.getSession(id).map { it?.toDomainModel() }
	fun getOngoingSessions() =
		sessionDao.getOngoingSessions().map { it.map(RoomSession::toDomainModel) }

	fun getOngoingSessionIds() = sessionDao.getOngoingSessionIds()
	fun startSession(): Long {
		val id = sessionDao.insertSession(RoomSession(startTime = Instant.now().toEpochMilli()))
		Timber.d("Starting a session with ID = $id!")
		return id
	}


	fun saveSession(session: DomainSession) = sessionDao.insertSession(session.toRoomModel())

	fun stopSession(session: DomainSession): Long {
		session.endTime = Date.from(Instant.now())
		return saveSession(session)
	}
}