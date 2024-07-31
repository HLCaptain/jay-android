/*
 * Copyright (c) 2022-2024 Balázs Püspök-Kiss (Illyan)
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

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import illyan.jay.data.sensor.MapboxDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapboxInteractor @Inject constructor(
    private val mapboxDataSource: MapboxDataSource
) {
    private val defaultRequest = LocationRequest
        .Builder(LocationInteractor.LOCATION_REQUEST_INTERVAL_REALTIME)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(callback: LocationCallback) =  mapboxDataSource.requestLocationUpdates(defaultRequest, callback)
    fun removeLocationUpdates(callback: LocationCallback) = mapboxDataSource.removeLocationUpdates(callback)
}