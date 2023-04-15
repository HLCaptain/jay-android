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

package illyan.jay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom dialog content styling used (based on Material 3 AlertDialogContent)
 */
@Composable
fun JayDialogContent(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier.heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.66f).dp),
    icon: @Composable (BoxScope.() -> Unit)? = null,
    title: @Composable (BoxScope.() -> Unit)? = null,
    text: @Composable (BoxScope.() -> Unit)? = null,
    buttons: @Composable (BoxScope.() -> Unit)? = null,
    dialogPaddingValues: PaddingValues = JayDialogContentPadding,
    iconPaddingValues: PaddingValues = JayDialogIconPadding,
    titlePaddingValues: PaddingValues = JayDialogTitlePadding,
    textPaddingValues: PaddingValues = JayDialogTextPadding,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    buttonContentColor: Color = MaterialTheme.colorScheme.primary,
) {
    JayDialogContent(
        modifier = modifier,
        surface = {
            JayDialogSurface(
                modifier = modifier,
                shape = shape,
                color = containerColor,
                tonalElevation = tonalElevation,
                content = it
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(dialogPaddingValues)
        ) {
            AnimatedVisibility(visible = icon != null) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(iconPaddingValues)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
            }
            AnimatedVisibility(visible = title != null) {
                title?.let {
                    CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                        val textStyle = MaterialTheme.typography.headlineSmall
                        ProvideTextStyle(textStyle) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .fillMaxWidth()
                                    .padding(titlePaddingValues)
                                    .align(
                                        if (icon == null) {
                                            Alignment.Start
                                        } else {
                                            Alignment.CenterHorizontally
                                        }
                                    )
                            ) {
                                title()
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = text != null) {
                text?.let {
                    CompositionLocalProvider(LocalContentColor provides textContentColor) {
                        val textStyle = MaterialTheme.typography.bodyMedium
                        ProvideTextStyle(textStyle) {
                            Box(
                                textModifier
                                    .fillMaxWidth()
                                    .weight(weight = 1f, fill = false)
                                    .padding(textPaddingValues)
                                    .align(Alignment.Start)
                            ) {
                                text()
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = buttons != null) {
                buttons?.let {
                    CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        ProvideTextStyle(value = textStyle) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.End)
                            ) {
                                buttons()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JayDialogSurface(
    modifier: Modifier = Modifier,
    shape: Shape = AlertDialogDefaults.shape,
    color: Color = AlertDialogDefaults.containerColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        tonalElevation = tonalElevation,
        content = content
    )
}

@Composable
fun JayDialogContent(
    modifier: Modifier = Modifier,
    surface: @Composable (@Composable () -> Unit) -> Unit = {
        JayDialogSurface(
            modifier = modifier,
            content = it
        )
    },
    content: @Composable () -> Unit = {},
) = surface(content)

internal val DialogMinWidth = 280.dp
internal val DialogMaxWidth = 560.dp

// Paddings for each of the dialog's parts.
val JayDialogContentPadding = PaddingValues(all = 24.dp)
val JayDialogIconPadding = PaddingValues(bottom = 16.dp)
val JayDialogTitlePadding = PaddingValues(bottom = 16.dp)
val JayDialogTextPadding = PaddingValues(bottom = 16.dp)
