package illyan.jay.ui.toggle.service

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServiceToggleViewModel @Inject constructor(
    private val serviceTogglePresenter: ServiceTogglePresenter
) : RainbowCakeViewModel<ServiceToggleViewState>(Initial) {

    fun load() = executeNonBlocking {
        viewState = Loading
        viewState = if (serviceTogglePresenter.isJayServiceRunning()) On else Off
        serviceTogglePresenter.addJayServiceStateListener { isRunning, name ->
            viewState = if (isRunning) On else Off
        }
    }

    fun toggleService() = executeNonBlocking {
        viewState = Loading
        if (serviceTogglePresenter.isJayServiceRunning()) {
            serviceTogglePresenter.stopService()
        } else {
            serviceTogglePresenter.startService()
        }
    }
}