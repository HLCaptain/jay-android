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

package illyan.jay.ui.navigation

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.search.result.SearchResultType
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.mapView
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.home.tryFlyToLocation
import illyan.jay.ui.map.getBitmapFromVectorDrawable
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.navigation.model.toPoint
import illyan.jay.ui.sheet.SheetNavGraph
import illyan.jay.util.largeTextPlaceholder
import illyan.jay.util.plus
import illyan.jay.util.textPlaceholder
import java.math.RoundingMode

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
fun NavigationScreen(
    placeToNavigate: Place,
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    DisposableEffect(Unit) {
        viewModel.load(placeToNavigate)
        onDispose { viewModel.dispose() }
    }
    var sheetHeightNotSet by remember { mutableStateOf(true) }
    val place by viewModel.place.collectAsState()
    val placeInfo by viewModel.placeInfo.collectAsState()
    LaunchedEffect(sheetState.isAnimationRunning) {
        sheetHeightNotSet = sheetState.isAnimationRunning
    }
    val context = LocalContext.current
    DisposableEffect(
        place,
        placeInfo
    ) {
        if (place != null) {
            val pointAnnotationManager = mapView.value?.annotations?.createPointAnnotationManager()
            val pointAnnotationOptions = PointAnnotationOptions()
                .withIconImage(
                    getBitmapFromVectorDrawable(
                        context,
                        R.drawable.ic_jay_marker_icon_v3_round
                    )
                )
                // I know, the point annotation is fake, because it is not at
                // the placeInfo's coordinates, but instead a place's.
                .withPoint(place!!.toPoint())
            val annotation = pointAnnotationManager?.create(pointAnnotationOptions)
            onDispose { annotation?.let { pointAnnotationManager.delete(it) } }
        } else {
            onDispose {}
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
        PlaceInfoScreen(
            modifier = Modifier.fillMaxWidth(),
            viewModel = viewModel
        )
    }
}

@Composable
fun PlaceInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val placeInfo by viewModel.placeInfo.collectAsState()
    val place by viewModel.place.collectAsState()
    val shouldShowAddress by viewModel.shouldShowAddress.collectAsState()
    val isLoading = placeInfo == null
    val horizontalPadding = DefaultScreenOnSheetPaddingHorizontal + PaddingValues(
        start = 2.dp,
        end = 2.dp,
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(horizontalPadding)
                .largeTextPlaceholder(place == null),
            text = place?.name ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        AnimatedVisibility(
            modifier = Modifier
                .padding(horizontalPadding)
                .textPlaceholder(isLoading),
            visible = shouldShowAddress && placeInfo?.address != null
        ) {
            Text(
                text = placeInfo?.address?.formattedAddress() ?: stringResource(R.string.unknown),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        AnimatedVisibility(visible = !placeInfo?.categories.isNullOrEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = horizontalPadding
            ) {
                items(placeInfo?.categories ?: emptyList()) {
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
            text = placeInfo?.coordinate?.run {
                latitude()
                    .toBigDecimal()
                    .setScale(6, RoundingMode.HALF_UP)
                    .toString() +
                        " " +
                        longitude()
                            .toBigDecimal()
                            .setScale(6, RoundingMode.HALF_UP)
                            .toString()
            } ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
