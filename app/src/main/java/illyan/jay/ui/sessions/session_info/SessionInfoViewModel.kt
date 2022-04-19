package illyan.jay.ui.sessions.session_info

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionInfoViewModel @Inject constructor(
    private val sessionInfoPresenter: SessionInfoPresenter
) : RainbowCakeViewModel<SessionInfoViewState>(Initial) {

    fun loadSession(id: Long) = executeNonBlocking {
        viewState = Loading
        sessionInfoPresenter.getSession(id).collect { session ->
            session?.let {
                viewState = Ready(it)
                return@collect
            }
            viewState = NotFound
        }
    }
}