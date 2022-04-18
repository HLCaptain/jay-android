package illyan.jay.ui.sessions.session_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentSessionInfoBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class SessionInfoFragment : RainbowCakeFragment<SessionInfoViewState, SessionInfoViewModel, FragmentSessionInfoBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentSessionInfoBinding = FragmentSessionInfoBinding::inflate
    private val args: SessionInfoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadSession(args.sessionId)
    }

    override fun render(viewState: SessionInfoViewState) {
        when(viewState) {
            is Initial -> {

            }
            is Loading -> {

            }
            is Ready -> {

            }
            is NotFound -> {

            }
        }.exhaustive
    }
}