package illyan.jay.ui.main_nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.MainActivity
import illyan.jay.databinding.FragmentMainNavBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class MainNavFragment : RainbowCakeFragment<MainNavViewState, MainNavViewModel, FragmentMainNavBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainNavBinding = FragmentMainNavBinding::inflate

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationMain.setupWithNavController(
            binding.mainNavHost.findNavController()
        )
    }

    override fun render(viewState: MainNavViewState) {
        when(viewState) {
            is Initial -> {
                (requireActivity() as MainActivity).setNavController(binding.mainNavHost.findNavController())
            }
            is Loading -> {

            }
            is Ready -> {

            }
        }.exhaustive
    }
}