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

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

@ExperimentalComposeUiApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Breadcrumb(
    modifier: Modifier = Modifier,
    elements: List<String> = emptyList(),
    maxElementShown: Int = 2
) {
    val lazyListState = rememberLazyListState()
    LaunchedEffect(elements.size) {
        if (elements.isNotEmpty()) lazyListState.scrollToItem(0)
    }
    LazyRow(
        modifier = modifier,
        state = lazyListState,
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(elements) { index, element ->
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(false) }
                    .apply { targetState = elements.size - maxElementShown <= index }
            ) {
                Row(
                    modifier = Modifier
                        .animateContentSize()
                        .animateEnterExit(
                            enter = fadeIn(),
                            exit = fadeOut()
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visibleState = remember { MutableTransitionState(false) }
                            .apply { targetState = elements.size - maxElementShown < index }
                    ) {
                        Icon(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = ""
                        )
                    }
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "$index screen"
                    )
                }
            }
        }
    }
}