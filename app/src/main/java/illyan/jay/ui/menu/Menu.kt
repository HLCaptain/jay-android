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

package illyan.jay.ui.menu

import android.app.Activity
import android.content.Context
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.MainActivity
import illyan.jay.R
import illyan.jay.ui.destinations.FreeDriveScreenDestination
import illyan.jay.ui.destinations.SessionsScreenDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.isSearching
import illyan.jay.ui.home.sendBroadcast
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.map.ButeK
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.search.SearchViewModel.Companion.KeyPlaceQuery
import illyan.jay.ui.sheet.SheetViewModel.Companion.ACTION_QUERY_PLACE
import illyan.jay.ui.theme.SignatureTone95
import illyan.jay.util.isCollapsedOrWillBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@RootNavGraph
@NavGraph
annotation class MenuNavGraph(
    val start: Boolean = false,
)

val MenuItemPadding = 6.dp
val ListMaxHeight = 384.dp
val ListMinHeight = 128.dp

val DefaultContentPadding = PaddingValues(
    bottom = MenuItemPadding + RoundedCornerRadius
)

val DefaultScreenOnSheetPadding = PaddingValues(
    top = MenuItemPadding,
    start = MenuItemPadding,
    end = MenuItemPadding,
    bottom = RoundedCornerRadius
)

@OptIn(ExperimentalFoundationApi::class)
@MenuNavGraph(start = true)
@Destination
@Composable
fun MenuScreen(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val context = LocalContext.current
    BackPressHandler {
        Timber.d("Intercepted back press!")
        (context as Activity).moveTaskToBack(false)
    }
    val gridState = rememberLazyStaggeredGridState()
    val localBroadcastManager = LocalBroadcastManager.getInstance(context)
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = Modifier
            .heightIn(
                min = ListMinHeight,
                max = ListMaxHeight,
            )
            .padding(DefaultScreenOnSheetPadding)
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            ),
        contentPadding = DefaultContentPadding,
        state = gridState
    ) {
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = stringResource(R.string.navigate_to_bme),
                icon = Icons.Rounded.Navigation,
                onClick = {
                    localBroadcastManager.sendBroadcast(
                        Place(
                            latitude = ButeK.latitude,
                            longitude = ButeK.longitude
                        ),
                        KeyPlaceQuery,
                        ACTION_QUERY_PLACE
                    )
                },
                color = SignatureTone95
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = stringResource(R.string.start_free_drive_navigation),
                icon = Icons.Rounded.Navigation,
                onClick = {
                    destinationsNavigator.navigate(FreeDriveScreenDestination)
                },
                color = SignatureTone95
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = stringResource(R.string.sessions),
                icon = Icons.Rounded.Route,
                onClick = {
                    destinationsNavigator.navigate(SessionsScreenDestination)
                },
                color = SignatureTone95
            )
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCard(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.menu_item_title),
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {},
) {
    // Navigate to appropriate screen in whatever
    val cardColors = CardDefaults.cardColors(
        containerColor = color
    )
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = cardColors
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 4.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    ),
                    imageVector = icon,
                    contentDescription = stringResource(R.string.menu_item_icon)
                )
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                modifier = Modifier
                    .padding(
                        start = 4.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ),
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun BackPressHandler(
    backPressedDispatcher: OnBackPressedDispatcher? =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    customDisposableEffectKey: Any? = null,
    isEnabled: () -> Boolean = { true },
    onBackPressed: () -> Unit,
) {
    val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Timber.d("Intercepted back press!")
                currentOnBackPressed()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(backPressedDispatcher, customDisposableEffectKey) {
        if (isEnabled()) {
            backPressedDispatcher?.addCallback(lifecycleOwner, backCallback)
        }

        onDispose {
            backCallback.remove()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetScreenBackPressHandler(
    backPressedDispatcher: OnBackPressedDispatcher? =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    customDisposableEffectKey: Any? = null,
    isEnabled: () -> Boolean = { true },
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    destinationsNavigator: DestinationsNavigator,
    onBackPressed: () -> Unit = {},
) {
    BackPressHandler(
        backPressedDispatcher = backPressedDispatcher,
        customDisposableEffectKey = customDisposableEffectKey,
        isEnabled = isEnabled,
    ) {
        onBackPressed()
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
}

@RequiresApi(33)
@Composable
fun BackPressHandler(
    onBackInvokedDispatcher: OnBackInvokedDispatcher,
    onBackPressed: () -> Unit,
) {
    val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)

    val backCallback = remember {
        OnBackInvokedCallback {
            Timber.d("Intercepted back press >= API 33!")
            currentOnBackPressed()
        }
    }

    DisposableEffect(onBackInvokedDispatcher) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            PRIORITY_DEFAULT,
            backCallback
        )

        onDispose {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(backCallback)
        }
    }
}
