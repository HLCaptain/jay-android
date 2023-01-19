/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

pose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.data.disk.model.AppSettings
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.absoluteBottom
import illyan.jay.ui.home.absoluteTop
import illyan.jay.ui.home.cameraPadding
import illyan.jay.ui.home.density
import illyan.jay.ui.home.flyToLocation
import illyan.jay.ui.home.mapView
import illyan.jay.ui.map.toEdgeInsets
import illyan.jay.ui.map.turnOnWithDefaultPuck
import illyan.jay.util.plus
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch.*

/

package illyan.jay.ui.freedrive

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.data.disk.model.AppSettings
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.absoluteBottom
import illyan.jay.ui.home.absoluteTop
import illyan.jay.ui.home.cameraPadding
import illyan.jay.ui.home.density
import illyan.jay.ui.home.flyToLocation
import illyan.jay.ui.home.mapView
import illyan.jay.ui.map.toEdgeInsets
import illyan.jay.ui.map.turnOnWithDefaultPuck
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.util.plus
import kotlinx.coroutines.launch

private const val paddingRatio = 0.25f

val DefaultScreenOnSheetPadding = PaddingValues(
    start = MenuItemPadding * 2,
    end = MenuItemPadding * 2,
    top = MenuItemPadding * 2,
    bottom = RoundedCornerRadius
)

fun calculatePaddingOffset(): PaddingValues {
    val layoutHeight = absoluteBottom.value - absoluteTop.value
    val freeSpace = layoutHeight - cameraPadding.value.calculateBottomPadding()
    return PaddingValues(
        bottom = freeSpace * paddingRatio
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@MenuNavGraph
@Destination
@Composable
fun FreeDriveScreen(
    viewModel: FreeDriveViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    when (locationPermissionState.status) {
        is PermissionStatus.Denied -> {
            LocationPermissionDeniedScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        bottom = 8.dp + RoundedCornerRadius,
                        start = 8.dp,
                        end = 8.dp
                    ),
                locationPermissionState = locationPermissionState
            )
        }

        is PermissionStatus.Granted -> {
            val startServiceAutomatically by viewModel.startServiceAutomatically
                .collectAsState(AppSettings.default.turnOnFreeDriveAutomatically)
            val cameraPadding by cameraPadding.collectAsState()
            DisposableEffect(Unit) {
                coroutineScope.launch {
                    viewModel.load()
                    mapView.value?.let {
                        viewModel.loadViewport(
                            it.getMapboxMap(),
                            it.camera,
                            cameraPadding + calculatePaddingOffset()
                        )
                    }
                }
                mapView.value?.location?.turnOnWithDefaultPuck(context)
                onDispose {
                    viewModel.disposeViewport()
                    viewModel.lastLocation.value?.let { location ->
                        flyToLocation(
                            extraCameraOptions = { builder ->
                                builder
                                    .pitch(0.0)
                                    .bearing(0.0)
                                    .zoom(12.0)
                                    .center(
                                        Point.fromLngLat(
                                            location.longitude,
                                            location.latitude
                                        )
                                    )
                            }
                        )
                    }
                }
            }
            LaunchedEffect(cameraPadding) {
                viewModel.followingPaddingOffset =
                    (cameraPadding + calculatePaddingOffset()).toEdgeInsets(density.value)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DefaultScreenOnSheetPadding)
            ) {
                val isServiceRunning by viewModel.isJayServiceRunning.collectAsState()
                val buttonLabel = if (isServiceRunning) {
                    stringResource(R.string.stop_free_driving)
                } else {
                    stringResource(R.string.start_free_driving)
                }
                Button(
                    onClick = { viewModel.toggleService() }
                ) {
                    Text(text = buttonLabel)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.automatically_turn_on_free_driving),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Switch(
                        checked = startServiceAutomatically,
                        onCheckedChange = {
                            coroutineScope.launch {
                                viewModel.setAutoStartService(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionDeniedScreen(
    modifier: Modifier = Modifier,
    locationPermissionState: PermissionState,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.location_permission_denied_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.location_permission_denied_description),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { locationPermissionState.launchPermissionRequest() },
            ) {
                Text(
                    text = stringResource(R.string.request_permission),
                )
            }
        }
    }
}