package illyan.jay.ui.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import illyan.jay.BuildConfig
import kotlinx.coroutines.launch

@Composable
fun MapboxMap(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    lat: Double = 47.481491,
    lng: Double = 19.056219,
    zoom: Double = 12.0,
    styleUri: String = Style.OUTDOORS
) {
    val opt = MapInitOptions(
        context,
        plugins = emptyList(),
        resourceOptions = ResourceOptions.Builder()
            .accessToken(BuildConfig.MapboxAccessToken)
            .build()
    )
    val map = remember { MapView(context, opt) }
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
            mapboxMap.loadStyleUri(styleUri) {
                mapboxMap.centerTo(lat = lat, lng = lng, zoom = zoom)
                setMapInitialized(true)
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    AndroidView(
        modifier = modifier,
        factory = { map }
    ) {
        coroutineScope.launch {
            it.getMapboxMap().centerTo(lat = lat, lng = lng, zoom = zoom)
        }
    }
}

fun MapboxMap.centerTo(
    lat: Double,
    lng: Double,
    zoom: Double
) {
    val point = Point.fromLngLat(lng, lat)

    val camera = CameraOptions.Builder()
        .center(point)
        .zoom(zoom)
        .build()

    setCamera(camera)
}
