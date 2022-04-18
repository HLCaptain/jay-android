package illyan.jay.ui.sessions_nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.MainActivity
import illyan.jay.databinding.FragmentSessionsNavBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class SessionsNavFragment : RainbowCakeFragment<SessionsNavViewState, SessionsNavViewModel, FragmentSessionsNavBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentSessionsNavBinding = FragmentSessionsNavBinding::inflate

    override fun render(viewState: SessionsNavViewState) {
        when(viewState) {
            is Initial -> {
                (requireActivity() as MainActivity).setNavController(binding.sessionsNavHost.findNavController())
            }
            is Loading -> {

            }
            is Ready -> {

            }
        }.exhaustive
    }
}