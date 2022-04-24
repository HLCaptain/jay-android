/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

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
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class LoginFragment : RainbowCakeFragment<LoginViewState, LoginViewModel, FragmentLoginBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding =
		FragmentLoginBinding::inflate

	/**
	 * Used to sign in via Google.
	 */
	private val googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
		viewModel.handleGoogleSignInResult(requireActivity(), task)
	}
	lateinit var googleSignInClient: GoogleSignInClient

	override fun render(viewState: LoginViewState) {
		val nav = findNavController()
		when (viewState) {
			is Initial -> {
				binding.loginStatus.text = "Initializing"
			}
			is Loading -> {
				binding.loginStatus.text = "Loading"
			}
			is LoggingIn -> {
				// Show a loading indicator in the background.
				// Block any buttons which can interfere with the state.
				binding.loginStatus.text = "Logging in!"
			}
			is LoggedIn -> {
				binding.loginStatus.text = "Logged in!"
				val action = LoginFragmentDirections.actionLoginFragmentToMainNavFragment()
				nav.popBackStack(nav.graph.startDestinationId, false)
				nav.navigate(action)
			}
			is LoggedOut -> {
				binding.loginStatus.text = "Logged out!"
			}
		}.exhaustive
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// Fetching and activating changes from the firebase cloud.
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
		viewModel.refresh()
	}
}