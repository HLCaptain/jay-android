/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.realtime_map

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentRealtimeMapBinding
import illyan.jay.ui.custom.RainbowCakeFragment
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@AndroidEntryPoint
@RuntimePermissions
class RealtimeMapFragment :
	RainbowCakeFragment<RealtimeMapViewState, RealtimeMapViewModel, FragmentRealtimeMapBinding>(),
	OnMapReadyCallback {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentRealtimeMapBinding =
		FragmentRealtimeMapBinding::inflate

	private var camBounds = LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
	private var map: GoogleMap? = null

	@SuppressLint("MissingPermission")
	override fun render(viewState: RealtimeMapViewState) {
		when (viewState) {
			is Initial -> {
				// Show an splash screen screen
			}
			is Loading -> {
				// Show a loading indicator
			}
			is Ready -> {
				map?.let {
					it.isMyLocationEnabled = true
					if (!camBounds.contains(viewState.currentLocation.latLng)) {
						it.animateCamera(
							CameraUpdateFactory.newLatLngZoom(
								viewState.currentLocation.latLng,
								16f
							)
						)
						val southwest = SphericalUtil.computeOffset(
							viewState.currentLocation.latLng,
							100.0,
							45.0 + 180.0
						)
						val northeast = SphericalUtil.computeOffset(
							viewState.currentLocation.latLng,
							100.0,
							45.0
						)
						camBounds = LatLngBounds(southwest, northeast)
					}
				}
				Unit
			}
		}.exhaustive
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.realtimeMap.getFragment<SupportMapFragment>().getMapAsync(this)
	}

	override fun onMapReady(p0: GoogleMap) {
		map = p0
		loadMapWithPermissionCheck()
	}

	override fun onPause() {
		viewModel.unload()
		super.onPause()
	}

	@NeedsPermission(
		Manifest.permission.FOREGROUND_SERVICE,
		Manifest.permission.ACCESS_FINE_LOCATION
	)
	fun loadMap() {
		viewModel.load()
	}
}