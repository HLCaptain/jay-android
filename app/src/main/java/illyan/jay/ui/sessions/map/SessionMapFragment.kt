/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.sessions.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.addPolyline
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentSessionMapBinding
import illyan.jay.ui.custom.RainbowCakeFragment
import illyan.jay.ui.sessions.map.model.UiLocation

@AndroidEntryPoint
class SessionMapFragment :
	RainbowCakeFragment<SessionMapViewState, SessionMapViewModel, FragmentSessionMapBinding>(),
	OnMapReadyCallback {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentSessionMapBinding =
		FragmentSessionMapBinding::inflate

	private val args: SessionMapFragmentArgs by navArgs()
	private var map: GoogleMap? = null

	override fun render(viewState: SessionMapViewState) {
		when (viewState) {
			is Initial -> {
				// Show splash screen of some kind
			}
			is Loading -> {
				// Show loading indicator
			}
			is Ready -> {
				map?.let {
					it.clear()
					viewState.locations.forEachIndexed { index, location ->
						it.addPolyline {
							if (index != viewState.locations.lastIndex) {
								addAll(
									mutableListOf(
										location.latLng,
										viewState.locations[index + 1].latLng
									)
								)
								color(location.color.toArgb())
							}
						}
					}
					if (viewState.locations.isNotEmpty()) {
						val latLngBounds = getPathBounds(viewState.locations)
						if (!it.projection.visibleRegion.latLngBounds.contains(latLngBounds.southwest)
							|| !it.projection.visibleRegion.latLngBounds.contains(latLngBounds.northeast)
							|| viewState.doAnimateCamera
						) {
							it.animateCamera(
								CameraUpdateFactory.newLatLngBounds(
									LatLngBounds(latLngBounds.southwest, latLngBounds.northeast),
									160
								)
							)
						}
					}
				}
				Unit
			}
		}.exhaustive
	}

	/**
	 * Get LatLng bounds for a path.
	 *
	 * @param locations list which contains the locations for the path.
	 * Each location will be in the bounds.
	 *
	 * @return the bounds which will contain all location in the path.
	 */
	private fun getPathBounds(locations: List<UiLocation>): LatLngBounds {
		var latLngBounds = LatLngBounds(
			locations.first().latLng,
			locations.first().latLng
		)
		locations.forEach { latLngBounds = latLngBounds.including(it.latLng) }
		return latLngBounds
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.sessionRealtimeMap.getFragment<SupportMapFragment>().getMapAsync(this)
	}

	override fun onMapReady(p0: GoogleMap) {
		map = p0
		viewModel.loadPath(args.sessionId)
	}
}