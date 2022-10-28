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

package illyan.jay.ui.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.MainActivity
import illyan.jay.ui.home.asString
import illyan.jay.ui.home.isCollapsedOrWillBe
import illyan.jay.ui.home.isSearching
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.home.tryFlyToLocation
import illyan.jay.ui.menu.BackPressHandler
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.sheet.SheetNavGraph
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@SheetNavGraph
@Destination
@Composable
fun NavigationScreen(
    place: Place,
    zoom: Double = 6.0,
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: NavigationViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    BackPressHandler {
        Timber.d("Handling back press in Navigation!")
        // If searching and back is pressed, close the sheet instead of the app
        if (sheetState.isCollapsedOrWillBe()) (context as MainActivity).moveTaskToBack(false)
        if (isSearching) {
            coroutineScope.launch {
                // This call will automatically unfocus the textfield
                // because BottomSearchBar listens on sheet changes.
                sheetState.collapse()
            }
        } else {
            destinationsNavigator.navigateUp()
        }
    }
    DisposableEffect(Unit) {
        viewModel.load(place)
        onDispose { viewModel.dispose() }
    }
    var sheetHeightNotSet by remember { mutableStateOf(true) }
    LaunchedEffect(
        sheetState.isAnimationRunning,
        viewModel.place,
    ) {
        tryFlyToLocation(
            extraCondition = { !sheetHeightNotSet && viewModel.isNewPlace },
            place = viewModel.place,
            zoom = zoom
        ) { viewModel.isNewPlace = false }
        sheetHeightNotSet = false
    }
    Text(
        text = "Sheet state:\n${sheetState.asString()}"
    )
}
