package illyan.jay.service.listener

import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.maps.android.SphericalUtil
import illyan.jay.data.disk.toDomainModel
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationEventListener @Inject constructor(
	private val locationInteractor: LocationInteractor,
	private val sessionInteractor: SessionInteractor
) : SessionSensorEventListener(sessionInteractor) {

	var locationCallback: LocationCallback = object : LocationCallback() {}
		set(value) {
			field = object : LocationCallback() {
				override fun onLocationResult(locationResult: LocationResult) {
					super.onLocationResult(locationResult)
					scope.launch(Dispatchers.IO) {
						// Saving locations for every ongoing session
						ongoingSessionIds.toList().forEach { sessionId ->
							val newLocation = locationResult.lastLocation.toDomainModel(sessionId)
							// Updating distances for each location
							locationInteractor.getLocations(sessionId, 1)
								.flowOn(Dispatchers.IO)
								.map { it.firstOrNull() }
								.first { location ->
									location?.let { lastLocation ->
										// Need session to calculate new distance value from the old one
										sessionInteractor.getSession(sessionId)
											.flowOn(Dispatchers.IO)
											.first {
												it?.let { session ->
													// Updating distances
													session.distance += SphericalUtil.computeDistanceBetween(
														lastLocation.latLng,
														newLocation.latLng
													)
													// Saving the session with the new distance
													sessionInteractor.saveSession(session)
												}
												true
											}
									}
									true
								}
							locationInteractor.saveLocation(newLocation)
						}
					}
					value.onLocationResult(locationResult)
				}

				override fun onLocationAvailability(p0: LocationAvailability) =
					value.onLocationAvailability(p0)
			}
		}

	var locationRequest = LocationRequest
		.create()
		.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
		.setInterval(200)
		.setSmallestDisplacement(1f)

	init {
		locationCallback = object : LocationCallback() {}
	}
}