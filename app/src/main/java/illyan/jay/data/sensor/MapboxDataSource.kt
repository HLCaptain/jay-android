/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.sensor

import android.annotation.SuppressLint
import android.os.Looper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapboxDataSource @Inject constructor(
    private val locationEngine: LocationEngine
) {
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>
    ) {
        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun removeLocationUpdates(
        callback: LocationEngineCallback<LocationEngineResult>
    ) {
        locationEngine.removeLocationUpdates(callback)
    }
}