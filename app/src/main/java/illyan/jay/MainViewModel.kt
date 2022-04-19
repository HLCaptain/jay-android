package illyan.jay

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainPresenter: MainPresenter
) : RainbowCakeViewModel<MainViewState>(Initial) {

    fun load() = executeNonBlocking {
        viewState = if (mainPresenter.isUserLoggedIn()) LoggedIn else LoggedOut
        mainPresenter.addAuthStateListener {
            viewState = if (it) LoggedIn else LoggedOut
        }
    }
}