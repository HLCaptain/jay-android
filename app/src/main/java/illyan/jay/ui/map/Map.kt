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

package illyan.jay.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.common.Cancelable
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapLoaded
import com.mapbox.maps.MapOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import illyan.jay.LocalMapboxNotSupported
import illyan.jay.R
import illyan.jay.ui.components.PreviewAll
import illyan.jay.ui.poi.model.Place
import timber.log.Timber

val BmeK = Place(
    latitude = 47.481491,
    longitude = 19.056219
)

@SuppressLint("IncorrectNumberOfArgumentsInExpression")
val puckScaleExpression = interpolate {
    linear()
    zoom()
    stop {
        literal(0.0)
        literal(0.6)
    }
    stop {
        literal(20.0)
        literal(1.0)
    }
}.toJson()

@Composable
fun MapboxMap(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    initialStyleUri: String = Style.OUTDOORS,
    onMapFullyLoaded: (MapView) -> Unit = {},
    onMapInitialized: (MapView) -> Unit = {},
    mapOptions: MapOptions = MapInitOptions.getDefaultMapOptions(context),
    cameraOptionsBuilder: CameraOptions.Builder = CameraOptions.Builder()
        .center(
            Point.fromLngLat(
                BmeK.longitude,
                BmeK.latitude
            )
        )
        .zoom(4.0),
) {
    val cameraOptions by remember { mutableStateOf(cameraOptionsBuilder.build()) }
    var cameraCenter by rememberSaveable { mutableStateOf(cameraOptions.center) }
    var cameraBearing by rememberSaveable { mutableStateOf(cameraOptions.bearing) }
    var cameraZoom by rememberSaveable { mutableStateOf(cameraOptions.zoom) }
    var cameraPitch by rememberSaveable { mutableStateOf(cameraOptions.pitch) }
    var cameraPadding by rememberSaveable { mutableStateOf(cameraOptions.padding) }

    val options = MapInitOptions(
        context = context,
        styleUri = initialStyleUri,
        mapOptions = mapOptions,
        cameraOptions = CameraOptions.Builder()
            .center(cameraCenter)
            .bearing(cameraBearing)
            .zoom(cameraZoom)
            .pitch(cameraPitch)
            .padding(cameraPadding)
            .build(),
    )

    val map = remember {
        val initializedMap = MapView(
            context = context,
            mapInitOptions = options,
        )
        onMapInitialized(initializedMap)
        initializedMap
    }

    LaunchedEffect(initialStyleUri) {
        map.mapboxMap.loadStyleUri(initialStyleUri)
    }

    MapboxMapContainer(
        modifier = modifier,
        map = map,
        onMapFullyLoaded = onMapFullyLoaded,
        onCameraChanged = {
            cameraCenter = it.center
            cameraBearing = it.bearing
            cameraPadding = it.padding
            cameraZoom = it.zoom
            cameraPitch = it.pitch
        }
    )
}

@Composable
private fun MapboxMapContainer(
    modifier: Modifier,
    map: MapView,
    onMapFullyLoaded: (MapView) -> Unit = {},
    onCameraChanged: (CameraState) -> Unit = {}
) {
    DisposableEffect(Unit) {
        val onMapLoadedListener = { _: MapLoaded -> onMapFullyLoaded(map) }
        val cancelable = mutableListOf<Cancelable>()
        map.mapboxMap.subscribeMapLoaded(onMapLoadedListener)
        map.mapboxMap.subscribeCameraChanged { onCameraChanged(map.mapboxMap.cameraState) }
        onDispose { cancelable.forEach { it.cancel() } }
    }
    val statusBarHeight = LocalDensity.current.run { WindowInsets.statusBars.getTop(this) }
    val fixedStatusBarHeight = rememberSaveable(statusBarHeight) { statusBarHeight }
    val isMapNotSupported = LocalMapboxNotSupported.current
    LaunchedEffect(isMapNotSupported) {
        Timber.d("MapboxMapContainer: isMapNotSupported: $isMapNotSupported")
    }
    Crossfade(
        modifier = modifier,
        targetState = isMapNotSupported,
        label = "MapboxMap"
    ) { notSupported ->
        if (notSupported) {
            LaunchedEffect(Unit) {
                onMapFullyLoaded(map)
                map.visibility = View.GONE
            }
            MapsNotSupportedCard(modifier = modifier)
        } else {
            AndroidView(
                modifier = modifier,
                factory = { map }
            ) {
                it.logo.position = 0
                it.logo.marginTop = fixedStatusBarHeight.toFloat()
                it.attribution.position = 0
                it.attribution.marginTop = fixedStatusBarHeight.toFloat()
                it.attribution.marginLeft = 240f
                it.compass.marginTop = fixedStatusBarHeight.toFloat()
                it.gestures.scrollEnabled = true
                it.scalebar.isMetricUnits = true // TODO: set this in settings or based on location, etc.
                it.scalebar.enabled = false // TODO: enable it later if needed (though pay attention to ugly design)
            }
        }
    }
}

@Composable
fun MapsNotSupportedCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Rounded.BrokenImage,
                    contentDescription = "Mapbox Map Error Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.mapbox_map_initialization_problem),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Mapbox Map Error Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.mapbox_map_unsupported_opengl),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(R.string.mapbox_map_problem_not_affecting_jay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@PreviewAll
@Composable
fun PreviewMapsNotSupportedCard() {
    MapsNotSupportedCard()
}

fun LocationComponentPlugin.turnOnWithDefaultPuck() {
    if (!enabled) {
        enabled = true
        locationPuck = LocationPuck2D(
            topImage = ImageHolder.Companion.from(R.drawable.jay_puck_transparent_background),
            scaleExpression = puckScaleExpression
        )
    }
}

fun CameraOptions.Builder.padding(
    paddingValues: PaddingValues,
    context: Context,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): CameraOptions.Builder {
    val pixelDensity = context.resources.displayMetrics.density
    return padding(
        paddingValues.toEdgeInsets(
            density = pixelDensity,
            layoutDirection = layoutDirection
        )
    )
}

fun CameraOptions.Builder.padding(
    paddingValues: PaddingValues,
    density: Density,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): CameraOptions.Builder {
    return padding(paddingValues, density.density, layoutDirection)
}

fun CameraOptions.Builder.padding(
    paddingValues: PaddingValues,
    density: Float,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): CameraOptions.Builder {
    return padding(paddingValues.toEdgeInsets(density, layoutDirection))
}

fun PaddingValues.toEdgeInsets(
    density: Float,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
) = EdgeInsets(
    calculateTopPadding().value.toDouble() * density,
    calculateLeftPadding(layoutDirection).value.toDouble() * density,
    calculateBottomPadding().value.toDouble() * density,
    calculateRightPadding(layoutDirection).value.toDouble() * density
)
