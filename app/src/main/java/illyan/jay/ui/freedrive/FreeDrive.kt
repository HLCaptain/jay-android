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

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.maps.plugin.locationcomponent.location
import com.ramcosta.composedestinations.annotation.Destination
import illyan.jay.R
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.mapView
import illyan.jay.ui.map.turnOnWithDefaultPuck
import illyan.jay.ui.menu.MenuNavGraph

@OptIn(ExperimentalPermissionsApi::class)
@MenuNavGraph
@Destination
@Composable
fun FreeDriveScreen(
    freeDriveViewModel: FreeDriveViewModel = hiltViewModel(),
) {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

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
            // TODO start session, then wait for new data to be shown by Mapbox Navigation SDK
            val startServiceAutomatically by freeDriveViewModel.startServiceAutomatically.collectAsState()
            LaunchedEffect(true) {
                freeDriveViewModel.load()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(32.dp)
            ) {
                mapView.location.turnOnWithDefaultPuck(context)
                // TODO toggle to save preference to enable free-driving when navigated to screen automatically or not
                val isServiceRunning by freeDriveViewModel.isJayServiceRunning.collectAsState()
                val buttonLabel = if (isServiceRunning) {
                    stringResource(R.string.stop_free_driving)
                } else {
                    stringResource(R.string.start_free_driving)
                }
                Button(onClick = { freeDriveViewModel.toggleService() }
                ) {
                    Text(text = buttonLabel)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        style = MaterialTheme.typography.labelLarge,
                        text = stringResource(R.string.automatically_turn_on_free_driving)
                    )
                    Switch(
                        checked = startServiceAutomatically,
                        onCheckedChange = {
                        /* TODO save preferences */
                            freeDriveViewModel.setAutoStartService(it)
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
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.location_permission_denied_description),
            style = MaterialTheme.typography.bodyMedium
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
                    color = Color.White
                )
            }
        }
    }
}