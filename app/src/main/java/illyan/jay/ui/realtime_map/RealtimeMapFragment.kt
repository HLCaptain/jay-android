package illyan.jay.ui.realtime_map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.zsmb.rainbowcake.extensions.exhaustive
import co.zsmb.rainbowcake.hilt.getViewModelFromFactory
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.R
import illyan.jay.databinding.FragmentRealtimeMapBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
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

			}
			is Loading -> {

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
		viewModel.load()
	}

	override fun onPause() {
		viewModel.unload()
		super.onPause()
	}
}