/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.MapboxInteractor
import illyan.jay.ui.map.ButeK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mapboxInteractor: MapboxInteractor,
) : ViewModel() {

    private val _initialLocation = MutableStateFlow<Location?>(null)
    val initialLocation = _initialLocation.asStateFlow()

    private val _initialLocationLoaded = MutableStateFlow(false)
    val initialLocationLoaded = _initialLocationLoaded.asStateFlow()

    private val _cameraOptionsBuilder = MutableStateFlow<CameraOptions.Builder?>(null)
    val cameraOptionsBuilder = _cameraOptionsBuilder.asStateFlow()

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            result?.let {
                if (_initialLocation.value == null) {
                    _initialLocation.value = it.lastLocation
                    _initialLocationLoaded.value = true
                    disposeLocationUpdates(this)
                }
            }
        }
        override fun onFailure(exception: Exception) { exception.printStackTrace() }
    }

    private val request = LocationEngineRequest
        .Builder(LocationInteractor.LOCATION_REQUEST_INTERVAL_DEFAULT)
        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
        .build()

    suspend fun loadLastLocation() {
        mapboxInteractor.requestLocationUpdates(request, callback)
        initialLocation.first { location ->
            if (location != null) {
                _cameraOptionsBuilder.value = CameraOptions.Builder()
                    .zoom(12.0)
                    .center(Point.fromLngLat(location.longitude, location.latitude))
                true
            } else {
                // Use Bute K building as the default location for now.
                _cameraOptionsBuilder.value = CameraOptions.Builder()
                    .zoom(12.0)
                    .center(Point.fromLngLat(ButeK.longitude, ButeK.latitude))
                false
            }
        }
    }

    private fun disposeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        mapboxInteractor.removeLocationUpdates(callback)
    }

    fun dispose() {
        disposeLocationUpdates(callback)
    }
}
