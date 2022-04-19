/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

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
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class HomeFragment : RainbowCakeFragment<HomeViewState, HomeViewModel, FragmentHomeBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding =
		FragmentHomeBinding::inflate

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
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

		viewModel.load()
	}

	override fun render(viewState: HomeViewState) {
		when (viewState) {
			is Initial -> {
				binding.homeText.text = "Initializing..."
			}
			is Loading -> {
				binding.homeText.text = "Loading..."
			}
			is Ready -> {
				binding.homeText.text = Firebase.remoteConfig["welcome_message"].asString()
			}
		}.exhaustive
	}
}