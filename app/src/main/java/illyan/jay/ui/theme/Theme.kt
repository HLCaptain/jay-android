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

package illyan.jay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import illyan.jay.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.ln
import kotlin.math.roundToInt

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimary = md_theme_light_onPrimary,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondary = md_theme_light_onSecondary,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiary = md_theme_light_onTertiary,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    outline = md_theme_light_outline,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimary = md_theme_dark_onPrimary,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiary = md_theme_dark_onTertiary,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    outline = md_theme_dark_outline,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)
@Composable
fun animateColorScheme(
    targetColorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = spring(),
): StatefulColorScheme {
    val primary = animateColorAsState(targetValue = targetColorScheme.primary, animationSpec = animationSpec)
    val onPrimary = animateColorAsState(targetValue = targetColorScheme.onPrimary, animationSpec = animationSpec)
    val primaryContainer = animateColorAsState(targetValue = targetColorScheme.primaryContainer, animationSpec = animationSpec)
    val onPrimaryContainer = animateColorAsState(targetValue = targetColorScheme.onPrimaryContainer, animationSpec = animationSpec)
    val inversePrimary = animateColorAsState(targetValue = targetColorScheme.inversePrimary, animationSpec = animationSpec)
    val secondary = animateColorAsState(targetValue = targetColorScheme.secondary, animationSpec = animationSpec)
    val onSecondary = animateColorAsState(targetValue = targetColorScheme.onSecondary, animationSpec = animationSpec)
    val secondaryContainer = animateColorAsState(targetValue = targetColorScheme.secondaryContainer, animationSpec = animationSpec)
    val onSecondaryContainer = animateColorAsState(targetValue = targetColorScheme.onSecondaryContainer, animationSpec = animationSpec)
    val tertiary = animateColorAsState(targetValue = targetColorScheme.tertiary, animationSpec = animationSpec)
    val onTertiary = animateColorAsState(targetValue = targetColorScheme.onTertiary, animationSpec = animationSpec)
    val tertiaryContainer = animateColorAsState(targetValue = targetColorScheme.tertiaryContainer, animationSpec = animationSpec)
    val onTertiaryContainer = animateColorAsState(targetValue = targetColorScheme.onTertiaryContainer, animationSpec = animationSpec)
    val background = animateColorAsState(targetValue = targetColorScheme.background, animationSpec = animationSpec)
    val onBackground = animateColorAsState(targetValue = targetColorScheme.onBackground, animationSpec = animationSpec)
    val surface = animateColorAsState(targetValue = targetColorScheme.surface, animationSpec = animationSpec)
    val onSurface = animateColorAsState(targetValue = targetColorScheme.onSurface, animationSpec = animationSpec)
    val surfaceVariant = animateColorAsState(targetValue = targetColorScheme.surfaceVariant, animationSpec = animationSpec)
    val onSurfaceVariant = animateColorAsState(targetValue = targetColorScheme.onSurfaceVariant, animationSpec = animationSpec)
    val surfaceTint = animateColorAsState(targetValue = targetColorScheme.surfaceTint, animationSpec = animationSpec)
    val inverseSurface = animateColorAsState(targetValue = targetColorScheme.inverseSurface, animationSpec = animationSpec)
    val inverseOnSurface = animateColorAsState(targetValue = targetColorScheme.inverseOnSurface, animationSpec = animationSpec)
    val error = animateColorAsState(targetValue = targetColorScheme.error, animationSpec = animationSpec)
    val onError = animateColorAsState(targetValue = targetColorScheme.onError, animationSpec = animationSpec)
    val errorContainer = animateColorAsState(targetValue = targetColorScheme.errorContainer, animationSpec = animationSpec)
    val onErrorContainer = animateColorAsState(targetValue = targetColorScheme.onErrorContainer, animationSpec = animationSpec)
    val outline = animateColorAsState(targetValue = targetColorScheme.outline, animationSpec = animationSpec)
    val outlineVariant = animateColorAsState(targetValue = targetColorScheme.outlineVariant, animationSpec = animationSpec)
    val scrim = animateColorAsState(targetValue = targetColorScheme.scrim, animationSpec = animationSpec)
    val surfaceBright = animateColorAsState(targetValue = targetColorScheme.surfaceBright, animationSpec = animationSpec)
    val surfaceDim = animateColorAsState(targetValue = targetColorScheme.surfaceDim, animationSpec = animationSpec)
    val surfaceContainer = animateColorAsState(targetValue = targetColorScheme.surfaceContainer, animationSpec = animationSpec)
    val surfaceContainerHigh = animateColorAsState(targetValue = targetColorScheme.surfaceContainerHigh, animationSpec = animationSpec)
    val surfaceContainerHighest = animateColorAsState(targetValue = targetColorScheme.surfaceContainerHighest, animationSpec = animationSpec)
    val surfaceContainerLow = animateColorAsState(targetValue = targetColorScheme.surfaceContainerLow, animationSpec = animationSpec)
    val surfaceContainerLowest = animateColorAsState(targetValue = targetColorScheme.surfaceContainerLowest, animationSpec = animationSpec)
    val rememberedStatefulColorScheme = remember {
        StatefulColorScheme(
            primaryState = primary,
            onPrimaryState = onPrimary,
            primaryContainerState = primaryContainer,
            onPrimaryContainerState = onPrimaryContainer,
            inversePrimaryState = inversePrimary,
            secondaryState = secondary,
            onSecondaryState = onSecondary,
            secondaryContainerState = secondaryContainer,
            onSecondaryContainerState = onSecondaryContainer,
            tertiaryState = tertiary,
            onTertiaryState = onTertiary,
            tertiaryContainerState = tertiaryContainer,
            onTertiaryContainerState = onTertiaryContainer,
            backgroundState = background,
            onBackgroundState = onBackground,
            surfaceState = surface,
            onSurfaceState = onSurface,
            surfaceVariantState = surfaceVariant,
            onSurfaceVariantState = onSurfaceVariant,
            surfaceTintState = surfaceTint,
            inverseSurfaceState = inverseSurface,
            inverseOnSurfaceState = inverseOnSurface,
            errorState = error,
            onErrorState = onError,
            errorContainerState = errorContainer,
            onErrorContainerState = onErrorContainer,
            outlineState = outline,
            outlineVariantState = outlineVariant,
            scrimState = scrim,
            surfaceBrightState = surfaceBright,
            surfaceDimState = surfaceDim,
            surfaceContainerState = surfaceContainer,
            surfaceContainerHighState = surfaceContainerHigh,
            surfaceContainerHighestState = surfaceContainerHighest,
            surfaceContainerLowState = surfaceContainerLow,
            surfaceContainerLowestState = surfaceContainerLowest,
        )
    }
    return rememberedStatefulColorScheme
}

