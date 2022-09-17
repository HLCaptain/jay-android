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

package illyan.jay.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import illyan.jay.BuildConfig
import illyan.jay.ui.navigation.model.Place

val ButeK = Place(
    latitude = 47.481491,
    longitude = 19.056219
)

@Composable
fun MapboxMap(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    lat: Double = ButeK.latitude,
    lng: Double = ButeK.longitude,
    zoom: Double = 12.0,
    styleUri: String = Style.OUTDOORS,
    onMapLoaded: (MapView) -> Unit = {},
    centerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val opt = MapInitOptions(
        context,
        resourceOptions = ResourceOptions.Builder()
            .accessToken(BuildConfig.MapboxAccessToken)
            .build()
    )
    val map = remember {
        val mapLoaded = MapView(context, opt)
        onMapLoaded(mapLoaded)
        mapLoaded
    }
    MapboxMapContainer(
        modifier = modifier,
        map = map,
        lat = lat,
        lng = lng,
        zoom = zoom,
        styleUri = styleUri,
        centerPadding = centerPadding
    )
}

@Composable
private fun MapboxMapContainer(
    modifier: Modifier,
    map: MapView,
    lat: Double,
    lng: Double,
    zoom: Double,
    styleUri: String,
    centerPadding: PaddingValues
) {
    val (isMapInitialized, setMapInitialized) = remember(map) { mutableStateOf(false) }
    LaunchedEffect(map, isMapInitialized) {
        if (!isMapInitialized) {
            val mapboxMap = map.getMapboxMap()
            mapboxMap.loadStyleUri(styleUri)
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(lng, lat))
                    .zoom(zoom)
                    .padding(centerPadding)
                    .build()
            )
            setMapInitialized(true)
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { map }
    ) {
        it.gestures.scrollEnabled = true
        it.logo.enabled = false
        it.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(lng, lat))
                .zoom(zoom)
                .build()
        )
    }
}

fun CameraOptions.Builder.padding(
    paddingValues: PaddingValues,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr
): CameraOptions.Builder {
    return padding(
        EdgeInsets(
            paddingValues.calculateTopPadding().value.toDouble(),
            paddingValues.calculateLeftPadding(layoutDirection).value.toDouble(),
            paddingValues.calculateBottomPadding().value.toDouble(),
            paddingValues.calculateRightPadding(layoutDirection).value.toDouble()
        )
    )
}

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
