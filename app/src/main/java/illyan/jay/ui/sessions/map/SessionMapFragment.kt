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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.addPolyline
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.databinding.FragmentSessionMapBinding
import illyan.jay.ui.custom.RainbowCakeFragment

@AndroidEntryPoint
class SessionMapFragment : RainbowCakeFragment<SessionMapViewState, SessionMapViewModel, FragmentSessionMapBinding>(), OnMapReadyCallback {
	override fun provideViewModel() = getViewModelFromFactory()
	override fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> FragmentSessionMapBinding = FragmentSessionMapBinding::inflate

	private val args: SessionMapFragmentArgs by navArgs()
	private var map: GoogleMap? = null

	override fun render(viewState: SessionMapViewState) {
		when(viewState) {
			is Initial -> {

			}
			is Loading -> {

			}
			is Ready -> {
				// TODO: rework state handling for maps
				// TODO: use colors, etc.
				map?.let {
					it.clear()
					it.addPolyline { addAll(viewState.locations.map { location -> location.latLng }) }
					if (viewState.locations.isNotEmpty()) {
						var southWest = viewState.locations.first().latLng
						var northEast = viewState.locations.first().latLng
						viewState.locations.forEach { location ->
							if (southWest.longitude > location.latLng.longitude) {
								southWest = LatLng(southWest.latitude, location.latLng.longitude)
							}
							if (southWest.latitude > location.latLng.latitude) {
								southWest = LatLng(location.latLng.latitude, southWest.longitude)
							}
							if (northEast.longitude < location.latLng.longitude) {
								northEast = LatLng(northEast.latitude, location.latLng.longitude)
							}
							if (northEast.latitude < location.latLng.latitude) {
								northEast = LatLng(location.latLng.latitude, northEast.longitude)
							}
						}
						if (!it.projection.visibleRegion.latLngBounds.contains(southWest) || !it.projection.visibleRegion.latLngBounds.contains(northEast) || viewState.firstLoaded) {
							it.animateCamera(
								CameraUpdateFactory.newLatLngBounds(
									LatLngBounds(
										southWest,
										northEast
									),
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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.sessionRealtimeMap.getFragment<SupportMapFragment>().getMapAsync(this)
	}

	override fun onMapReady(p0: GoogleMap) {
		map = p0
		viewModel.loadPath(args.sessionId)
	}
}