package illyan.jay.ui.sessions.map

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionMapViewModel @Inject constructor(
	private val sessionMapPresenter: SessionMapPresenter
) : RainbowCakeViewModel<SessionMapViewState>(Initial) {

	fun loadPath(sessionId: Int) = executeNonBlocking {
		viewState = Loading
		var firstLoaded = true

		sessionMapPresenter.getLocations(sessionId).collect {
			viewState = Ready(it, firstLoaded)
			firstLoaded = false
		}
	}
}