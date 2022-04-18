package illyan.jay.ui.sessions_nav

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionsNavViewModel @Inject constructor(

) : RainbowCakeViewModel<SessionsNavViewState>(Initial)