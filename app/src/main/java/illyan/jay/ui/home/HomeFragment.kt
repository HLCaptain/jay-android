package illyan.jay.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentHomeBinding
import illyan.jay.ui.fragment.RainbowCakeFragmentVB

@AndroidEntryPoint
class HomeFragment : RainbowCakeFragmentVB<HomeViewState, HomeViewModel, FragmentHomeBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()

        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            GoogleSignIn.getClient(
                requireActivity(),
                GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Firebase.remoteConfig["default_web_client_id"].asString())
                    .requestEmail()
                    .build()
            ).signOut()
        }
    }

    override fun render(viewState: HomeViewState) {
        when(viewState) {
            is Loading -> {
                binding.homeText.text = "Loading..."
            }
            is HomeReady -> {
                binding.homeText.text = Firebase.remoteConfig["welcome_message"].asString()
            }
        }.exhaustive
    }
}