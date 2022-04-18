package illyan.jay.domain.interactor

import illyan.jay.data.disk.datasource.SessionDiskDataSource
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainSession
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionInteractor @Inject constructor(
    private val sessionDiskDataSource: SessionDiskDataSource
) {
    fun getSession(id: Int) = sessionDiskDataSource.getSession(id)
    fun getSessions() = sessionDiskDataSource.getSessions()
    fun getOngoingSessions() = sessionDiskDataSource.getOngoingSessions()
    fun getOngoingSessionIds() = sessionDiskDataSource.getOngoingSessionIds()
    fun saveSession(session: DomainSession) = sessionDiskDataSource.saveSession(session)
    fun startSession() = sessionDiskDataSource.startSession()
    fun stopSession(session: DomainSession) = sessionDiskDataSource.stopSession(session)
    suspend fun stopSession(id: Int) {
        sessionDiskDataSource.getSession(id).first {
            it?.let { session ->
                session.endTime = Date.from(Instant.now())
                sessionDiskDataSource.saveSession(session)
            }
            true
        }
    }
    suspend fun stopOngoingSessions() {
        getOngoingSessions().first {
            it.forEach { session ->
                stopSession(session)
            }
            it.isNotEmpty()
        }
    }
}