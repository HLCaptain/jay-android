package illyan.jay.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentLoginBinding
import illyan.jay.ui.fragment.RainbowCakeFragmentVB
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : RainbowCakeFragmentVB<LoginViewState, LoginViewModel, FragmentLoginBinding>() {
    override fun provideViewModel() = getViewModelFromFactory()
    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    private val googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val task: Task<GoogleSignInAccount> =
            GoogleSignIn.getSignedInAccountFromIntent(it.data)
        viewModel.handleGoogleSignInResult(requireActivity(), task)
    }
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
            googleSignInClient = GoogleSignIn.getClient(
                requireActivity(),
                GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Firebase.remoteConfig["default_web_client_id"].asString())
                    .requestEmail()
                    .build()
            )
        }
        viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.googleSignInButton.setOnClickListener {
            viewModel.onTryLogin()
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Called onStart!")

        viewModel.refresh()
    }

    override fun render(viewState: LoginViewState) {
        when(viewState) {
            is Loading -> {
                binding.loginStatus.text = "Loading"
            }
            is LoginReady -> {
                val nav = findNavController()
                if (viewState.isLoggedIn) {
                    binding.loginStatus.text = "Logged in!"
                    val action = LoginFragmentDirections.actionLoginFragmentToNavGraphMain()
                    nav.popBackStack(nav.graph.startDestinationId, false)
                    nav.navigate(action)
                } else {
                    binding.loginStatus.text = "Logged out!"
                }
            }
            is LoggingIn -> {
                binding.loginStatus.text = "Logging in!"
            }
        }.exhaustive
    }
}