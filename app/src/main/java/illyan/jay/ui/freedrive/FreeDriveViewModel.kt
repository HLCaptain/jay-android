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

package illyan.jay.ui.freedrive

import android.location.Location
import androidx.compose.foundation.layout.PaddingValues
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.mapbox.common.location.toCommonLocation
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.data.datastore.model.AppSettings
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.MapboxInteractor
import illyan.jay.domain.interactor.ServiceInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.ui.map.toEdgeInsets
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
class FreeDriveViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val serviceInteractor: ServiceInteractor,
    private val mapboxInteractor: MapboxInteractor,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val isJayServiceRunning = serviceInteractor.isJayServiceRunning

    val startServiceAutomatically = settingsInteractor.userPreferences
        .map { it?.freeDriveAutoStart ?: AppSettings.default.preferences.freeDriveAutoStart }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSettings.default.preferences.freeDriveAutoStart
        )

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation = _lastLocation.asStateFlow()

    private val _viewportDataSource = MutableStateFlow<MapboxNavigationViewportDataSource?>(null)
    val viewportDataSource = _viewportDataSource.asStateFlow()

    private val _mapboxNavigation = MutableStateFlow<MapboxNavigation?>(null)
    val mapboxNavigation = _mapboxNavigation.asStateFlow()

    var followingPaddingOffset: EdgeInsets?
        get() = viewportDataSource.value?.followingPadding
        set(value) {
            value?.let { _viewportDataSource.value?.followingPadding = it }
        }

    private val _navigationCamera = MutableStateFlow<NavigationCamera?>(null)
    val navigationCamera = _navigationCamera.asStateFlow()

    private val _firstLocationReceived = MutableStateFlow(false )
    val firstLocationReceived = _firstLocationReceived.asStateFlow()

    private val routesObserver = RoutesObserver { result ->
        val routes = result.navigationRoutes
        if (routes.isNotEmpty()) {
            viewportDataSource.value?.onRouteChanged(routes.first())
            viewportDataSource.value?.evaluate()
        } else {
            viewportDataSource.value?.clearRouteData()
            viewportDataSource.value?.evaluate()
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        viewportDataSource.value?.onRouteProgressChanged(routeProgress)
        viewportDataSource.value?.evaluate()
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { lastLocation ->
                viewportDataSource.value?.onLocationChanged(lastLocation.toCommonLocation())
                viewportDataSource.value?.evaluate()
                _lastLocation.value = lastLocation
                if (!firstLocationReceived.value) {
                    _firstLocationReceived.value = true
                    navigationCamera.value?.requestNavigationCameraToFollowing()
                }
                Unit
            }
        }
    }

    fun load() {
        viewModelScope.launch(dispatcherIO) {
            startServiceAutomatically.first {
                if (it) serviceInteractor.startJayService()
                true
            }
        }
    }

    fun loadViewport(
        map: MapboxMap,
        camera: CameraAnimationsPlugin,
        padding: EdgeInsets,
    ) {
        _mapboxNavigation.value = MapboxNavigationApp.current()
        mapboxNavigation.value?.let {
            it.registerRoutesObserver(routesObserver)
            it.registerRouteProgressObserver(routeProgressObserver)
        }
        _viewportDataSource.value = MapboxNavigationViewportDataSource(map)
        // same as followingPaddingOffset = padding
        viewportDataSource.value?.followingPadding = padding
        viewportDataSource.value?.followingZoomPropertyOverride(16.0)
        _navigationCamera.value = NavigationCamera(
            mapboxMap = map,
            cameraPlugin = camera,
            viewportDataSource = _viewportDataSource.value!!,
        )
        mapboxInteractor.requestLocationUpdates(callback)
    }

    fun loadViewport(
        map: MapboxMap,
        camera: CameraAnimationsPlugin,
        padding: PaddingValues,
        density: Float = 2.75f,
    ) {
        loadViewport(
            map = map,
            camera = camera,
            padding = padding.toEdgeInsets(density),
        )
    }

    fun disposeViewport() {
        mapboxInteractor.removeLocationUpdates(callback)
        _navigationCamera.value?.requestNavigationCameraToIdle()
        _mapboxNavigation.value?.let {
            it.unregisterRoutesObserver(routesObserver)
            it.unregisterRouteProgressObserver(routeProgressObserver)
        }
        _firstLocationReceived.value = false
    }

    fun toggleService() {
        if (isJayServiceRunning.value) {
            serviceInteractor.stopJayService()
        } else {
            serviceInteractor.startJayService()
        }
    }

    fun setAutoStartService(startServiceAutomatically: Boolean) {
        settingsInteractor.freeDriveAutoStart = startServiceAutomatically
    }
}