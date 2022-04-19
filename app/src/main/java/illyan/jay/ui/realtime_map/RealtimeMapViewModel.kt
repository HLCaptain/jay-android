package illyan.jay.ui.realtime_map

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RealtimeMapViewModel @Inject constructor(
	private val realtimeMapPresenter: RealtimeMapPresenter
) : RainbowCakeViewModel<RealtimeMapViewState>(Initial) {

	fun load() = executeNonBlocking {
		viewState = Loading
		realtimeMapPresenter.setLocationListener {
			viewState = Ready(it)
		}
	}

	fun unload() = executeNonBlocking {
		realtimeMapPresenter.stopListening()
	}
}