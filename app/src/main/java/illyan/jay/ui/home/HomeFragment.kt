package illyan.jay.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentHomeBinding
import illyan.jay.databinding.FragmentLoginBinding
import illyan.jay.ui.fragment.RainbowCakeFragmentVB
import illyan.jay.ui.login.LoginReady

@AndroidEntryPoint
class HomeFragment : RainbowCakeFragmentVB<HomeViewState, HomeViewModel, FragmentHomeBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun render(viewState: HomeViewState) {
        when(viewState) {
            is Loading -> {
                binding.homeText.text = "Loading.."
            }
            is HomeReady -> {
                binding.homeText.text = "Welcome!"
            }
        }.exhaustive
    }
}