private const val lightMapStyleUrl = "mapbox://styles/illyan/cl3kgeewz004k15ldn7x091r2"
private const val darkMapStyleUrl = "mapbox://styles/illyan/cl3kg2wpq001414muhgrpj15u"
private val _mapStyleUrl = MutableStateFlow(lightMapStyleUrl)
val mapStyleUrl = _mapStyleUrl.asStateFlow()

private lateinit var darkMapMarkers: MapMarkers
// val drawable = AppCompatResources.getDrawable(context, R.drawable.jay_puck_transparent_background)
// val image = drawable!!.toBitmap(height = puckHeight, width = puckHeight * drawable.intrinsicWidth / drawable.intrinsicHeight)
private lateinit var lightMapMarkers: MapMarkers
private val _mapMarkers = MutableStateFlow<MapMarkers?>(null)
val mapMarkers = _mapMarkers.asStateFlow()

val targetColorScheme = StatefulColorScheme(LightColors)
internal val LocalStatefulColorScheme = staticCompositionLocalOf { targetColorScheme }
val LocalToggleTheme = compositionLocalOf { {} }
val LocalTheme = compositionLocalOf { Theme.System }

enum class Theme {
    Light, Dark, System
}

@Composable
fun JayTheme(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    var theme by remember { mutableStateOf(Theme.System) }
    val toggleTheme = {
        theme = when (theme) {
            Theme.Light -> Theme.Dark
            Theme.Dark -> Theme.System
            Theme.System -> Theme.Light
        }
    }
    val darkIcons by remember {
        derivedStateOf {
            when (theme) {
                Theme.Light -> true
                Theme.Dark -> false
                Theme.System -> !isSystemInDarkTheme
            }
        }
    }
    val context = LocalContext.current
    val colorScheme by remember {
        derivedStateOf {
            when (theme) {
                Theme.Dark -> DarkColors
                Theme.Light -> LightColors
                Theme.System -> {
                    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (isSystemInDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                    } else {
                        if (isSystemInDarkTheme) DarkColors else LightColors
                    }
                }
            }
        }
    }
    val systemUiController = rememberSystemUiController()
    val colorSchemeState = animateColorScheme(colorScheme, spring())
    val view = LocalView.current
    val density = LocalDensity.current.density
    val markerHeight = (36.dp * density).value.roundToInt()
    lightMapMarkers = MapMarkers(
        height = markerHeight,
        locationPuckDrawableId = R.drawable.jay_puck_transparent_background,
        poiDrawableId = R.drawable.jay_marker_icon_v3_round,
        pathStartDrawableId = R.drawable.jay_begin_light_marker_icon,
        pathEndDrawableId = R.drawable.jay_finish_light_marker_icon,
    )
    darkMapMarkers = MapMarkers(
        height = markerHeight,
        locationPuckDrawableId = R.drawable.jay_puck_transparent_background,
        poiDrawableId = R.drawable.jay_marker_icon_v3_round,
        pathStartDrawableId = R.drawable.jay_begin_dark_marker_icon,
        pathEndDrawableId = R.drawable.jay_finish_dark_marker_icon,
    )
    if (!view.isInEditMode) {
        LaunchedEffect(theme) {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkIcons
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Update all of the system bar colors to be transparent
            // and use dark icons if we're in light theme
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = darkIcons
            )
            _mapStyleUrl.update { if (!darkIcons) darkMapStyleUrl else lightMapStyleUrl }
            _mapMarkers.update { if (!darkIcons) darkMapMarkers else lightMapMarkers }
        }
    }

    CompositionLocalProvider(
        LocalStatefulColorScheme provides colorSchemeState,
        LocalToggleTheme provides toggleTheme,
        LocalTheme provides theme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.signatureBlue: Color
    get() = Color(0xFF1B8FFF)

val MaterialTheme.signaturePink: Color
    get() = Color(0xFFFF63A0)

val MaterialTheme.statefulColorScheme: StatefulColorScheme
    @Composable
    get() = LocalStatefulColorScheme.current

@Composable
fun StatefulColorScheme.surfaceColorAtElevation(
    elevation: Dp,
): Color {
    val color by remember {
        derivedStateOf {
            if (elevation == 0.dp) {
                surface
            } else {
                val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
                surfaceTint.copy(alpha = alpha).compositeOver(surface)
            }
        }
    }
    return color
}
