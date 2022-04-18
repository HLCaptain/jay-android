package illyan.jay.ui.toggle.service

import android.content.Context
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServiceToggleViewModel @Inject constructor(
    private val serviceTogglePresenter: ServiceTogglePresenter
) : RainbowCakeViewModel<ServiceToggleViewState>(Initial) {

    fun load() = execute {
        viewState = Loading
        viewState = if (serviceTogglePresenter.isJayServiceRunning()) On else Off
        serviceTogglePresenter.addJayServiceStateListener { isRunning, name ->
            viewState = if (isRunning) On else Off
        }
    }

    fun toggleService() = execute {
        viewState = Loading
        if (serviceTogglePresenter.isJayServiceRunning()) {
            serviceTogglePresenter.stopService()
        } else {
            serviceTogglePresenter.startService()
        }
    }
}