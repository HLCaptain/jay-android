package illyan.jay.ui.realtime_map

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import illyan.jay.data.disk.toDomainModel
import illyan.jay.domain.interactor.SensorInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.ui.realtime_map.model.UiLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RealtimeMapPresenter @Inject constructor(
	private val sensorInteractor: SensorInteractor
) {

	private val locationRequest = LocationRequest
		.create()
		.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
		.setInterval(200)
		.setSmallestDisplacement(1f)

	private var locationCallback: LocationCallback = object : LocationCallback() {}

	fun setLocationListener(listener: (UiLocation) -> Unit) {
		locationCallback = object : LocationCallback() {
			override fun onLocationResult(p0: LocationResult) {
				super.onLocationResult(p0)
				listener.invoke(p0.lastLocation.toUiLocation())
			}
		}
		sensorInteractor.requestLocationUpdates(
			locationRequest,
			locationCallback
		)
	}

	fun stopListening() = sensorInteractor.removeLocationUpdates(locationCallback)
}

private fun Location.toUiLocation() = UiLocation(
	latLng = LatLng(latitude, longitude)
)