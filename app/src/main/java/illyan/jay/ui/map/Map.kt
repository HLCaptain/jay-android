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
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.scalebar.scalebar
import illyan.jay.R
import illyan.jay.ui.navigation.model.Place

val ButeK = Place(
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
    styleUri: () -> String = { Style.OUTDOORS },
    onMapFullyLoaded: (MapView) -> Unit = {},
    onMapInitialized: (MapView) -> Unit = {},
    resourceOptions: ResourceOptions = MapInitOptions.getDefaultResourceOptions(context),
    mapOptions: MapOptions = MapInitOptions.getDefaultMapOptions(context),
    cameraOptionsBuilder: CameraOptions.Builder = CameraOptions.Builder()
        .center(
            Point.fromLngLat(
                ButeK.longitude,
                ButeK.latitude
            )
        )
        .zoom(4.0),
) {
    val options = MapInitOptions(
        context = context,
        resourceOptions = resourceOptions,
        styleUri = styleUri(),
        mapOptions = mapOptions,
        cameraOptions = cameraOptionsBuilder.build(),
    )
    val map = remember {
        val initializedMap = MapView(context, options)
        onMapInitialized(initializedMap)
        initializedMap
    }
    MapboxMapContainer(
        modifier = modifier,
        map = map,
        onMapFullyLoaded,
    )
}

@Composable
private fun MapboxMapContainer(
    modifier: Modifier,
    map: MapView,
    onMapFullyLoaded: (MapView) -> Unit = {},
) {
    DisposableEffect(Unit) {
        val onMapLoadedListener = { _: MapLoadedEventData -> onMapFullyLoaded(map) }
        map.getMapboxMap().addOnMapLoadedListener(onMapLoadedListener)
        onDispose { map.getMapboxMap().removeOnMapLoadedListener(onMapLoadedListener) }
    }
    AndroidView(
        modifier = modifier,
        factory = { map }
    ) {
        it.gestures.scrollEnabled = true
        // it.logo.enabled = false // Logo is enabled due to Terms of Service
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

// https://www.geeksforgeeks.org/how-to-convert-a-vector-to-bitmap-in-android/
fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
