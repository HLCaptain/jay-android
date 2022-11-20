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

package illyan.jay.ui.freedrive

import android.location.Location
import androidx.compose.foundation.layout.PaddingValues
import androidx.lifecycle.ViewModel
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.MapboxInteractor
import illyan.jay.domain.interactor.ServiceInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.ui.map.toEdgeInsets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class FreeDriveViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val serviceInteractor: ServiceInteractor,
    private val mapboxInteractor: MapboxInteractor,
) : ViewModel() {

    val isJayServiceRunning = serviceInteractor.isJayServiceRunning
    val startServiceAutomatically =
        settingsInteractor.appSettingsFlow.map { it.turnOnFreeDriveAutomatically }

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation = _lastLocation.asStateFlow()

    private val _viewportDataSource = MutableStateFlow<MapboxNavigationViewportDataSource?>(null)
    val viewportDataSource = _viewportDataSource.asStateFlow()

    private val _mapboxNavigation = MutableStateFlow<MapboxNavigation?>(null)
    val mapboxNavigation = _mapboxNavigation.asStateFlow()

    var followingPaddingOffset: EdgeInsets?
        get() = viewportDataSource.value?.followingPadding
        set(value) {
            value?.let{
                _viewportDataSource.value?.followingPadding = it
            }
        }

    private val _navigationCamera = MutableStateFlow<NavigationCamera?>(null)
    val navigationCamera = _navigationCamera.asStateFlow()

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

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        var firstLocationReceived = false
        override fun onSuccess(result: LocationEngineResult?) {
            result?.let {
                it.lastLocation?.let { lastLocation ->
                    viewportDataSource.value?.onLocationChanged(lastLocation)
                    viewportDataSource.value?.evaluate()
                    _lastLocation.value = lastLocation
                    if (!firstLocationReceived) {
                        firstLocationReceived = true
                        navigationCamera.value?.requestNavigationCameraToFollowing()
                    }
                    Unit
                }
            }
        }

        override fun onFailure(exception: Exception) {
            exception.printStackTrace()
        }
    }

    suspend fun load() {
        startServiceAutomatically.first {
            if (it) serviceInteractor.startJayService()
            true
        }
    }

    fun loadViewport(
        map: MapboxMap,
        camera: CameraAnimationsPlugin,
        padding: EdgeInsets,
    ) {
        _mapboxNavigation.value = MapboxNavigation(
            navigationOptions = mapboxInteractor.defaultNavigationOptions
        )
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
        mapboxInteractor.requestLocationUpdates(mapboxInteractor.defaultRequest, callback)
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
        _mapboxNavigation.value?.onDestroy()
    }

    fun toggleService() {
        if (isJayServiceRunning.value) {
            serviceInteractor.stopJayService()
        } else {
            serviceInteractor.startJayService()
        }
    }

    suspend fun setAutoStartService(startServiceAutomatically: Boolean) {
        settingsInteractor.updateAppSettings {
            it.copy(turnOnFreeDriveAutomatically = startServiceAutomatically)
        }
    }
}