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
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.MainActivity
import illyan.jay.R
import illyan.jay.domain.model.Theme
import illyan.jay.ui.components.PreviewThemesScreensFonts
import illyan.jay.ui.destinations.FreeDriveDestination
import illyan.jay.ui.destinations.SessionsDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.isSearching
import illyan.jay.ui.home.sheetState
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.LocalTheme
import illyan.jay.ui.theme.statefulColorScheme
import illyan.jay.ui.theme.surfaceColorAtElevation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@RootNavGraph
@NavGraph
annotation class MenuNavGraph(
    val start: Boolean = false,
)

val MenuItemPadding = 6.dp

val DefaultContentPadding = PaddingValues(
    bottom = RoundedCornerRadius + MenuItemPadding * 2
)

val DefaultScreenOnSheetPadding = PaddingValues(
    top = MenuItemPadding * 2,
    start = MenuItemPadding * 2,
    end = MenuItemPadding * 2,
    bottom =  RoundedCornerRadius + MenuItemPadding * 2
)

@MenuNavGraph(start = true)
@Destination
@Composable
fun MenuScreen(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    BackPressHandler {
        Timber.d("Intercepted back press!")
        (context as Activity).moveTaskToBack(false)
    }
    val localizedNameOfBme = stringResource(R.string.bme)
    MenuContent(
        onNavigateToBme = {
            viewModel.onClickNavigateToBmeButton(localizedNameOfBme)
        },
        onFreeDrive = {
            viewModel.onClickFreeDriveButton()
            destinationsNavigator.navigate(FreeDriveDestination)
        },
        onSessions = {
            viewModel.onClickSessionsButton()
            destinationsNavigator.navigate(SessionsDestination)
        },
        onToggleTheme = viewModel::toggleTheme,
    )
}

@Composable
fun MenuContent(
    onNavigateToBme: () -> Unit = {},
    onFreeDrive: () -> Unit = {},
    onSessions: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
) {
    val gridState = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        verticalItemSpacing = MenuItemPadding * 2,
        horizontalArrangement = Arrangement.spacedBy(MenuItemPadding * 2),
        //contentPadding = DefaultContentPadding, // TODO: include this when finally devs fixed contentPadding on StaggeredGrids
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            .padding(DefaultScreenOnSheetPadding),
        state = gridState
    ) {
        item {
            MenuItemCard(
                title = stringResource(R.string.navigate_to_bme),
                icon = Icons.Rounded.TravelExplore,
                onClick = onNavigateToBme,
            )
        }
        item {
            MenuItemCard(
                title = stringResource(R.string.free_drive),
                icon = Icons.Rounded.Navigation,
                onClick = onFreeDrive,
            )
        }
        item {
            MenuItemCard(
                title = stringResource(R.string.sessions),
                icon = Icons.Rounded.Route,
                onClick = onSessions,
            )
        }
        item {
            val theme = LocalTheme.current
            MenuItemCard(
                title = stringResource(R.string.toggle_theme),
                icon = when (theme) {
                    Theme.Light -> Icons.Rounded.LightMode
                    Theme.Dark -> Icons.Rounded.DarkMode
                    Theme.System -> Icons.Rounded.Settings
                    null -> null
                },
                onClick = onToggleTheme,
            )
        }
    }
}

@PreviewThemesScreensFonts
@Composable
private fun MenuContentPreview() {
    JayTheme {
        MenuContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCard(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.menu_item_title),
    icon: ImageVector? = null,
    cardColors: CardColors = (@Composable {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.statefulColorScheme.surfaceColorAtElevation(1.dp),
            contentColor = MaterialTheme.statefulColorScheme.onSurface
        )
    })(),
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = cardColors,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Crossfade(targetState = icon) {
                if (it != null) {
                    Icon(
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 4.dp,
                            top = 12.dp,
                            bottom = 12.dp
                        ),
                        imageVector = it,
                        contentDescription = stringResource(R.string.menu_item_icon)
                    )
                } else {
                    Spacer(modifier = Modifier.width(12.dp))
                }
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
        if (sheetState.isCollapsed) (context as MainActivity).moveTaskToBack(false)
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
