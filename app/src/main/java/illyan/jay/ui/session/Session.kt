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

package illyan.jay.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowRightAlt
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.LightDarkThemePreview
import illyan.jay.ui.components.SmallCircularProgressIndicator
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.mapView
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.home.tryFlyToPath
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.util.format
import java.math.RoundingMode

val DefaultScreenOnSheetPadding = PaddingValues(
    start = MenuItemPadding * 2,
    end = MenuItemPadding * 2,
    top = MenuItemPadding,
    bottom = RoundedCornerRadius + MenuItemPadding * 2
)

@OptIn(ExperimentalMaterialApi::class)
@MenuNavGraph
@Destination
@Composable
fun SessionScreen(
    sessionUUID: String,
    viewModel: SessionViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    LaunchedEffect(Unit) {
        viewModel.load(sessionUUID)
    }
    var sheetHeightNotSet by remember { mutableStateOf(true) }
    var flownToPath by remember { mutableStateOf(false) }
    val path by viewModel.path.collectAsState()
    var fakeStopped by remember { mutableStateOf(false) }
    LaunchedEffect(sheetState.isAnimationRunning) {
        if (fakeStopped) {
            sheetHeightNotSet = sheetState.isAnimationRunning
        }
        if (!sheetState.isAnimationRunning) {
            fakeStopped = true
        }
    }
    LaunchedEffect(
        path,
        sheetHeightNotSet
    ) {
        path?.let {
            if (!flownToPath) {
                tryFlyToPath(
                    path = path!!.map { location ->
                        Point.fromLngLat(
                            location.latLng.longitude,
                            location.latLng.latitude
                        )
                    },
                    extraCondition = { !sheetHeightNotSet },
                    onFly = { flownToPath = true }
                )
            }
        }
    }
    DisposableEffect(
        path
    ) {
        val annotationsPlugin = mapView.value?.annotations
        val polylineAnnotationManager = annotationsPlugin?.createPolylineAnnotationManager()
        polylineAnnotationManager?.create(
            option = PolylineAnnotationOptions()
                .withPoints(
                    path?.map {
                        Point.fromLngLat(it.latLng.longitude, it.latLng.latitude)
                    } ?: emptyList()
                )
                // TODO: make this drawn line a gradient, showing speed via LineLayer,
                //  aka https://docs.mapbox.com/android/legacy/maps/examples/line-gradient/
                // AzureBlue
                .withLineColor("#1b8fff")
                .withLineWidth(5.0)
        )
        onDispose {
            annotationsPlugin?.removeAnnotationManager(polylineAnnotationManager!!)
        }
    }
    SessionDetailsScreen(
        viewModel = viewModel
    )
}

@LightDarkThemePreview
@Composable
fun SessionDetailsScreen(
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsState()
    val path by viewModel.path.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DefaultScreenOnSheetPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session?.startLocationName ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowRightAlt, contentDescription = "",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
                Crossfade(targetState = session?.endDateTime == null) {
                    if (it) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz, contentDescription = "",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    } else {
                        Text(
                            text = session?.endLocationName ?: stringResource(R.string.unknown),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = path == null || session == null
            ) {
                SmallCircularProgressIndicator()
            }
        }
        Column {
            Text(
                text = "${stringResource(R.string.distance)}: " +
                        if (session?.totalDistance == null) {
                            stringResource(R.string.unknown)
                        } else {
                            "${session!!.totalDistance!!
                                .div(1000)
                                .toBigDecimal()
                                .setScale(2, RoundingMode.FLOOR)} " +
                                    stringResource(R.string.kilometers)
                        },
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${stringResource(R.string.duration)}: " +
                        (session?.duration?.format(
                            separator = " ",
                            second = stringResource(R.string.second_short),
                            minute = stringResource(R.string.minute_short),
                            hour = stringResource(R.string.hour_short),
                            day = stringResource(R.string.day_short)
                        )
                            ?: stringResource(R.string.unknown)),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}