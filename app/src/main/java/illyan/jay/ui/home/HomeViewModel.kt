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

package illyan.jay.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.perf.FirebasePerformance
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.MapboxInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.ui.map.BmeK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mapboxInteractor: MapboxInteractor,
    private val authInteractor: AuthInteractor,
    private val sessionInteractor: SessionInteractor,
    private val performance: FirebasePerformance,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {

    private val _initialLocation = MutableStateFlow<Location?>(null)
    val initialLocation = _initialLocation.asStateFlow()

    private val locationLoadTrace = performance.newTrace("Initial location load")

    val initialLocationLoaded = initialLocation.map {
        locationLoadTrace.apply { if (it != null) stop() else start() }
        it != null
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _cameraOptionsBuilder = MutableStateFlow<CameraOptions.Builder?>(null)
    val cameraOptionsBuilder = _cameraOptionsBuilder.asStateFlow()

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val userPhotoUrl = authInteractor.userPhotoUrlStateFlow

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            result?.let {
                if (_initialLocation.value == null) {
                    _initialLocation.value = it.lastLocation
                    disposeLocationUpdates(this)
                }
            }
        }
        override fun onFailure(exception: Exception) { exception.printStackTrace() }
    }

    fun stopDanglingOngoingSessions() {
        viewModelScope.launch(dispatcherIO) {
            sessionInteractor.stopDanglingSessions()
        }
    }

    fun loadLastLocation() {
        viewModelScope.launch(dispatcherIO) {
            // TODO: load last location from database first
            initialLocation.first { location ->
                if (location != null) {
                    _cameraOptionsBuilder.value = CameraOptions.Builder()
                        .zoom(12.0)
                        .center(Point.fromLngLat(location.longitude, location.latitude))
                    true
                } else {
                    // Use BME K building as the default location for now.
                    _cameraOptionsBuilder.value = CameraOptions.Builder()
                        .zoom(12.0)
                        .center(Point.fromLngLat(BmeK.longitude, BmeK.latitude))
                    false
                }
            }
        }
    }

    fun requestLocationUpdates() {
        mapboxInteractor.requestLocationUpdates(mapboxInteractor.defaultRequest, callback)
    }

    private fun disposeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        mapboxInteractor.removeLocationUpdates(callback)
    }

    fun dispose() {
        disposeLocationUpdates(callback)
    }
}
