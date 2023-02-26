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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import illyan.jay.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Values got from material3/Tooltip.kt
private const val TooltipFadeInDuration = 150L
private const val TooltipFadeOutDuration = 75L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TooltipElevatedCard(
    modifier: Modifier = Modifier,
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val tooltipState = remember { PlainTooltipState() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(tooltipState.isVisible) {
        if (tooltipState.isVisible) onShowTooltip() else onDismissTooltip()
    }

    var currentTooltip by remember {
        mutableStateOf<@Composable () -> Unit>(
            if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        )
    }
    LaunchedEffect(enabled, tooltipState.isVisible) {
        if (!tooltipState.isVisible) {
            // Waiting for the fade out animation to end, then switch tooltip
            delay(TooltipFadeOutDuration)
            currentTooltip = if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        }
    }

    PlainTooltipBox(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tooltip = currentTooltip,
        tooltipState = tooltipState,
    ) {
        OutlinedCard(
            enabled = enabled,
            onClick = {}
        ) {
            Surface(
                modifier = Modifier
                    .animateContentSize()
                    .combinedClickable(
                        onLongClick = {
                            coroutineScope.launch {
                                if (enabled || disabledTooltip != null) tooltipState.show()
                            }
                        },
                        onClick = {}
                    ),
                color = Color.Transparent,
                content = content
            )
        }
    }
}

@Composable
fun CopiedToKeyboardTooltip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Done,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = stringResource(R.string.copied_to_clipboard))
    }
}