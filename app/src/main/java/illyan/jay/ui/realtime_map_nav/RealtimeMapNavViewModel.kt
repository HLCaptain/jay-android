package illyan.jay.ui.realtime_map_nav

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RealtimeMapNavViewModel @Inject constructor(

) : RainbowCakeViewModel<RealtimeMapNavViewState>(Initial)