package illyan.jay.ui.sessions.session_info

import co.zsmb.rainbowcake.withIOContext
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainSession
import illyan.jay.ui.sessions.session_info.model.UiSession
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionInfoPresenter @Inject constructor(
    private val sessionInteractor: SessionInteractor
) {
    suspend fun getSession(id: Long) = withIOContext {
	    return@withIOContext sessionInteractor.getSession(id).map { it?.toUiModel() }
    }
}

private fun DomainSession.toUiModel() = UiSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
)