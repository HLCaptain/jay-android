/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.data.sensor

import android.annotation.SuppressLint
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import javax.inject.Inject

/**
 * Sensor data source helps registering SensorEventListeners to sensor events.
 * Also can register LocationCallbacks to location updates.
 *
 * @property sensorManager enables listening to sensor events.
 * @property fusedLocationProviderClient enables listening to location events
 * and setting location request options.
 * @constructor Create empty Sensor data source
 */
class SensorDataSource @Inject constructor(
	private val sensorManager: SensorManager,
	private val fusedLocationProviderClient: FusedLocationProviderClient
) {
	/**
	 * Register sensor listener.
	 *
	 * @param listener a SensorEventListener object.
	 * @param type of sensors requested.
	 * @param delay requested delay to fire an event.
	 * This might fluctuate based on device usage.
	 *
	 * @return true if the sensor is supported and successfully enabled.
	 */
	fun registerSensorListener(
		listener: SensorEventListener,
		type: Int,
		delay: Int
	) = sensorManager.registerListener(listener, sensorManager.getDefaultSensor(type), delay)

	/**
	 * Unregister sensor listener.
	 *
	 * @param listener listener to unregister/unsubscribe from updates.
	 */
	fun unregisterSensorListener(listener: SensorEventListener) =
		sensorManager.unregisterListener(listener)

	/**
	 * Update sensor listener.
	 *
	 * @param newListener new listener to register/subscribe to updates.
	 * @param oldListener old listener to unregister/unsubscribe from updates.
	 * @param type  requested delay to fire an event.
	 * @param delay requested delay to fire an event.
	 * This might fluctuate based on device usage.
	 */
	fun updateSensorListener(
		newListener: SensorEventListener,
		oldListener: SensorEventListener,
		type: Int,
		delay: Int
	) {
		unregisterSensorListener(oldListener)
		registerSensorListener(newListener, type, delay)
	}

	/**
	 * Request location updates.
	 *
	 * @param request data object which determines the quality of service.
	 * @param callback method to call when there is a new location update.
	 */
	@SuppressLint("MissingPermission")
	fun requestLocationUpdates(
		request: LocationRequest,
		callback: LocationCallback
	) = fusedLocationProviderClient
		.requestLocationUpdates(request, callback, Looper.getMainLooper())


	/**
	 * Remove location updates.
	 *
	 * @param callback method to remove from new location updates.
	 */
	fun removeLocationUpdates(callback: LocationCallback) =
		fusedLocationProviderClient.removeLocationUpdates(callback)
}