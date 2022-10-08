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

import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Expand
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.ui.NavGraphs
import illyan.jay.ui.destinations.FreeDriveScreenDestination
import illyan.jay.ui.destinations.MenuListDestination
import illyan.jay.ui.destinations.NavigationScreenDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.sendBroadcast
import illyan.jay.ui.map.ButeK
import illyan.jay.ui.menu.MenuViewModel.Companion.ACTION_QUERY_PLACE
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.search.SearchViewModel.Companion.KeyPlaceQuery
import timber.log.Timber

@RootNavGraph
@NavGraph
annotation class MenuNavGraph(
    val start: Boolean = false,
)

val MenuItemPadding = 6.dp
val ListMaxHeight = 384.dp
val ListMinHeight = 128.dp

@MenuNavGraph(start = true)
@Destination
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    DisposableEffect(key1 = true) {
        viewModel.load {
            destinationsNavigator.navigate(
                NavigationScreenDestination(it, 12.0)
            )
        }
        onDispose { viewModel.dispose() }
    }
    DestinationsNavHost(
        modifier = modifier,
        navGraph = NavGraphs.menu,
        startRoute = MenuListDestination
    )
}

@OptIn(ExperimentalFoundationApi::class)
@MenuNavGraph
@Destination
@Composable
fun MenuList(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val gridState = rememberLazyStaggeredGridState()
    val context = LocalContext.current
    val localBroadcastManager = LocalBroadcastManager.getInstance(context)
    var menuShouldBeBigger by remember { mutableStateOf(false) }
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = Modifier
            .heightIn(
                min = ListMinHeight,
                max = ListMaxHeight,
            )
            .padding(
                start = MenuItemPadding,
                end = MenuItemPadding,
                // TODO: remove additional bottom padding after getting contentPadding working
                bottom = RoundedCornerRadius + MenuItemPadding
            )
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            ),
        contentPadding = PaddingValues(
            top = MenuItemPadding,
            // FIXME: bottom content padding is not applied
            bottom = RoundedCornerRadius + MenuItemPadding
        ),
        state = gridState
    ) {
        // TODO: fill up list with content
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "Navigate to BME",
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
                color = Color.Cyan
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "Start free-drive navigation",
                icon = Icons.Rounded.Navigation,
                onClick = {
                    destinationsNavigator.navigate(FreeDriveScreenDestination)
                },
                color = Color.Cyan
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "This is a title with longer text"
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "Lol, this is a menu item, which is really cool"
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "Nice"
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "Bruh, press this menu item or dont, I dont care, but hey, have a great day"
            )
        }
        item {
            MenuItemCard(
                modifier = Modifier.padding(MenuItemPadding),
                title = "Make the menu bigger",
                color = Color.Magenta,
                icon = Icons.Rounded.Expand
            ) {
                menuShouldBeBigger = !menuShouldBeBigger
            }
        }
    }
    Spacer(modifier = Modifier.height(RoundedCornerRadius))
    val spacerHeight by animateDpAsState(targetValue = if (menuShouldBeBigger) 800.dp else 0.dp)
    Spacer(modifier = Modifier.height(spacerHeight))
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCard(
    modifier: Modifier = Modifier,
    title: String = "Menu Item Title",
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
                    contentDescription = "Menu Item Icon"
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

    DisposableEffect(key1 = backPressedDispatcher) {
        backPressedDispatcher?.addCallback(backCallback)

        onDispose {
            backCallback.remove()
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

    DisposableEffect(key1 = onBackInvokedDispatcher) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            PRIORITY_DEFAULT,
            backCallback
        )

        onDispose {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(backCallback)
        }
    }
}
