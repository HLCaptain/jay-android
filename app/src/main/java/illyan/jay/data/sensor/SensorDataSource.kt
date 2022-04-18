package illyan.jay.data.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

class SensorDataSource @Inject constructor(
	private val sensorManager: SensorManager,
	private val fusedLocationProviderClient: FusedLocationProviderClient
) {
	fun registerSensorListener(
		listener: SensorEventListener,
		type: Int,
		delay: Int
	) = sensorManager.registerListener(listener, sensorManager.getDefaultSensor(type), delay)

	fun updateSensorListener(
		newListener: SensorEventListener,
		oldListener: SensorEventListener,
		type: Int,
		delay: Int
	) {
		unregisterSensorListener(oldListener)
		registerSensorListener(newListener, type, delay)
	}

	fun unregisterSensorListener(listener: SensorEventListener) =
		sensorManager.unregisterListener(listener)

	@SuppressLint("MissingPermission")
	fun requestLocationUpdates(
		request: LocationRequest,
		callback: LocationCallback
	) = fusedLocationProviderClient
		.requestLocationUpdates(request, callback, Looper.getMainLooper())


	fun removeLocationUpdates(callback: LocationCallback) =
		fusedLocationProviderClient.removeLocationUpdates(callback)
}