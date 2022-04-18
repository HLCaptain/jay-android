package illyan.jay.ui.home_nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.MainActivity
import illyan.jay.databinding.FragmentHomeNavBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class HomeNavFragment :
	RainbowCakeFragment<HomeNavViewState, HomeNavViewModel, FragmentHomeNavBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeNavBinding =
		FragmentHomeNavBinding::inflate

	override fun render(viewState: HomeNavViewState) {
		when (viewState) {
			is Initial -> {
				(requireActivity() as MainActivity).setNavController(binding.homeNavHost.findNavController())
			}
			is Loading -> {

			}
			is Ready -> {

			}
		}.exhaustive
	}
}