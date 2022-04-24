/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.realtime_map_nav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.MainActivity
import illyan.jay.databinding.FragmentRealtimeMapNavBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class RealtimeMapNavFragment : RainbowCakeFragment<RealtimeMapNavViewState, RealtimeMapNavViewModel, FragmentRealtimeMapNavBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentRealtimeMapNavBinding = FragmentRealtimeMapNavBinding::inflate

	override fun render(viewState: RealtimeMapNavViewState) {
		// Not listening to any viewState changes right now.
	}

	/**
	 * Bind navController here for the smoothest transition!
	 */
	override fun onStart() {
		super.onStart()
		(requireActivity() as MainActivity).addNavController(binding.realtimeMapNavHost.findNavController())
	}

	override fun onStop() {
		(requireActivity() as MainActivity).popNavController()
		super.onStop()
	}
}