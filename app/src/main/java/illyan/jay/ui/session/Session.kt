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

package illyan.jay.ui.session

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
import illyan.jay.ui.home.mapView
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.home.tryFlyToPath
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.sessions.DefaultScreenOnSheetPadding
import illyan.jay.util.format
import illyan.jay.util.plus

@OptIn(ExperimentalMaterialApi::class)
@MenuNavGraph
@Destination
@Composable
fun SessionScreen(
    sessionId: Long,
    viewModel: SessionViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    LaunchedEffect(Unit) {
        viewModel.load(sessionId)
    }
    val session by viewModel.session.collectAsState()
    var sheetHeightNotSet by remember { mutableStateOf(true) }
    var sessionLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(
        session,
        sheetState.isAnimationRunning
    ) {
        session?.let {
            if (!sessionLoaded) {
                tryFlyToPath(
                    path = it.locations.map { location ->
                        Point.fromLngLat(
                            location.latLng.longitude,
                            location.latLng.latitude
                        )
                    },
                    extraCondition = { !sheetHeightNotSet },
                    onFly = { sessionLoaded = true }
                )
            }
            sheetHeightNotSet = false
        }
    }
    DisposableEffect(
        session
    ) {
        val annotationsPlugin = mapView.value?.annotations
        val polylineAnnotationManager = annotationsPlugin?.createPolylineAnnotationManager()
        polylineAnnotationManager?.create(
            option = PolylineAnnotationOptions()
                .withPoints(
                    session?.locations?.map {
                        Point.fromLngLat(it.latLng.longitude, it.latLng.latitude)
                    } ?: emptyList()
                )
                // AzureBlue
                .withLineColor("#1b8fff")
                .withLineWidth(5.0)
        )
        onDispose {
            annotationsPlugin?.removeAnnotationManager(polylineAnnotationManager!!)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DefaultScreenOnSheetPadding + PaddingValues(bottom = MenuItemPadding)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = session?.startLocationName ?: stringResource(R.string.unknown),
                style = MaterialTheme.typography.titleLarge
            )
            Icon(imageVector = Icons.Rounded.ArrowRightAlt, contentDescription = "")
            Crossfade(targetState = session?.endDateTime == null) {
                if (it) {
                    Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = "")
                } else {
                    Text(
                        text = session?.endLocationName ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
        Column {
            Text(
                text = "${stringResource(R.string.distance)}: " +
                        "${session?.totalDistance ?: stringResource(R.string.unknown)} " +
                        stringResource(R.string.meters)
            )
            Text(
                text = "${stringResource(R.string.duration)}: " +
                        session?.duration?.format(
                            separator = " ",
                            second = stringResource(R.string.second_short),
                            minute = stringResource(R.string.minute_short),
                            hour = stringResource(R.string.hour_short),
                            day = stringResource(R.string.day_short)
                        )
            )
        }
    }
}