package illyan.jay.domain.interactor

import android.annotation.SuppressLint
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import illyan.jay.data.sensor.SensorDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorInteractor @Inject constructor(
	private val sensorDataSource: SensorDataSource
) {
	fun registerSensorListener(
		listener: SensorEventListener,
		type: Int,
		delay: Int
	) = sensorDataSource.registerSensorListener(listener, type, delay)

	fun unregisterSensorListener(listener: SensorEventListener) =
		sensorDataSource.unregisterSensorListener(listener)

	fun requestLocationUpdates(
		request: LocationRequest,
		callback: LocationCallback
	) = sensorDataSource.requestLocationUpdates(request, callback)

	fun removeLocationUpdates(callback: LocationCallback) =
		sensorDataSource.removeLocationUpdates(callback)
}