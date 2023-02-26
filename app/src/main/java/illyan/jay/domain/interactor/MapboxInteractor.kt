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

package illyan.jay.domain.interactor

import android.annotation.SuppressLint
import android.content.Context
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.navigation.base.options.NavigationOptions
import illyan.jay.BuildConfig
import illyan.jay.data.sensor.MapboxDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapboxInteractor @Inject constructor(
    private val mapboxDataSource: MapboxDataSource,
    context: Context
) {
    val defaultRequest: LocationEngineRequest = LocationEngineRequest
        .Builder(LocationInteractor.LOCATION_REQUEST_INTERVAL_REALTIME)
        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
        .build()

    val defaultNavigationOptions: NavigationOptions = NavigationOptions.Builder(context)
        .accessToken(BuildConfig.MapboxAccessToken)
        .build()

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>
    ) {
        mapboxDataSource.requestLocationUpdates(request, callback)
    }

    fun removeLocationUpdates(
        callback: LocationEngineCallback<LocationEngineResult>
    ) {
        mapboxDataSource.removeLocationUpdates(callback)
    }
}