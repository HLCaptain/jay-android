/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.ActivityMainBinding
import illyan.jay.service.JayService
import illyan.jay.ui.custom.RainbowCakeActivity
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : RainbowCakeActivity<MainViewState, MainViewModel, ActivityMainBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBinding() = ActivityMainBinding.inflate(layoutInflater)

	private lateinit var navControllerDelegate: NavController

	override fun render(viewState: MainViewState) {
		when (viewState) {
			is Initial -> {
				// show spalsh screen?
			}
			is LoggedIn -> {
				Timber.d("Logged in!")
			}
			is LoggedOut -> {
				val nav = binding.loginNavHostFragment.findNavController()
				Timber.d("Logged out!")
				stopService(Intent(this, JayService::class.java))
				if (nav.currentDestination?.id != nav.graph.startDestinationId) {
					nav.popBackStack(nav.graph.startDestinationId, false)
				}
				Unit
			}
		}.exhaustive
	}

	override fun onResume() {
		super.onResume()
		viewModel.load()
	}

	override fun onBackPressed() {
		Timber.d("onBackPressed called")
		Timber.d("navControllerDelegate.graph = ${navControllerDelegate.graph.displayName}")
		if (navControllerDelegate.currentDestination?.id == navControllerDelegate.graph.startDestinationId) {
			finish()
		} else {
			super.onBackPressed()
		}
	}

	override fun onNavigateUp(): Boolean {
		Timber.d("onNavigateUp called")
		val nav = binding.loginNavHostFragment.findNavController()
		if (navControllerDelegate.currentDestination?.id != navControllerDelegate.graph.startDestinationId) {
			return navControllerDelegate.navigateUp()
		} else {
			if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
				finish()
			}
		}
		return super.onNavigateUp()
	}

	override fun onSupportNavigateUp(): Boolean {
		Timber.d("onSupportNavigateUp called")
		val nav = binding.loginNavHostFragment.findNavController()
		if (navControllerDelegate.currentDestination?.id != navControllerDelegate.graph.startDestinationId) {
			return navControllerDelegate.navigateUp()
		} else {
			if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
				finish()
			}
		}
		return super.onSupportNavigateUp()
	}

	fun setNavController(navController: NavController) {
		navControllerDelegate = navController
		setupActionBarWithNavController(
			navControllerDelegate,
			AppBarConfiguration(navControllerDelegate.graph)
		)
	}
}