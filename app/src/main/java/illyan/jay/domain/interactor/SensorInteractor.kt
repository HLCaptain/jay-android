/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.domain.interactor

import android.hardware.SensorEventListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import illyan.jay.data.sensor.SensorDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sensor interactor is a layer which aims to be the intermediary
 * between a higher level logic and lower level data source.
 *
 * @property sensorDataSource lower level data source to interact with.
 * @constructor Create empty Sensor interactor
 */
@Singleton
class SensorInteractor @Inject constructor(
    private val sensorDataSource: SensorDataSource
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
    ) = sensorDataSource.registerSensorListener(listener, type, delay)

    /**
     * Unregister sensor listener.
     *
     * @param listener listener to unregister/unsubscribe from updates.
     */
    fun unregisterSensorListener(listener: SensorEventListener) =
        sensorDataSource.unregisterSensorListener(listener)

    /**
     * Request location updates.
     *
     * @param request data object which determines the quality of service.
     * @param callback method to call when there is a new location update.
     */
    fun requestLocationUpdates(
        request: LocationRequest,
        callback: LocationCallback
    ) = sensorDataSource.requestLocationUpdates(request, callback)

    /**
     * Remove location updates.
     *
     * @param callback method to remove from new location updates.
     */
    fun removeLocationUpdates(callback: LocationCallback) =
        sensorDataSource.removeLocationUpdates(callback)
}
