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