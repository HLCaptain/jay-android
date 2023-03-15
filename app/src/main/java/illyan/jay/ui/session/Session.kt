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

import android.graphics.Color.parseColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.MediumCircularProgressIndicator
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.mapView
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.home.tryFlyToPath
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.session.model.UiLocation
import illyan.jay.ui.session.model.UiSession
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.mapMarkers
import illyan.jay.ui.theme.signatureBlue
import illyan.jay.util.format
import timber.log.Timber
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.TimeZone

val DefaultScreenOnSheetPadding = PaddingValues(
    top = MenuItemPadding * 2,
    bottom = RoundedCornerRadius + MenuItemPadding * 2
)

@OptIn(ExperimentalMaterialApi::class)
@MenuNavGraph
@Destination
@Composable
fun SessionScreen(
    sessionUUID: String,
    viewModel: SessionViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    LaunchedEffect(Unit) {
        viewModel.load(sessionUUID)
    }
    var sheetHeightNotSet by remember { mutableStateOf(true) }
    var flownToPath by remember { mutableStateOf(false) }
    val path by viewModel.path.collectAsStateWithLifecycle()
    var fakeStateChangeStopped by remember { mutableStateOf(false) }
    LaunchedEffect(sheetState.isAnimationRunning) {
        if (fakeStateChangeStopped) {
            sheetHeightNotSet = sheetState.isAnimationRunning
        }
        if (!sheetState.isAnimationRunning) {
            fakeStateChangeStopped = true
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
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val mapMarkers by mapMarkers.collectAsStateWithLifecycle()
    DisposableEffect(
        path
    ) {
        val points = path?.map {
            Point.fromLngLat(it.latLng.longitude, it.latLng.latitude)
        } ?: emptyList()
        val sortedLocations = path?.sortedBy { it.zonedDateTime }?.map { it.latLng }
        val startPoint = sortedLocations?.first()
        val endPoint = sortedLocations?.last()
        val annotationsPlugin = mapView.value?.annotations
        val lineWidth = (2.dp * density).value.toDouble()
        // FIXME: extract source and layer id functionality to a different class
        val pointAnnotationManager = annotationsPlugin?.createPointAnnotationManager(
            annotationConfig = AnnotationConfig(
                sourceId = "MARKER_SOURCE_ID",
                layerId = "MARKER_LAYER_ID",
            )
        )
        // Used reference from https://github.com/mapbox/mapbox-maps-android/blob/e8becd34eede7049feeaa4a8d3cca1b72be9f1bb/app/src/main/java/com/mapbox/maps/testapp/examples/linesandpolygons/LineGradientActivity.kt#L32
        mapView.value?.getMapboxMap()?.getStyle { style ->
            Timber.d("Adding source")
            style.addSource(
                geoJsonSource(id = "ROUTE_LINE_SOURCE_ID") {
                    feature(Feature.fromGeometry(LineString.fromLngLats(points)))
                    lineMetrics(true)
                }
            )
            style.addLayerBelow(
                layer = lineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID") {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineWidth(lineWidth)
                    lineGradient(
                        interpolate {
                            linear()
                            lineProgress()
                            stop(0.0) { color(parseColor("#00ff8c")) }
                            stop(1.0) { color(MaterialTheme.signatureBlue.toArgb()) }
                        }
                    )
                },
                below = "MARKER_LAYER_ID"
            )
        }
        startPoint?.let { point ->
            mapMarkers?.let {
                pointAnnotationManager?.create(
                    option = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(point.longitude, point.latitude))
                        .withIconImage(it.getPathStartBitmap(context))
                        .withIconAnchor(IconAnchor.BOTTOM)
                )
            }
        }
        endPoint?.let { point ->
            mapMarkers?.let {
                pointAnnotationManager?.create(
                    option = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(point.longitude, point.latitude))
                        .withIconImage(it.getPathEndBitmap(context))
                        .withIconAnchor(IconAnchor.BOTTOM)
                )
            }
        }
        onDispose {
            annotationsPlugin?.removeAnnotationManager(pointAnnotationManager!!)
            mapView.value?.getMapboxMap()?.getStyle {
                it.removeStyleLayer("ROUTE_LAYER_ID")
                it.removeStyleSource("ROUTE_LINE_SOURCE_ID")
            }
        }
    }
    val session by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailsScreen(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DefaultScreenOnSheetPadding),
        session = session,
        path = path,
    )
}

