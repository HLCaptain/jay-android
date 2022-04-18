package illyan.jay.ui.sessions.list

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import co.zsmb.rainbowcake.withIOContext
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionsPresenter: SessionsPresenter
) : RainbowCakeViewModel<SessionsViewState>(Initial) {

    fun load() = executeNonBlocking {
        viewState = Loading
        sessionsPresenter.getSessions().collect { sessions ->
            viewState = Ready(sessions)
        }
    }
}