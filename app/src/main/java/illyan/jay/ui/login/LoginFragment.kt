package illyan.jay.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.viewModels
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import co.zsmb.rainbowcake.navigation.navigator
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentLoginBinding
import illyan.jay.ui.fragment.RainbowCakeFragmentVB
import illyan.jay.ui.home.HomeFragment

@AndroidEntryPoint
class LoginFragment : RainbowCakeFragmentVB<LoginViewState, LoginViewModel, FragmentLoginBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.load(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.googleSignInButton.setOnClickListener {
            viewModel.tryGoogleSignIn()
        }
    }

    override fun render(viewState: LoginViewState) {
        when(viewState) {
            is Loading -> {
                binding.loginStatus.text = "Loading"
            }
            is LoginReady -> {
                if (viewState.isLoggedIn) {
                    binding.loginStatus.text = "Logged in!"
                    navigator?.replace(HomeFragment())
                } else {
                    binding.loginStatus.text = "Not logged in!"
                }
            }
        }.exhaustive
    }
}