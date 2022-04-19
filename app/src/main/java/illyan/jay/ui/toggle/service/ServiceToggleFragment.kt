/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.toggle.service

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentServiceToggleBinding
import illyan.jay.ui.custom.RainbowCakeFragment
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@AndroidEntryPoint
@RuntimePermissions
class ServiceToggleFragment : RainbowCakeFragment<ServiceToggleViewState, ServiceToggleViewModel, FragmentServiceToggleBinding>() {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentServiceToggleBinding = FragmentServiceToggleBinding::inflate

	companion object {
		const val INITIAL = 0
		const val LOADING = 0
		const val ON = 1
		const val OFF = 2
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.load()
		binding.serviceLoadingCard.setOnClickListener {
			toggleServiceWithPermissionCheck()
		}
		binding.serviceOnCard.setOnClickListener {
			toggleServiceWithPermissionCheck()
		}
		binding.serviceOffCard.setOnClickListener {
			toggleServiceWithPermissionCheck()
		}
	}

	override fun render(viewState: ServiceToggleViewState) {
		when(viewState) {
			is Initial -> {
				binding.serviceToggleCardFlipper.displayedChild = INITIAL
			}
			is Loading -> {
				binding.serviceToggleCardFlipper.displayedChild = LOADING
			}
			is On -> {
				binding.serviceToggleCardFlipper.displayedChild = ON
			}
			is Off -> {
				binding.serviceToggleCardFlipper.displayedChild = OFF
			}
		}.exhaustive
	}

	@NeedsPermission(
		Manifest.permission.FOREGROUND_SERVICE,
		Manifest.permission.ACCESS_FINE_LOCATION
	)
	fun toggleService() {
		viewModel.toggleService()
	}
}