@Composable
fun SessionDetailsScreen(
    modifier: Modifier = Modifier,
    session: UiSession? = null,
    path: List<UiLocation>? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(start = MenuItemPadding * 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Crossfade(
                    modifier = Modifier.animateContentSize(),
                    targetState = session?.startLocationName
                ) {
                    Text(
                        text = it ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ArrowRightAlt, contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Crossfade(
                    modifier = Modifier.animateContentSize(),
                    targetState = (session?.endDateTime == null) to session?.endLocationName
                ) {
                    if (it.first) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz, contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    } else {
                        Text(
                            text = it.second ?: stringResource(R.string.unknown),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = path == null || session == null
            ) {
                MediumCircularProgressIndicator(
                    modifier = Modifier.padding(end = MenuItemPadding * 2)
                )
            }
        }
        val unknown = stringResource(R.string.unknown)
        val ongoing = stringResource(R.string.ongoing)
        val configuration = LocalConfiguration.current
        // Needed ConfigurationCompat to work under API 24
        val locale = ConfigurationCompat.getLocales(configuration)[0]
        val zone = TimeZone.getDefault()
        val dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(zone.toZoneId())
        SessionDetailsList(
            contentPadding = PaddingValues(horizontal = MenuItemPadding * 2),
            details = listOf(
                stringResource(R.string.distance) to if (session?.totalDistance == null) {
                    unknown
                } else {
                    "${session.totalDistance
                        .div(1000)
                        .toBigDecimal()
                        .setScale(2, RoundingMode.FLOOR)
                    } ${stringResource(R.string.kilometers)}"
                },
                stringResource(R.string.duration) to session?.duration?.format(
                    separator = " ",
                    second = stringResource(R.string.second_short),
                    minute = stringResource(R.string.minute_short),
                    hour = stringResource(R.string.hour_short),
                    day = stringResource(R.string.day_short)
                ),
                stringResource(R.string.start_date) to session?.startDateTime?.format(
                    if (locale != null) {
                        dateTimeFormatter.withLocale(locale)
                    } else {
                        dateTimeFormatter
                    }
                ),
                stringResource(R.string.end_date) to (session?.endDateTime?.format(
                    if (locale != null) {
                        dateTimeFormatter.withLocale(locale)
                    } else {
                        dateTimeFormatter
                    }
                ) ?: ongoing),
                stringResource(R.string.start_location) to if (session?.startCoordinate != null) {
                    session.startCoordinate.run {
                        "(${
                            latitude
                                .toBigDecimal()
                                .setScale(6, RoundingMode.HALF_UP)
                        }, ${
                            longitude
                                .toBigDecimal()
                                .setScale(6, RoundingMode.HALF_UP)
                        })"
                    }
                } else {
                    unknown
                },
                stringResource(R.string.end_location) to if (session?.endCoordinate != null) {
                    session.endCoordinate.run {
                        "(${
                            latitude
                                .toBigDecimal()
                                .setScale(6, RoundingMode.HALF_UP)
                        }, ${
                            longitude
                                .toBigDecimal()
                                .setScale(6, RoundingMode.HALF_UP)
                        })"
                    }
                } else {
                    unknown
                },
                stringResource(R.string.session_id) to session?.uuid
            ),
        )
    }
}

@Composable
fun SessionDetailsList(
    details: List<Pair<String, String?>> = emptyList(),
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyRow(
        contentPadding = contentPadding
    ) {
        item {
            Column {
                details.forEach {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = it.first,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = it.second ?: stringResource(R.string.unknown),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun SessionDetailsScreenPreview(
    session: UiSession? = null,
    path: List<UiLocation>? = null,
) {
    JayTheme {
        SessionDetailsScreen(
            session = session,
            path = path,
        )
    }
}
