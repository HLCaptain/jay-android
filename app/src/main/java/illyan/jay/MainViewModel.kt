package illyan.jay

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainPresenter: MainPresenter
) : RainbowCakeViewModel<MainViewState>(MainStart) {

    fun load() = execute {
        viewState = MainReady(mainPresenter.isUserLoggedIn())
        mainPresenter.addAuthStateListener {
            viewState = MainReady(it)
        }
    }
}