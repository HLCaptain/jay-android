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

package illyan.jay.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.flyTo
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.ui.home.mapView
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.navigation.model.Place

@OptIn(ExperimentalMaterialApi::class)
@MenuNavGraph
@Destination
@Composable
fun NavigationScreen(
    place: Place,
    zoom: Double = 6.0,
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.toDouble()
    // TODO: check with viewmodel for new broadcast receiver places and
    //  swap this launchedeffect to something else
    DisposableEffect(key1 = true) {
        viewModel.load(place)
        onDispose { viewModel.dispose() }
    }
    var firstOnLaunch by remember { mutableStateOf(true) }
    LaunchedEffect(
        key1 = sheetState.isAnimationRunning,
        key2 = viewModel.place
    ) {
        if (!sheetState.isAnimationRunning && !firstOnLaunch) {
            mapView.getMapboxMap().flyTo(
                CameraOptions.Builder()
                    .center(
                        Point.fromLngLat(
                            viewModel.place.longitude,
                            viewModel.place.latitude
                        )
                    )
                    .zoom(zoom)
                    .padding(
                        EdgeInsets(
                            0.0,
                            0.0,
                            // sheetState.offset.value is not in DP! Probably px...
                            screenHeight - sheetState.offset.value / 4,
                            0.0
                        )
                    )
                    .build()
            )
        }
        firstOnLaunch = false
    }
    Text(
        modifier = Modifier.height(200.dp),
        text = "Sheet offset ${sheetState.offset.value}\n" +
                "isAnimationRunning ${sheetState.isAnimationRunning}\n" +
                "Screen height $screenHeight"
    )
}
