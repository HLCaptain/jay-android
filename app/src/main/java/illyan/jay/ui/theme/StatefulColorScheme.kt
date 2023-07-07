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

package illyan.jay.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

@Stable
class StatefulColorScheme(
    primaryState: State<Color>,
    onPrimaryState: State<Color>,
    primaryContainerState: State<Color>,
    onPrimaryContainerState: State<Color>,
    inversePrimaryState: State<Color>,
    secondaryState: State<Color>,
    onSecondaryState: State<Color>,
    secondaryContainerState: State<Color>,
    onSecondaryContainerState: State<Color>,
    tertiaryState: State<Color>,
    onTertiaryState: State<Color>,
    tertiaryContainerState: State<Color>,
    onTertiaryContainerState: State<Color>,
    backgroundState: State<Color>,
    onBackgroundState: State<Color>,
    surfaceState: State<Color>,
    onSurfaceState: State<Color>,
    surfaceVariantState: State<Color>,
    onSurfaceVariantState: State<Color>,
    surfaceTintState: State<Color>,
    inverseSurfaceState: State<Color>,
    inverseOnSurfaceState: State<Color>,
    errorState: State<Color>,
    onErrorState: State<Color>,
    errorContainerState: State<Color>,
    onErrorContainerState: State<Color>,
    outlineState: State<Color>,
    outlineVariantState: State<Color>,
    scrimState: State<Color>,
    surfaceBrightState: State<Color>,
    surfaceDimState: State<Color>,
    surfaceContainerState: State<Color>,
    surfaceContainerHighState: State<Color>,
    surfaceContainerHighestState: State<Color>,
    surfaceContainerLowState: State<Color>,
    surfaceContainerLowestState: State<Color>,
) {
    constructor(
        primaryState: State<Color>,
        onPrimaryState: State<Color>,
        primaryContainerState: State<Color>,
        onPrimaryContainerState: State<Color>,
        inversePrimaryState: State<Color>,
        secondaryState: State<Color>,
        onSecondaryState: State<Color>,
        secondaryContainerState: State<Color>,
        onSecondaryContainerState: State<Color>,
        tertiaryState: State<Color>,
        onTertiaryState: State<Color>,
        tertiaryContainerState: State<Color>,
        onTertiaryContainerState: State<Color>,
        backgroundState: State<Color>,
        onBackgroundState: State<Color>,
        surfaceState: State<Color>,
        onSurfaceState: State<Color>,
        surfaceVariantState: State<Color>,
        onSurfaceVariantState: State<Color>,
        surfaceTintState: State<Color>,
        inverseSurfaceState: State<Color>,
        inverseOnSurfaceState: State<Color>,
        errorState: State<Color>,
        onErrorState: State<Color>,
        errorContainerState: State<Color>,
        onErrorContainerState: State<Color>,
        outlineState: State<Color>,
        outlineVariantState: State<Color>,
        scrimState: State<Color>,
    ) : this(
        primaryState = primaryState,
        onPrimaryState = onPrimaryState,
        primaryContainerState = primaryContainerState,
        onPrimaryContainerState = onPrimaryContainerState,
        inversePrimaryState = inversePrimaryState,
        secondaryState = secondaryState,
        onSecondaryState = onSecondaryState,
        secondaryContainerState = secondaryContainerState,
        onSecondaryContainerState = onSecondaryContainerState,
        tertiaryState = tertiaryState,
        onTertiaryState = onTertiaryState,
        tertiaryContainerState = tertiaryContainerState,
        onTertiaryContainerState = onTertiaryContainerState,
        backgroundState = backgroundState,
        onBackgroundState = onBackgroundState,
        surfaceState = surfaceState,
        onSurfaceState = onSurfaceState,
        surfaceVariantState = surfaceVariantState,
        onSurfaceVariantState = onSurfaceVariantState,
        surfaceTintState = surfaceTintState,
        inverseSurfaceState = inverseSurfaceState,
        inverseOnSurfaceState = inverseOnSurfaceState,
        errorState = errorState,
        onErrorState = onErrorState,
        errorContainerState = errorContainerState,
        onErrorContainerState = onErrorContainerState,
        outlineState = outlineState,
        outlineVariantState = outlineVariantState,
        scrimState = scrimState,
        surfaceBrightState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceDimState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerHighState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerHighestState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerLowState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerLowestState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
    )

    constructor(
        colorScheme: ColorScheme,
    ) : this(
        primaryState = mutableStateOf(colorScheme.primary, structuralEqualityPolicy()),
        onPrimaryState = mutableStateOf(colorScheme.onPrimary, structuralEqualityPolicy()),
        primaryContainerState = mutableStateOf(colorScheme.primaryContainer, structuralEqualityPolicy()),
        onPrimaryContainerState = mutableStateOf(colorScheme.onPrimaryContainer, structuralEqualityPolicy()),
        inversePrimaryState = mutableStateOf(colorScheme.inversePrimary, structuralEqualityPolicy()),
        secondaryState = mutableStateOf(colorScheme.secondary, structuralEqualityPolicy()),
        onSecondaryState = mutableStateOf(colorScheme.onSecondary, structuralEqualityPolicy()),
        secondaryContainerState = mutableStateOf(colorScheme.secondaryContainer, structuralEqualityPolicy()),
        onSecondaryContainerState = mutableStateOf(colorScheme.onSecondaryContainer, structuralEqualityPolicy()),
        tertiaryState = mutableStateOf(colorScheme.tertiary, structuralEqualityPolicy()),
        onTertiaryState = mutableStateOf(colorScheme.onTertiary, structuralEqualityPolicy()),
        tertiaryContainerState = mutableStateOf(colorScheme.tertiaryContainer, structuralEqualityPolicy()),
        onTertiaryContainerState = mutableStateOf(colorScheme.onTertiaryContainer, structuralEqualityPolicy()),
        backgroundState = mutableStateOf(colorScheme.background, structuralEqualityPolicy()),
        onBackgroundState = mutableStateOf(colorScheme.onBackground, structuralEqualityPolicy()),
        surfaceState = mutableStateOf(colorScheme.surface, structuralEqualityPolicy()),
        onSurfaceState = mutableStateOf(colorScheme.onSurface, structuralEqualityPolicy()),
        surfaceVariantState = mutableStateOf(colorScheme.surfaceVariant, structuralEqualityPolicy()),
        onSurfaceVariantState = mutableStateOf(colorScheme.onSurfaceVariant, structuralEqualityPolicy()),
        surfaceTintState = mutableStateOf(colorScheme.surfaceTint, structuralEqualityPolicy()),
        inverseSurfaceState = mutableStateOf(colorScheme.inverseSurface, structuralEqualityPolicy()),
        inverseOnSurfaceState = mutableStateOf(colorScheme.inverseOnSurface, structuralEqualityPolicy()),
        errorState = mutableStateOf(colorScheme.error, structuralEqualityPolicy()),
        onErrorState = mutableStateOf(colorScheme.onError, structuralEqualityPolicy()),
        errorContainerState = mutableStateOf(colorScheme.errorContainer, structuralEqualityPolicy()),
        onErrorContainerState = mutableStateOf(colorScheme.onErrorContainer, structuralEqualityPolicy()),
        outlineState = mutableStateOf(colorScheme.outline, structuralEqualityPolicy()),
        outlineVariantState = mutableStateOf(colorScheme.outlineVariant, structuralEqualityPolicy()),
        scrimState = mutableStateOf(colorScheme.scrim, structuralEqualityPolicy()),
        surfaceBrightState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceDimState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerHighState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerHighestState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerLowState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
        surfaceContainerLowestState = mutableStateOf(Color.Unspecified, structuralEqualityPolicy()),
    )

    val primary by primaryState
    val onPrimary by onPrimaryState
    val primaryContainer by primaryContainerState
    val onPrimaryContainer by onPrimaryContainerState
    val inversePrimary by inversePrimaryState
    val secondary by secondaryState
    val onSecondary by onSecondaryState
    val secondaryContainer by secondaryContainerState
    val onSecondaryContainer by onSecondaryContainerState
    val tertiary by tertiaryState
    val onTertiary by onTertiaryState
    val tertiaryContainer by tertiaryContainerState
    val onTertiaryContainer by onTertiaryContainerState
    val background by backgroundState
    val onBackground by onBackgroundState
    val surface by surfaceState
    val onSurface by onSurfaceState
    val surfaceVariant by surfaceVariantState
    val onSurfaceVariant by onSurfaceVariantState
    val surfaceTint by surfaceTintState
    val inverseSurface by inverseSurfaceState
    val inverseOnSurface by inverseOnSurfaceState
    val error by errorState
    val onError by onErrorState
    val errorContainer by errorContainerState
    val onErrorContainer by onErrorContainerState
    val outline by outlineState
    val outlineVariant by outlineVariantState
    val scrim by scrimState
    val surfaceBright by surfaceBrightState
    val surfaceDim by surfaceDimState
    val surfaceContainer by surfaceContainerState
    val surfaceContainerHigh by surfaceContainerHighState
    val surfaceContainerHighest by surfaceContainerHighestState
    val surfaceContainerLow by surfaceContainerLowState
    val surfaceContainerLowest by surfaceContainerLowestState

    var primaryState by mutableStateOf(primaryState, structuralEqualityPolicy())
        internal set
    var onPrimaryState by mutableStateOf(onPrimaryState, structuralEqualityPolicy())
        internal set
    var primaryContainerState by mutableStateOf(primaryContainerState, structuralEqualityPolicy())
        internal set
    var onPrimaryContainerState by mutableStateOf(onPrimaryContainerState, structuralEqualityPolicy())
        internal set
    var inversePrimaryState by mutableStateOf(inversePrimaryState, structuralEqualityPolicy())
        internal set
    var secondaryState by mutableStateOf(secondaryState, structuralEqualityPolicy())
        internal set
    var onSecondaryState by mutableStateOf(onSecondaryState, structuralEqualityPolicy())
        internal set
    var secondaryContainerState by mutableStateOf(secondaryContainerState, structuralEqualityPolicy())
        internal set
    var onSecondaryContainerState by mutableStateOf(onSecondaryContainerState, structuralEqualityPolicy())
        internal set
    var tertiaryState by mutableStateOf(tertiaryState, structuralEqualityPolicy())
        internal set
    var onTertiaryState by mutableStateOf(onTertiaryState, structuralEqualityPolicy())
        internal set
    var tertiaryContainerState by mutableStateOf(tertiaryContainerState, structuralEqualityPolicy())
        internal set
    var onTertiaryContainerState by mutableStateOf(onTertiaryContainerState, structuralEqualityPolicy())
        internal set
    var backgroundState by mutableStateOf(backgroundState, structuralEqualityPolicy())
        internal set
    var onBackgroundState by mutableStateOf(onBackgroundState, structuralEqualityPolicy())
        internal set
    var surfaceState by mutableStateOf(surfaceState, structuralEqualityPolicy())
        internal set
    var onSurfaceState by mutableStateOf(onSurfaceState, structuralEqualityPolicy())
        internal set
    var surfaceVariantState by mutableStateOf(surfaceVariantState, structuralEqualityPolicy())
        internal set
    var onSurfaceVariantState by mutableStateOf(onSurfaceVariantState, structuralEqualityPolicy())
        internal set
    var surfaceTintState by mutableStateOf(surfaceTintState, structuralEqualityPolicy())
        internal set
    var inverseSurfaceState by mutableStateOf(inverseSurfaceState, structuralEqualityPolicy())
        internal set
    var inverseOnSurfaceState by mutableStateOf(inverseOnSurfaceState, structuralEqualityPolicy())
        internal set
    var errorState by mutableStateOf(errorState, structuralEqualityPolicy())
        internal set
    var onErrorState by mutableStateOf(onErrorState, structuralEqualityPolicy())
        internal set
    var errorContainerState by mutableStateOf(errorContainerState, structuralEqualityPolicy())
        internal set
    var onErrorContainerState by mutableStateOf(onErrorContainerState, structuralEqualityPolicy())
        internal set
    var outlineState by mutableStateOf(outlineState, structuralEqualityPolicy())
        internal set
    var outlineVariantState by mutableStateOf(outlineVariantState, structuralEqualityPolicy())
        internal set
    var scrimState by mutableStateOf(scrimState, structuralEqualityPolicy())
        internal set
    var surfaceBrightState by mutableStateOf(surfaceBrightState, structuralEqualityPolicy())
        internal set
    var surfaceDimState by mutableStateOf(surfaceDimState, structuralEqualityPolicy())
        internal set
    var surfaceContainerState by mutableStateOf(surfaceContainerState, structuralEqualityPolicy())
        internal set
    var surfaceContainerHighState by mutableStateOf(surfaceContainerHighState, structuralEqualityPolicy())
        internal set
    var surfaceContainerHighestState by mutableStateOf(surfaceContainerHighestState, structuralEqualityPolicy())
        internal set
    var surfaceContainerLowState by mutableStateOf(surfaceContainerLowState, structuralEqualityPolicy())
        internal set
    var surfaceContainerLowestState by mutableStateOf(surfaceContainerLowestState, structuralEqualityPolicy())
        internal set

    /** Returns a copy of this ColorScheme, optionally overriding some of the values. */
    fun copy(
        primaryState: State<Color> = this.primaryState,
        onPrimaryState: State<Color> = this.onPrimaryState,
        primaryContainerState: State<Color> = this.primaryContainerState,
        onPrimaryContainerState: State<Color> = this.onPrimaryContainerState,
        inversePrimaryState: State<Color> = this.inversePrimaryState,
        secondaryState: State<Color> = this.secondaryState,
        onSecondaryState: State<Color> = this.onSecondaryState,
        secondaryContainerState: State<Color> = this.secondaryContainerState,
        onSecondaryContainerState: State<Color> = this.onSecondaryContainerState,
        tertiaryState: State<Color> = this.tertiaryState,
        onTertiaryState: State<Color> = this.onTertiaryState,
        tertiaryContainerState: State<Color> = this.tertiaryContainerState,
        onTertiaryContainerState: State<Color> = this.onTertiaryContainerState,
        backgroundState: State<Color> = this.backgroundState,
        onBackgroundState: State<Color> = this.onBackgroundState,
        surfaceState: State<Color> = this.surfaceState,
        onSurfaceState: State<Color> = this.onSurfaceState,
        surfaceVariantState: State<Color> = this.surfaceVariantState,
        onSurfaceVariantState: State<Color> = this.onSurfaceVariantState,
        surfaceTintState: State<Color> = this.surfaceTintState,
        inverseSurfaceState: State<Color> = this.inverseSurfaceState,
        inverseOnSurfaceState: State<Color> = this.inverseOnSurfaceState,
        errorState: State<Color> = this.errorState,
        onErrorState: State<Color> = this.onErrorState,
        errorContainerState: State<Color> = this.errorContainerState,
        onErrorContainerState: State<Color> = this.onErrorContainerState,
        outlineState: State<Color> = this.outlineState,
        outlineVariantState: State<Color> = this.outlineVariantState,
        scrimState: State<Color> = this.scrimState,
        surfaceBrightState: State<Color> = this.surfaceBrightState,
        surfaceDimState: State<Color> = this.surfaceDimState,
        surfaceContainerState: State<Color> = this.surfaceContainerState,
        surfaceContainerHighState: State<Color> = this.surfaceContainerHighState,
        surfaceContainerHighestState: State<Color> = this.surfaceContainerHighestState,
        surfaceContainerLowState: State<Color> = this.surfaceContainerLowState,
        surfaceContainerLowestState: State<Color> = this.surfaceContainerLowestState,
    ) = StatefulColorScheme(
        primaryState = primaryState,
        onPrimaryState = onPrimaryState,
        primaryContainerState = primaryContainerState,
        onPrimaryContainerState = onPrimaryContainerState,
        inversePrimaryState = inversePrimaryState,
        secondaryState = secondaryState,
        onSecondaryState = onSecondaryState,
        secondaryContainerState = secondaryContainerState,
        onSecondaryContainerState = onSecondaryContainerState,
        tertiaryState = tertiaryState,
        onTertiaryState = onTertiaryState,
        tertiaryContainerState = tertiaryContainerState,
        onTertiaryContainerState = onTertiaryContainerState,
        backgroundState = backgroundState,
        onBackgroundState = onBackgroundState,
        surfaceState = surfaceState,
        onSurfaceState = onSurfaceState,
        surfaceVariantState = surfaceVariantState,
        onSurfaceVariantState = onSurfaceVariantState,
        surfaceTintState = surfaceTintState,
        inverseSurfaceState = inverseSurfaceState,
        inverseOnSurfaceState = inverseOnSurfaceState,
        errorState = errorState,
        onErrorState = onErrorState,
        errorContainerState = errorContainerState,
        onErrorContainerState = onErrorContainerState,
        outlineState = outlineState,
        outlineVariantState = outlineVariantState,
        scrimState = scrimState,
        surfaceBrightState = surfaceBrightState,
        surfaceDimState = surfaceDimState,
        surfaceContainerState = surfaceContainerState,
        surfaceContainerHighState = surfaceContainerHighState,
        surfaceContainerHighestState = surfaceContainerHighestState,
        surfaceContainerLowState = surfaceContainerLowState,
        surfaceContainerLowestState = surfaceContainerLowestState,
    )

    @Deprecated(
        message =
        "Maintained for binary compatibility. Use overload with additional surface roles " +
                "instead",
        level = DeprecationLevel.HIDDEN
    )
    fun copy(
        primaryState: State<Color> = this.primaryState,
        onPrimaryState: State<Color> = this.onPrimaryState,
        primaryContainerState: State<Color> = this.primaryContainerState,
        onPrimaryContainerState: State<Color> = this.onPrimaryContainerState,
        inversePrimaryState: State<Color> = this.inversePrimaryState,
        secondaryState: State<Color> = this.secondaryState,
        onSecondaryState: State<Color> = this.onSecondaryState,
        secondaryContainerState: State<Color> = this.secondaryContainerState,
        onSecondaryContainerState: State<Color> = this.onSecondaryContainerState,
        tertiaryState: State<Color> = this.tertiaryState,
        onTertiaryState: State<Color> = this.onTertiaryState,
        tertiaryContainerState: State<Color> = this.tertiaryContainerState,
        onTertiaryContainerState: State<Color> = this.onTertiaryContainerState,
        backgroundState: State<Color> = this.backgroundState,
        onBackgroundState: State<Color> = this.onBackgroundState,
        surfaceState: State<Color> = this.surfaceState,
        onSurfaceState: State<Color> = this.onSurfaceState,
        surfaceVariantState: State<Color> = this.surfaceVariantState,
        onSurfaceVariantState: State<Color> = this.onSurfaceVariantState,
        surfaceTintState: State<Color> = this.surfaceTintState,
        inverseSurfaceState: State<Color> = this.inverseSurfaceState,
        inverseOnSurfaceState: State<Color> = this.inverseOnSurfaceState,
        errorState: State<Color> = this.errorState,
        onErrorState: State<Color> = this.onErrorState,
        errorContainerState: State<Color> = this.errorContainerState,
        onErrorContainerState: State<Color> = this.onErrorContainerState,
        outlineState: State<Color> = this.outlineState,
        outlineVariantState: State<Color> = this.outlineVariantState,
        scrimState: State<Color> = this.scrimState,
    ) = copy(
        primaryState = primaryState,
        onPrimaryState = onPrimaryState,
        primaryContainerState = primaryContainerState,
        onPrimaryContainerState = onPrimaryContainerState,
        inversePrimaryState = inversePrimaryState,
        secondaryState = secondaryState,
        onSecondaryState = onSecondaryState,
        secondaryContainerState = secondaryContainerState,
        onSecondaryContainerState = onSecondaryContainerState,
        tertiaryState = tertiaryState,
        onTertiaryState = onTertiaryState,
        tertiaryContainerState = tertiaryContainerState,
        onTertiaryContainerState = onTertiaryContainerState,
        backgroundState = backgroundState,
        onBackgroundState = onBackgroundState,
        surfaceState = surfaceState,
        onSurfaceState = onSurfaceState,
        surfaceVariantState = surfaceVariantState,
        onSurfaceVariantState = onSurfaceVariantState,
        surfaceTintState = surfaceTintState,
        inverseSurfaceState = inverseSurfaceState,
        inverseOnSurfaceState = inverseOnSurfaceState,
        errorState = errorState,
        onErrorState = onErrorState,
        errorContainerState = errorContainerState,
        onErrorContainerState = onErrorContainerState,
        outlineState = outlineState,
        outlineVariantState = outlineVariantState,
        scrimState = scrimState,
    )

    override fun toString(): String {
        return "StatefulColorScheme(" +
                "primaryState=$primaryState" +
                "onPrimaryState=$onPrimaryState" +
                "primaryContainerState=$primaryContainerState" +
                "onPrimaryContainerState=$onPrimaryContainerState" +
                "inversePrimaryState=$inversePrimaryState" +
                "secondaryState=$secondaryState" +
                "onSecondaryState=$onSecondaryState" +
                "secondaryContainerState=$secondaryContainerState" +
                "onSecondaryContainerState=$onSecondaryContainerState" +
                "tertiaryState=$tertiaryState" +
                "onTertiaryState=$onTertiaryState" +
                "tertiaryContainerState=$tertiaryContainerState" +
                "onTertiaryContainerState=$onTertiaryContainerState" +
                "backgroundState=$backgroundState" +
                "onBackgroundState=$onBackgroundState" +
                "surfaceState=$surfaceState" +
                "onSurfaceState=$onSurfaceState" +
                "surfaceVariantState=$surfaceVariantState" +
                "onSurfaceVariantState=$onSurfaceVariantState" +
                "surfaceTintState=$surfaceTintState" +
                "inverseSurfaceState=$inverseSurfaceState" +
                "inverseOnSurfaceState=$inverseOnSurfaceState" +
                "errorState=$errorState" +
                "onErrorState=$onErrorState" +
                "errorContainerState=$errorContainerState" +
                "onErrorContainerState=$onErrorContainerState" +
                "outlineState=$outlineState" +
                "outlineVariantState=$outlineVariantState" +
                "scrimState=$scrimState" +
                "surfaceBrightState=$surfaceBrightState" +
                "surfaceDimState=$surfaceDimState" +
                "surfaceContainerState=$surfaceContainerState" +
                "surfaceContainerHighState=$surfaceContainerHighState" +
                "surfaceContainerHighestState=$surfaceContainerHighestState" +
                "surfaceContainerLowState=$surfaceContainerLowState" +
                "surfaceContainerLowestState=$surfaceContainerLowestState" +
                ")"
    }
}
