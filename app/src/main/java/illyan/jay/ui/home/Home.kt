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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Parcelable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.NavGraphs
import illyan.jay.ui.map.ButeK
import illyan.jay.ui.map.MapboxMap
import illyan.jay.ui.map.getBitmapFromVectorDrawable
import illyan.jay.ui.map.turnOnWithDefaultPuck
import illyan.jay.ui.menu.BackPressHandler
import illyan.jay.ui.search.SearchViewModel.Companion.ActionSearchSelected
import illyan.jay.ui.search.SearchViewModel.Companion.KeySearchQuery
import illyan.jay.ui.search.SearchViewModel.Companion.KeySearchSelected
import illyan.jay.ui.theme.Neutral95
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@RootNavGraph(start = true)
@NavGraph
annotation class HomeNavGraph(
    val start: Boolean = false,
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
const val BottomSheetPartialMaxFraction = 1f

lateinit var mapView: MapView
lateinit var sheetState: BottomSheetState
var isSearching: Boolean = false

fun BottomSheetState.isExpanding() =
    isAnimationRunning && targetValue == BottomSheetValue.Expanded

fun BottomSheetState.isCollapsing() =
    isAnimationRunning && targetValue == BottomSheetValue.Collapsed

fun BottomSheetState.isExpendedOrWillBe() =
    isExpanding() || isExpanded

fun BottomSheetState.isCollapsedOrWillBe() =
    isCollapsing() || isCollapsed

fun onSearchBarDrag(
    coroutineScope: CoroutineScope,
    bottomSheetState: BottomSheetState,
    enabled: Boolean = true,
    onEnabledChange: (Boolean) -> Unit = {},
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
    isSearching: Boolean = false,
    bottomSheetState: BottomSheetState,
    maxCornerRadius: Dp = RoundedCornerRadius,
    minCornerRadius: Dp = 0.dp,
    threshold: Float = BottomSheetPartialExpendedFraction,
    fraction: Float = 0f,
): Dp {
    return if (isSearching) {
        minCornerRadius
    } else {
        if (
            bottomSheetState.isCollapsedOrWillBe()
        ) {
            maxCornerRadius
        } else {
            val max = BottomSheetPartialMaxFraction
            val min = 0f
            lerp(
                maxCornerRadius,
                minCornerRadius,
                (max - (max - fraction) / threshold).coerceIn(min, max)
            ).coerceAtLeast(0.dp)
        }
    }
}

inline fun <reified T : Parcelable> LocalBroadcastManager.sendBroadcast(
    message: T,
    key: String,
    action: String,
) {
    Timber.d(
        "Sending broadcast!\n" +
                "Message: $message\n" +
                "Key: $key\n" +
                "Action: $action"
    )
    val intent = Intent()
    intent.putExtra(key, message)
    intent.action = action
    sendBroadcast(intent)
}

fun LocalBroadcastManager.sendBroadcast(
    message: String,
    key: String,
    action: String,
) {
    Timber.d(
        "Sending broadcast!\n" +
                "Message: $message\n" +
                "Key: $key\n" +
                "Action: $action"
    )
    val intent = Intent()
    intent.putExtra(key, message)
    intent.action = action
    sendBroadcast(intent)
}

@OptIn(ExperimentalPermissionsApi::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    context: Context = LocalContext.current,
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    LaunchedEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent
        // and use dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        // setStatusBarColor() and setNavigationBarColor() also exist
    }
    ConstraintLayout {
        val (searchBar, scaffold) = createRefs()
        val scaffoldState = rememberBottomSheetScaffoldState()
        val bottomSheetState = scaffoldState.bottomSheetState
        sheetState = bottomSheetState
        val coroutineScope = rememberCoroutineScope()
        var isTextFieldFocused by remember { mutableStateOf(false) }
        var roundDp by remember { mutableStateOf(RoundedCornerRadius) }
        var shouldTriggerBottomSheetOnDrag by remember { mutableStateOf(true) }
        val softwareKeyboardController = LocalSoftwareKeyboardController.current
        val sheetCollapsing = bottomSheetState.isCollapsing()
        val focusManager = LocalFocusManager.current
        BackPressHandler {
            onHomeBackPress(isTextFieldFocused, focusManager, context)
        }
        LaunchedEffect(key1 = sheetCollapsing) {
            onSheetStateChanged(
                isTextFieldFocused,
                bottomSheetState,
                softwareKeyboardController
            )
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
                isSearching = isTextFieldFocused
                if (isTextFieldFocused) {
                    coroutineScope.launch {
                        // When searching, show search results on bottom sheet
                        bottomSheetState.expand()
                    }
                }
            }
        )
        BottomSheetScaffold(
            modifier = Modifier
                .constrainAs(scaffold) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            sheetContent = {
                BottomSheetScreen(
                    modifier = Modifier.imePadding(),
                    isSearching = isTextFieldFocused,
                    onBottomSheetFractionChange = {
                        roundDp = calculateCornerRadius(
                            isSearching = isTextFieldFocused,
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
                val locationPermissionState = rememberPermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION
                )
                MapboxMap( // Budapest University of Technology and Economics
                    modifier = Modifier.fillMaxSize(),
                    lat = ButeK.latitude,
                    lng = ButeK.longitude,
                    zoom = 12.0,
                    onMapLoaded = {
                        mapView = it
                        val pointAnnotationManager = it.annotations.createPointAnnotationManager()
                        val pointAnnotationOptions = PointAnnotationOptions()
                            // Define a geographic coordinate.
                            .withPoint(Point.fromLngLat(ButeK.longitude, ButeK.latitude))
                            // Specify the bitmap you assigned to the point annotation
                            // The bitmap will be added to map style automatically.
                            .withIconImage(
                                getBitmapFromVectorDrawable(
                                    context,
                                    R.drawable.ic_jay_marker_icon_v3_round
                                )
                            )
                        // Add the resulting pointAnnotation to the map.
                        pointAnnotationManager.create(pointAnnotationOptions)
                        when (locationPermissionState.status) {
                            is PermissionStatus.Granted -> {
                                it.location.turnOnWithDefaultPuck(context)
                            }

                            is PermissionStatus.Denied -> {
                                it.location.enabled = false
                            }
                        }
                    },
                    styleUri = "mapbox://styles/illyan/cl3kgeewz004k15ldn7x091r2",
                    centerPadding = PaddingValues(
                        bottom = SearchBarHeight
                    )
                )
            }
        }
    }
}

private fun onHomeBackPress(
    isTextFieldFocused: Boolean,
    focusManager: FocusManager,
    context: Context,
) {
    Timber.d("Handling back press from Home!")
    if (isTextFieldFocused) {
        // Remove the focus from the textfield
        focusManager.clearFocus()
    } else {
        (context as Activity).moveTaskToBack(false)
    }
}

private suspend fun onSheetStateChanged(
    isTextFieldFocused: Boolean,
    bottomSheetState: BottomSheetState,
    softwareKeyboardController: SoftwareKeyboardController?,
) {
    if (bottomSheetState.isCollapsing()) {
        // Close the keyboard when closing the bottom sheet
        if (isTextFieldFocused) {
            // If searching right now, expand bottom sheet
            // state after search screen is closed.
            bottomSheetState.expand()
        }
        softwareKeyboardController?.hide()
    }
}

@Preview(
    name = "Light mode",
    showBackground = true
)
@Preview(
    name = "Dark mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun BottomSearchBar(
    modifier: Modifier = Modifier,
    onDrag: (Float) -> Unit = {},
    bottomSheetState: BottomSheetState? = null,
    context: Context = LocalContext.current,
    onTextFieldFocusChanged: (FocusState) -> Unit = {},
    onDragAreaOffset: Dp = 48.dp,
) {
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
    val sheetCollapsing = bottomSheetState?.isCollapsing() ?: false
    LaunchedEffect(key1 = sheetCollapsing) {
        if (sheetCollapsing) {
            launch {
                focusRequester.freeFocus()
                focusManager.clearFocus()
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val draggable = Modifier.draggable(
        interactionSource = interactionSource,
        orientation = Orientation.Vertical,
        state = rememberDraggableState { onDrag(it) }
    )
    var searchFieldFocusState by remember { mutableStateOf<FocusState?>(null) }
    // We should help gestures when not searching
    // and bottomSheet is collapsed or collapsing
    val shouldHelpGestures = searchFieldFocusState?.isFocused == false &&
            bottomSheetState?.isCollapsedOrWillBe() == true
    ElevatedCard(
        modifier = modifier
            .then(if (shouldHelpGestures) draggable else Modifier)
            .offset(y = onDragAreaOffset),
        shape = RoundedCornerShape(
            topStart = RoundedCornerRadius,
            topEnd = RoundedCornerRadius
        ),
        colors = cardColors,
        onClick = { focusRequester.requestFocus() },
        interactionSource = interactionSource,
        elevation = cardElevation
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(draggable)
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
                        .onFocusChanged {
                            searchFieldFocusState = it
                            onTextFieldFocusChanged(it)
                        },
                    value = searchPlaceText,
                    onValueChange = {
                        searchPlaceText = it
                        LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(
                                searchPlaceText,
                                KeySearchQuery,
                                Intent.ACTION_SEARCH
                            )
                    },
                    label = { Text(stringResource(R.string.search)) },
                    placeholder = { Text(stringResource(R.string.where_to)) },
                    colors = colors,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                        onGo = { focusManager.clearFocus() },
                        onSearch = {
                            focusManager.clearFocus()
                            LocalBroadcastManager.getInstance(context)
                                .sendBroadcast(
                                    searchPlaceText,
                                    KeySearchSelected,
                                    ActionSearchSelected
                                )
                        }
                    ),
                    interactionSource = interactionSource,
                    trailingIcon = {
                        if (searchPlaceText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    searchPlaceText = ""
                                    focusRequester.requestFocus()
                                }
                            ) {
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
                    onClick = { /* TODO: show login dialog/screen */ },
                    modifier = Modifier.padding(AvatarPaddingValues),
                    interactionSource = interactionSource
                ) {
                    Image(
                        // Placeholder icon for now
                        painter = painterResource(R.drawable.ic_illyan_avatar_color),
                        contentDescription = stringResource(R.string.avatar_profile_picture),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(RoundedCornerRadius * 2)
                            .clip(CircleShape)
                            .background(Neutral95)
                    )
                }
            }
            Spacer(modifier = Modifier.height(onDragAreaOffset))
        }
    }
}

@Composable
fun BottomSheetScreen(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    onBottomSheetFractionChange: (Float) -> Unit = {},
) {
    val halfWayFraction = BottomSheetPartialExpendedFraction
    val fullScreenFraction = BottomSheetPartialMaxFraction
    ConstraintLayout(
        modifier = modifier
    ) {
        if (!isSearching) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
            }
        }
        if (isSearching) {
            onBottomSheetFractionChange(fullScreenFraction)
        } else {
            onBottomSheetFractionChange(halfWayFraction)
        }
        ConstraintLayout(
            modifier = modifier
        ) {
            val (menu, search) = createRefs()
            MenuNavHost(
                modifier = Modifier.constrainAs(menu) {
                    bottom.linkTo(parent.bottom)
                },
                isSearching = isSearching
            )
            SearchNavHost(
                modifier = Modifier.constrainAs(search) {
                    bottom.linkTo(parent.bottom)
                },
                isSearching = isSearching
            )
        }
    }
}

