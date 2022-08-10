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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.findNavController
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

	private val navControllerDelegateStack = ArrayDeque<NavController>()

	override fun render(viewState: MainViewState) {
		when (viewState) {
			is Initial -> {
				// show spalsh screen?
			}
			is LoggedIn -> {
				Timber.d("Logged in!")
			}
			is LoggedOut -> {
				Timber.d("Logged out!")
				stopService(Intent(this, JayService::class.java))
				// Resetting to the Login screen if needed.
				val nav = binding.loginNavHostFragment.findNavController()
				nav.popBackStack(nav.graph.startDestinationId, false)
				supportActionBar?.title = nav.graph.findStartDestination().label
			}
		}.exhaustive
	}

	override fun onStart() {
		super.onStart()
		// NavController is loaded in onStart, that is why we load the viewModel now.
		// It may drop an exception for not finding the navController we need,
		// if we call this load method earlier.
		viewModel.load()
	}

	override fun onBackPressed() {
		if (navControllerDelegateStack.lastOrNull()?.currentDestination?.id == navControllerDelegateStack.lastOrNull()?.graph?.startDestinationId) {
			finish()
		} else {
			super.onBackPressed()
		}
	}

	override fun onNavigateUp(): Boolean {
		val nav = binding.loginNavHostFragment.findNavController()
		if (navControllerDelegateStack.last().currentDestination?.id != navControllerDelegateStack.last().graph.startDestinationId) {
			return navControllerDelegateStack.last().navigateUp()
		} else {
			if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
				finish()
			}
		}
		return super.onNavigateUp()
	}

	override fun onSupportNavigateUp(): Boolean {
		val nav = binding.loginNavHostFragment.findNavController()
		if (navControllerDelegateStack.last().currentDestination?.id != navControllerDelegateStack.last().graph.startDestinationId) {
			return navControllerDelegateStack.last().navigateUp()
		} else {
			if (nav.previousBackStackEntry?.destination?.id == nav.graph.startDestinationId) {
				finish()
			}
		}
		return super.onSupportNavigateUp()
	}

	/**
	 * Add navigation controller to the delegate stack.
	 *
	 * @param navController navController becomes the active controller in the navigation graph
	 * and will be used to determine if the app should finish or navigate back further.
	 *
	 * @param doSetActionBar sets action bar with new navController if true.
	 */
	fun addNavController(navController: NavController, doSetActionBar: Boolean = true) {
		navControllerDelegateStack.addLast(navController)
		if (doSetActionBar) {
			setupActionBarWithNavController(navControllerDelegateStack.last())
		}
	}

	/**
	 * Pop a navController on the delegate stack.
	 * The navController before the popped one becomes the active controller.
	 *
	 * @param doSetActionBar sets up the action bar with the last navController
	 * after the pop if true and the stack is not empty.
	 */
	fun popNavController(doSetActionBar: Boolean = false): NavController {
		val removedLast = navControllerDelegateStack.removeLast()
		if (navControllerDelegateStack.isNotEmpty() && doSetActionBar) {
			setupActionBarWithNavController(navControllerDelegateStack.last())
		}
		return removedLast
	}
}