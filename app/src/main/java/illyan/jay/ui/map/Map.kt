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

package illyan.jay.ui.map

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import illyan.jay.R
import illyan.jay.ui.poi.model.Place

val BmeK = Place(
    latitude = 47.481491,
    longitude = 19.056219
)

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
    resourceOptions: ResourceOptions = MapInitOptions.getDefaultResourceOptions(context),
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
        resourceOptions = resourceOptions,
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
        map.getMapboxMap().loadStyleUri(initialStyleUri)
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
        val onMapLoadedListener = { _: MapLoadedEventData -> onMapFullyLoaded(map) }
        map.getMapboxMap().addOnMapLoadedListener(onMapLoadedListener)
        map.getMapboxMap().addOnCameraChangeListener { onCameraChanged(map.getMapboxMap().cameraState) }
        onDispose { map.getMapboxMap().removeOnMapLoadedListener(onMapLoadedListener) }
    }
    val statusBarHeight = LocalDensity.current.run { WindowInsets.statusBars.getTop(this) }
    val fixedStatusBarHeight = rememberSaveable { statusBarHeight }
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

fun LocationComponentPlugin.turnOnWithDefaultPuck(
    context: Context,
) {
    if (!enabled) {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.jay_puck_transparent_background)
        enabled = true
        locationPuck = LocationPuck2D(
            topImage = drawable,
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
