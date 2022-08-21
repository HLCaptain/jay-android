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

@file:OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)

package illyan.jay.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.R
import illyan.jay.ui.map.MapboxMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@RootNavGraph(start = true)
@NavGraph
annotation class HomeNavGraph(
    val start: Boolean = false
)

val RoundedCornerRadius = 24.dp
val SearchBarSpaceBetween = 8.dp
val SearchBarHeight = 64.dp
val SearchMarkerPaddingValues = PaddingValues(
    top = 8.dp,
    start = 4.dp
)
val SearchFieldPaddingValues = PaddingValues(
    top = 6.dp
)
val AvatarPaddingValues = PaddingValues(
    top = 8.dp,
    end = 8.dp
)
const val BottomSheetPartialExpendedFraction = 0.5f

fun BottomSheetState.isExpending() =
    isAnimationRunning && targetValue == BottomSheetValue.Expanded

fun BottomSheetState.isCollapsing() =
    isAnimationRunning && targetValue == BottomSheetValue.Collapsed

fun onSearchBarDrag(
    coroutineScope: CoroutineScope,
    bottomSheetState: BottomSheetState,
    enabled: Boolean = true,
    onEnabledChange: (Boolean) -> Unit = {}
) {
    // By dragging the search bar, we can toggle bottom sheet state
    if (enabled) {
        bottomSheetState.apply {
            if (isCollapsed) {
                onEnabledChange(false)
                coroutineScope.launch { expand() }
            } else if (isExpanded) {
                onEnabledChange(false)
                coroutineScope.launch { collapse() }
            }
        }
        Timber.d("Search bar is dragged!")
    }
}

fun calculateCornerRadius(
    bottomSheetState: BottomSheetState,
    maxCornerRadius: Dp = RoundedCornerRadius,
    minCornerRadius: Dp = 0.dp,
    threshold: Float = BottomSheetPartialExpendedFraction,
    fraction: Float = 0f
): Dp {
    return if (
        bottomSheetState.isCollapsed ||
        bottomSheetState.isCollapsing()
    ) {
        maxCornerRadius
    } else {
        lerp(
            maxCornerRadius,
            minCornerRadius,
            (1f - (1f - fraction) / threshold).coerceIn(0f, 1f)
        ).coerceAtLeast(0.dp)
    }
}

