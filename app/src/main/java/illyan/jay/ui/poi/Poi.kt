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

package illyan.jay.ui.poi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.LatLng
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.search.result.SearchResultType
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.mapView
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.home.tryFlyToLocation
import illyan.jay.ui.map.BmeK
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.poi.model.Place
import illyan.jay.ui.poi.model.PlaceMetadata
import illyan.jay.ui.sheet.SheetNavGraph
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.mapMarkers
import illyan.jay.util.largeTextPlaceholder
import illyan.jay.util.plus
import illyan.jay.util.textPlaceholder
import java.math.RoundingMode
import kotlin.math.roundToInt

val DefaultScreenOnSheetPaddingHorizontal = PaddingValues(
    start = MenuItemPadding * 2,
    end = MenuItemPadding * 2,
)

val DefaultScreenOnSheetPaddingVertical = PaddingValues(
    top = MenuItemPadding,
    bottom = RoundedCornerRadius + MenuItemPadding * 2,
)

val DefaultScreenOnSheetPadding =
    DefaultScreenOnSheetPaddingHorizontal + DefaultScreenOnSheetPaddingVertical

const val maxZoom = 18.0
const val largeZoom = 16.0
const val mediumZoom = 12.0
const val smallZoom = 8.0
const val verySmallZoom = 6.0
const val minZoom = 3.0


@OptIn(ExperimentalMaterialApi::class)
@SheetNavGraph
@Destination
@Composable
fun Poi(
    placeToNavigate: Place,
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: PoiViewModel = hiltViewModel(),
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    DisposableEffect(Unit) {
        viewModel.load(placeToNavigate)
        onDispose { viewModel.dispose() }
    }
    var sheetHeightNotSet by remember { mutableStateOf(true) }
    val place by viewModel.place.collectAsStateWithLifecycle()
    val placeMetadata by viewModel.placeInfo.collectAsStateWithLifecycle()
    LaunchedEffect(sheetState.isAnimationRunning) {
        sheetHeightNotSet = sheetState.isAnimationRunning
    }
    val context = LocalContext.current
    val mapMarkers by mapMarkers.collectAsStateWithLifecycle()
    val markerHeight = (36.dp * LocalDensity.current.density).value.roundToInt()
    DisposableEffect(
        place,
        placeMetadata,
        mapMarkers
    ) {
        val annotationsPlugin = mapView.value?.annotations
        val pointAnnotationManager = annotationsPlugin?.createPointAnnotationManager()
        place?.let { point ->
            mapMarkers?.let {
                pointAnnotationManager?.create(
                    option = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(point.longitude, point.latitude))
                        .withIconImage(
                            it.getPoiBitmap(
                                context = context,
                                height = markerHeight
                            )
                        )
                        .withIconAnchor(IconAnchor.BOTTOM)
                )
            }
        }
        onDispose {
            annotationsPlugin?.removeAnnotationManager(pointAnnotationManager!!)
        }
    }
    LaunchedEffect(
        sheetHeightNotSet,
        place,
    ) {
        place?.let {
            tryFlyToLocation(
                extraCondition = { !sheetHeightNotSet && viewModel.isNewPlace },
                place = it,
                zoom = when (it.type) {
                    SearchResultType.ADDRESS -> largeZoom
                    SearchResultType.NEIGHBORHOOD -> largeZoom
                    SearchResultType.POI -> largeZoom
                    SearchResultType.STREET -> largeZoom
                    SearchResultType.BLOCK -> largeZoom
                    SearchResultType.DISTRICT -> largeZoom
                    SearchResultType.POSTCODE ->  mediumZoom
                    SearchResultType.LOCALITY ->  mediumZoom
                    SearchResultType.PLACE -> mediumZoom
                    SearchResultType.COUNTRY ->  verySmallZoom
                    SearchResultType.REGION ->  minZoom
                    else -> mediumZoom
                },
                onFly = { viewModel.isNewPlace = false }
            )
        }
    }
    val verticalPadding = DefaultScreenOnSheetPaddingVertical
    Column(
        modifier = Modifier
            .padding(verticalPadding)
            .fillMaxWidth()
    ) {
        val shouldShowAddress by viewModel.shouldShowAddress.collectAsStateWithLifecycle()
        PoiScreen(
            modifier = Modifier.fillMaxWidth(),
            placeMetadata = placeMetadata,
            place = place,
            shouldShowAddress = shouldShowAddress,
            isLoading = placeMetadata == null,
        )
    }
}

@Composable
fun PoiScreen(
    modifier: Modifier = Modifier,
    placeMetadata: PlaceMetadata? = null,
    place: Place? = null,
    shouldShowAddress: Boolean = true,
    isLoading: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val horizontalPadding = DefaultScreenOnSheetPaddingHorizontal + PaddingValues(
            start = 2.dp,
            end = 2.dp,
        )
        Text(
            modifier = Modifier
                .padding(horizontalPadding)
                .largeTextPlaceholder(place == null),
            text = place?.name ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        AnimatedVisibility(
            modifier = Modifier
                .padding(horizontalPadding)
                .textPlaceholder(isLoading),
            visible = shouldShowAddress && placeMetadata?.address != null
        ) {
            Text(
                text = placeMetadata?.address ?: stringResource(R.string.unknown),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        AnimatedVisibility(visible = !placeMetadata?.categories.isNullOrEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = horizontalPadding
            ) {
                items(placeMetadata?.categories ?: emptyList()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                start = 6.dp,
                                end = 6.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                            text = it.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
        Text(
            modifier = Modifier
                .padding(horizontalPadding)
                .textPlaceholder(isLoading),
            text = placeMetadata?.latLng?.run {
                latitude
                    .toBigDecimal()
                    .setScale(6, RoundingMode.HALF_UP)
                    .toString() +
                        " " +
                        longitude
                            .toBigDecimal()
                            .setScale(6, RoundingMode.HALF_UP)
                            .toString()
            } ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDarkTheme
@Composable
private fun PoiScreenPreview() {
    JayTheme {
        PoiScreen(
            modifier = Modifier.fillMaxWidth(),
            place = BmeK,
            shouldShowAddress = true,
            placeMetadata = PlaceMetadata(
                categories = listOf("School", "University", "Historical"),
                latLng = LatLng(BmeK.latitude, BmeK.longitude),
                address = "Budapest, XI. District, BME K Épület"
            )
        )
    }
}