@Composable
private fun MenuNavHost(
    modifier: Modifier = Modifier,
    isSearching: Boolean,
) {
    val menuAlpha by animateFloatAsState(
        targetValue = if (isSearching) {
            0f
        } else {
            1f
        },
        animationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )
    val stiffness by animateFloatAsState(
        targetValue = if (isSearching) {
            Spring.StiffnessVeryLow
        } else {
            Spring.StiffnessLow
        }
    )
    val menuMaxHeight by animateDpAsState(
        targetValue = if (isSearching) {
            0.dp
        } else {
            600.dp
        },
        animationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = stiffness
        )
    )
    DestinationsNavHost(
        navGraph = NavGraphs.menu,
        modifier = modifier
            .heightIn(max = menuMaxHeight)
            .animateContentSize { _, _ -> }
            .alpha(alpha = menuAlpha)
            .navigationBarsPadding()
            .padding(bottom = SearchBarHeight - RoundedCornerRadius)
    )
}

@Composable
private fun SearchNavHost(
    modifier: Modifier = Modifier,
    isSearching: Boolean,
    fullScreenFraction: Float = BottomSheetPartialMaxFraction,
) {
    val searchAlpha by animateFloatAsState(
        targetValue = if (isSearching) {
            1f
        } else {
            0f
        },
        animationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )
    val searchFraction by animateFloatAsState(
        targetValue = if (isSearching) {
            fullScreenFraction
        } else {
            0f
        }
    )
    DestinationsNavHost(
        navGraph = NavGraphs.search,
        modifier = modifier
            .fillMaxHeight(fraction = searchFraction)
            .animateContentSize { _, _ -> }
            .alpha(alpha = searchAlpha)
            .navigationBarsPadding()
            .padding(bottom = SearchBarHeight - RoundedCornerRadius)
    )
}
