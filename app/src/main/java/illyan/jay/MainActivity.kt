/*
 * Copyright (c) 2022-2024 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import dagger.hilt.android.AndroidEntryPoint
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.ui.components.PreviewAccessibility
import illyan.jay.ui.theme.DefaultDestinationTransitions
import illyan.jay.ui.theme.JayThemeWithViewModel
import illyan.jay.util.MapboxExceptionHandler
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authInteractor: AuthInteractor

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                MapboxNavigationApp.attach(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                MapboxNavigationApp.detach(owner)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(applicationContext).build()
            }
        }

        // Catches only OpenGL errors from MapboxRenderThread
        val mapboxExceptionHandler = MapboxExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(mapboxExceptionHandler)

        setContent {
            var mapboxMapViewNotSupported by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                mapboxExceptionHandler.openGlNotSupportedCallback = {
                    mapboxMapViewNotSupported = true
                }
            }
            JayThemeWithViewModel {
                CompositionLocalProvider(LocalMapboxNotSupported provides mapboxMapViewNotSupported) {
                    MainScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

val LocalMapboxNotSupported = compositionLocalOf { false }

@PreviewAccessibility
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    DestinationsNavHost(
        navGraph = NavGraphs.root,
        modifier = modifier,
        defaultTransitions = DefaultDestinationTransitions
    )
}
