/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.main_nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentMainNavBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class MainNavFragment : RainbowCakeFragment<MainNavViewState, MainNavViewModel, FragmentMainNavBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainNavBinding = FragmentMainNavBinding::inflate

	override fun render(viewState: MainNavViewState) {
		when (viewState) {
			is Initial -> {

			}
			is Loading -> {

			}
			is Ready -> {

			}
		}.exhaustive
	}

	/**
	 * Not setting navController in MainActivity, because it will be overridden either way.
	 *
	 */
	override fun onStart() {
		super.onStart()
		binding.bottomNavigationMain.setupWithNavController(binding.mainNavHost.findNavController())
	}
}