@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen() {
    ConstraintLayout {
        val (searchBar, scaffold) = createRefs()
        val scaffoldState = rememberBottomSheetScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        val bottomSheetState = scaffoldState.bottomSheetState
        var isTextFieldFocused by remember { mutableStateOf(false) }
        var roundDp by remember { mutableStateOf(RoundedCornerRadius) }
        var shouldTriggerBottomSheetOnDrag by remember { mutableStateOf(true) }
        // Close the keyboard when closing the bottom sheet
        if (bottomSheetState.isCollapsing()) {
            LocalSoftwareKeyboardController.current?.hide()
        }
        // When the bottom sheet reaches its target state, reset onDrag trigger
        if (bottomSheetState.targetValue == bottomSheetState.currentValue) {
            // Resetting the trigger to enable bottom sheet toggle
            shouldTriggerBottomSheetOnDrag = true
        }
        BottomSearchBar(
            modifier = Modifier
                .zIndex(1f) // Search bar is in front of everything else
                .constrainAs(searchBar) {
                    bottom.linkTo(scaffold.bottom)
                    start.linkTo(parent.start)
                }
                .imePadding()
                .statusBarsPadding()
                .navigationBarsPadding(),
            onDrag = {
                onSearchBarDrag(
                    bottomSheetState = bottomSheetState,
                    enabled = shouldTriggerBottomSheetOnDrag,
                    onEnabledChange = { shouldTriggerBottomSheetOnDrag = it },
                    coroutineScope = coroutineScope
                )
            },
            bottomSheetState = bottomSheetState,
            onTextFieldFocusChanged = {
                isTextFieldFocused = it.hasFocus || it.isFocused
                coroutineScope.launch {
                    // When searching, show search results on bottom sheet
                    if (isTextFieldFocused) {
                        bottomSheetState.expand()
                    }
                }
            }
        )
        BottomSheetScaffold(
            modifier = Modifier.constrainAs(scaffold) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            sheetContent = {
                MenuScreen(
                    isTextFieldFocused = isTextFieldFocused,
                    onBottomSheetFractionChange = {
                        roundDp = calculateCornerRadius(
                            bottomSheetState = bottomSheetState,
                            maxCornerRadius = RoundedCornerRadius,
                            minCornerRadius = 0.dp,
                            fraction = it,
                            threshold = BottomSheetPartialExpendedFraction
                        )
                    }
                )
            },
            sheetPeekHeight = SearchBarHeight,
            scaffoldState = scaffoldState,
            sheetShape = RoundedCornerShape(
                topStart = roundDp,
                topEnd = roundDp
            )

        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = SearchBarHeight - RoundedCornerRadius / 2f)
            ) {
                MapboxMap( // Budapest University of Technology and Economics
                    lat = 47.481491,
                    lng = 19.056219,
                    zoom = 12.0
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomSearchBar(
    modifier: Modifier = Modifier,
    onDrag: (Float) -> Unit = {},
    bottomSheetState: BottomSheetState? = null,
    onTextFieldFocusChanged: (FocusState) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val cardColors = CardDefaults.elevatedCardColors(
        containerColor = Color.White
    )
    val elevation = 8.dp
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = elevation,
        disabledElevation = elevation,
        draggedElevation = elevation,
        focusedElevation = elevation,
        hoveredElevation = elevation,
        pressedElevation = elevation
    )
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    if (bottomSheetState?.isCollapsing() == true) {
        coroutineScope.launch {
            focusRequester.freeFocus()
            focusManager.clearFocus()
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val draggable = Modifier.draggable(
        interactionSource = interactionSource,
        orientation = Orientation.Vertical,
        state = rememberDraggableState { onDrag(it) }
    )
    ElevatedCard(
        modifier = modifier.then(draggable),
        shape = RoundedCornerShape(
            topStart = RoundedCornerRadius,
            topEnd = RoundedCornerRadius
        ),
        colors = cardColors,
        onClick = { focusRequester.requestFocus() },
        interactionSource = interactionSource,
        elevation = cardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SearchBarHeight),
            horizontalArrangement = Arrangement.spacedBy(SearchBarSpaceBetween)
        ) {
            IconButton(
                onClick = { focusRequester.requestFocus() },
                modifier = Modifier.padding(SearchMarkerPaddingValues)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_jay_marker_icon_v3_round),
                    contentDescription = stringResource(R.string.search_marker_icon),
                    modifier = Modifier.size(RoundedCornerRadius * 2)
                )
            }
            val colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                textColor = Color.Black,
                focusedLabelColor = Color.DarkGray,
                unfocusedLabelColor = Color.LightGray
            )
            var searchPlaceText by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(SearchFieldPaddingValues)
                    .then(draggable)
                    .focusRequester(focusRequester)
                    .onFocusChanged { onTextFieldFocusChanged(it) },
                value = searchPlaceText,
                onValueChange = { searchPlaceText = it },
                label = { Text(stringResource(R.string.search)) },
                placeholder = { Text(stringResource(R.string.where_to)) },
                colors = colors,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusRequester.freeFocus() }
                ),
                interactionSource = interactionSource,
                trailingIcon = {
                    if (searchPlaceText.isNotBlank()) {
                        IconButton(onClick = { searchPlaceText = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = stringResource(R.string.delete_text),
                                tint = Color.LightGray
                            )
                        }
                    }
                }
            )
            IconButton(
                onClick = { },
                modifier = Modifier.padding(AvatarPaddingValues),
                interactionSource = interactionSource
            ) {
                Image(
                    // Placeholder icon for now
                    painter = painterResource(R.drawable.ic_illyan_avatar_filled),
                    contentDescription = stringResource(R.string.avatar_profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(RoundedCornerRadius * 2)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }
        }
    }
}

@Composable
fun MenuScreen(
    isTextFieldFocused: Boolean = false,
    onBottomSheetFractionChange: (Float) -> Unit = {}
) {
    val halfWayFraction = BottomSheetPartialExpendedFraction
    val fullScreenFraction = 1f
    Column(
        modifier = Modifier.fillMaxHeight(
            fraction = if (isTextFieldFocused) {
                onBottomSheetFractionChange(fullScreenFraction)
                fullScreenFraction
            } else {
                onBottomSheetFractionChange(halfWayFraction)
                halfWayFraction
            }
        )
    ) {
        // TODO: Place DestinationsNavHost here
    }
}