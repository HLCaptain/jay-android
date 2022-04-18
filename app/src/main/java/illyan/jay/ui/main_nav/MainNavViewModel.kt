package illyan.jay.ui.main_nav

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainNavViewModel @Inject constructor(

) : RainbowCakeViewModel<MainNavViewState>(Initial)