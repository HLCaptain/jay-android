/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.R
import illyan.jay.ui.NavGraphs
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.theme.JayTheme

@RootNavGraph
@NavGraph
annotation class SettingsNavGraph(
    val start: Boolean = false,
)

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsDialog(
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    if (isDialogOpen) {
        SettingsDialogScreen(
            modifier = Modifier
                .widthIn(max = screenWidthDp - 32.dp)
                .heightIn(max = max(200.dp, screenHeightDp - 256.dp)),
            onDialogClosed = onDialogClosed
        ) {
            DestinationsNavHost(
                modifier = Modifier.fillMaxSize(),
                navGraph = NavGraphs.settings,
                engine = rememberAnimatedNavHostEngine(
                    rootDefaultAnimations = RootNavGraphDefaultAnimations(
                        enterTransition = {
                            slideInHorizontally(tween(200)) { it / 2 } + fadeIn(tween(200))
                        },
                        exitTransition = {
                            slideOutHorizontally(tween(200)) + fadeOut(tween(200))
                        },
                        popEnterTransition = {
                            slideInHorizontally(tween(200)) + fadeIn(tween(200))
                        },
                        popExitTransition = {
                            slideOutHorizontally(tween(200)) { it / 2 } + fadeOut(tween(200))
                        }
                    ),
                )
            )
        }
    }
}

@Composable
fun SettingsDialogScreen(
    modifier: Modifier = Modifier,
    onDialogClosed: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    AlertDialog(
        modifier = modifier,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
        ),
        onDismissRequest = onDialogClosed,
        title = {
            // TODO: enable ad button on this screen (only showing one ad on this screen)
            Text(text = stringResource(id = R.string.settings))
        },
        confirmButton = {
            // TODO: Toggle Settings Sync
        },
        dismissButton = {
            // TODO: Dismiss dialog (Cancel)
        },
        text = {
            content()
        },
    )
}

@SettingsNavGraph(start = true)
@Destination
@Composable
fun SettingsScreen() {

}

@PreviewLightDarkTheme
@Composable
fun SettingsDialogScreenPreview() {
    JayTheme {
        SettingsDialogScreen {
            SettingsScreen()
        }
    }
}
