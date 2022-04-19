/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.interactor

import android.hardware.SensorEventListener
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