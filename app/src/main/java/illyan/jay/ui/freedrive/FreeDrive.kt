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

package illyan.jay.ui.freedrive

import android.Manifest
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.data.datastore.model.AppSettings
import illyan.jay.ui.components.PreviewThemesScreensFonts
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
import illyan.jay.ui.settings.user.BooleanSetting
import illyan.jay.ui.theme.JayTheme
import illyan.jay.util.plus

private const val paddingRatio = 0.25f

val DefaultScreenOnSheetPadding = PaddingValues(
    start = MenuItemPadding * 2,
    end = MenuItemPadding * 2,
    top = MenuItemPadding * 2,
    bottom = RoundedCornerRadius + MenuItemPadding
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
fun FreeDrive(
    viewModel: FreeDriveViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val isServiceRunning by viewModel.isJayServiceRunning.collectAsStateWithLifecycle()
    val startServiceAutomatically by viewModel.startServiceAutomatically.collectAsStateWithLifecycle(
        AppSettings.default.preferences.freeDriveAutoStart
    )
    val cameraPadding by cameraPadding.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(cameraPadding) {
        viewModel.followingPaddingOffset =
            (cameraPadding + calculatePaddingOffset()).toEdgeInsets(density.value)
    }
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    when (locationPermissionState.status) {
        is PermissionStatus.Granted -> {
            DisposableEffect(Unit) {
                viewModel.load()
                mapView.value?.let {
                    viewModel.loadViewport(
                        it.getMapboxMap(),
                        it.camera,
                        cameraPadding + calculatePaddingOffset()
                    )
                }
                mapView.value?.location?.turnOnWithDefaultPuck(context)
                onDispose {
                    viewModel.disposeViewport()
                    viewModel.lastLocation.value?.let { location ->
                        flyToLocation {
                            it
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
                    }
                }
            }
            FreeDriveScreenWithPermission(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DefaultScreenOnSheetPadding),
                startServiceAutomatically = startServiceAutomatically,
                isServiceRunning = isServiceRunning,
                onToggleService = viewModel::toggleService,
                setStartServiceAutomatically = viewModel::setAutoStartService,
            )
        }
        is PermissionStatus.Denied -> {
            FreeDriveScreenWithoutPermission(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DefaultScreenOnSheetPadding),
                onRequestPermission = locationPermissionState::launchPermissionRequest
            )
        }
    }
}

@Composable
fun FreeDriveScreenWithPermission(
    modifier: Modifier = Modifier,
    startServiceAutomatically: Boolean = AppSettings.default.preferences.freeDriveAutoStart,
    isServiceRunning: Boolean = false,
    onToggleService: () -> Unit = {},
    setStartServiceAutomatically: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier
    ) {
        Button(
            modifier = Modifier.animateContentSize(),
            onClick = { onToggleService() }
        ) {
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = isServiceRunning
            ) {
                Text(
                    modifier = Modifier.animateContentSize(),
                    text = if (it) {
                        stringResource(R.string.stop_free_driving)
                    } else {
                        stringResource(R.string.start_free_driving)
                    }
                )
            }
        }
        BooleanSetting(
            settingName = stringResource(R.string.automatically_turn_on_free_driving),
            value = startServiceAutomatically,
            setValue = setStartServiceAutomatically,
            enabledText = stringResource(R.string.on),
            disabledText = stringResource(R.string.off),
        )
    }
}

@Composable
fun FreeDriveScreenWithoutPermission(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.location_permission_denied_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.location_permission_denied_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRequestPermission,
            ) {
                Text(
                    text = stringResource(R.string.request_permission),
                )
            }
        }
    }
}

@PreviewThemesScreensFonts
@Composable
private fun FreeDriveScreenWithPermissionPreview() {
    JayTheme {
        FreeDriveScreenWithPermission()
    }
}
@PreviewThemesScreensFonts
@Composable
private fun FreeDriveScreenWithoutPermissionPreview() {
    JayTheme {
        FreeDriveScreenWithoutPermission()
    }
}
