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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import illyan.jay.BuildConfig

@Composable
fun MapboxMap(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    lat: Double = 47.481491,
    lng: Double = 19.056219,
    zoom: Double = 12.0,
    styleUri: String = Style.OUTDOORS,
    onMapLoaded: (MapView) -> Unit = {}
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
        styleUri = styleUri
    )
}

@Composable
private fun MapboxMapContainer(
    modifier: Modifier,
    map: MapView,
    lat: Double,
    lng: Double,
    zoom: Double,
    styleUri: String
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
