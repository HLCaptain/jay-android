package illyan.jay.ui.home_nav

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeNavViewModel @Inject constructor(

) : RainbowCakeViewModel<HomeNavViewState>(Initial)