package illyan.jay.ui.realtime_map_nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.MainActivity
import illyan.jay.databinding.FragmentRealtimeMapNavBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class RealtimeMapNavFragment : RainbowCakeFragment<RealtimeMapNavViewState, RealtimeMapNavViewModel, FragmentRealtimeMapNavBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentRealtimeMapNavBinding = FragmentRealtimeMapNavBinding::inflate
	override fun render(viewState: RealtimeMapNavViewState) {
		when(viewState) {
			is Initial -> {
				(requireActivity() as MainActivity).setNavController(binding.realtimeMapNavHost.findNavController())
			}
			is Loading -> {

			}
			is Ready -> {

			}
		}.exhaustive
	}
}