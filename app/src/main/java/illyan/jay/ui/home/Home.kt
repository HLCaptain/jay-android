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

@file:OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)

package illyan.jay.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.applyDefaultParams
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.BuildConfig
import illyan.jay.MainActivity
import illyan.jay.R
import illyan.jay.ui.NavGraphs
import illyan.jay.ui.components.AvatarAsyncImage
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.map.BmeK
import illyan.jay.ui.map.MapboxMap
import illyan.jay.ui.map.padding
import illyan.jay.ui.map.toEdgeInsets
import illyan.jay.ui.map.turnOnWithDefaultPuck
import illyan.jay.ui.menu.BackPressHandler
import illyan.jay.ui.poi.model.Place
import illyan.jay.ui.profile.ProfileDialog
import illyan.jay.ui.search.SearchViewModel
import illyan.jay.ui.search.SearchViewModel.Companion.KeySearchQuery
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.mapStyleUrl
import illyan.jay.util.extraOptions
import illyan.jay.util.isCollapsedOrWillBe
import illyan.jay.util.isCollapsing
import illyan.jay.util.isExpanding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

// FIXME: replace MutableStateFlows with CompositionLocalProviders

private val _mapView: MutableStateFlow<MapView?> = MutableStateFlow(null)
val mapView = _mapView.asStateFlow()
lateinit var sheetState: BottomSheetState
var isSearching: Boolean = false

private val _bottomSheetFraction = MutableStateFlow(0f)
val bottomSheetFraction = _bottomSheetFraction.asStateFlow()

val sheetMaxHeight = 680.dp
val sheetMinHeight = 100.dp

private val _sheetContentHeight = MutableStateFlow(0.dp)
val sheetContentHeight = _sheetContentHeight.asStateFlow()

private val _density = MutableStateFlow(2.75f)
val density = _density.asStateFlow()

private val _screenHeight = MutableStateFlow(0.dp)
val screenHeight = _screenHeight.asStateFlow()

private val _absoluteTop = MutableStateFlow(0.dp)
val absoluteTop = _absoluteTop.asStateFlow()

private val _absoluteBottom = MutableStateFlow(0.dp)
val absoluteBottom = _absoluteBottom.asStateFlow()

private val _cameraPadding = MutableStateFlow(PaddingValues())
val cameraPadding = _cameraPadding.asStateFlow()

fun tryFlyToLocation(
    extraCondition: () -> Boolean = { true },
    place: Place,
    zoom: Double = 12.0,
    extraCameraOptions: (CameraOptions.Builder) -> CameraOptions.Builder = { it },
    onFly: () -> Unit = {},
) {
    tryFlyToLocation(
        extraCondition = extraCondition,
        point = Point.fromLngLat(
            place.longitude,
            place.latitude
        ),
        zoom = zoom,
        extraCameraOptions = extraCameraOptions,
        onFly = onFly
    )
}

fun flyToLocation(
    extraCameraOptions: (CameraOptions.Builder) -> CameraOptions.Builder = { it },
) {
    Timber.d(
        "Focusing camera to location\n" +
                "Current sheetHeight: ${sheetState.getOffsetAsDp(density.value)}\n" +
                "Current sheetState:\n${sheetState.asString()}"
    )
    refreshCameraPadding()
    mapView.value?.camera?.flyTo(
        CameraOptions.Builder()
            .padding(
                cameraPadding.value,
                density.value
            )
            .extraOptions(extraCameraOptions)
            .build()
    )
}

/**
 * This method takes animations and sheet offset into consideration
 * before focusing the camera onto a location.
 *
 * Usually used after a navigation in NavHosts located on the BottomSheet.
 */
fun tryFlyToLocation(
    extraCondition: () -> Boolean = { true },
    point: Point,
    zoom: Double = 12.0,
    extraCameraOptions: (CameraOptions.Builder) -> CameraOptions.Builder = { it },
    onFly: () -> Unit = {},
) {
    if (!sheetState.isAnimationRunning &&
        sheetState.offset.value >= 10f &&
        extraCondition()
    ) {
        onFly()
        flyToLocation(
            extraCameraOptions = {
                it
                    .zoom(zoom)
                    .center(point)
                    .extraOptions(extraCameraOptions)
            }
        )
    }
}

fun tryFlyToPath(
    extraCondition: () -> Boolean = { true },
    path: List<Point>,
    extraCameraOptions: (CameraOptions.Builder) -> CameraOptions.Builder = { it },
    onFly: () -> Unit = {},
) {
    if (path.isEmpty()) return
    if (!sheetState.isAnimationRunning &&
        sheetState.offset.value >= 10f &&
        extraCondition()
    ) {
        onFly()
        flyToLocation {
            val cameraOptions = mapView.value?.getMapboxMap()?.cameraForCoordinates(
                coordinates = path,
                padding = cameraPadding.value.toEdgeInsets(density.value)
            )
            it
                .zoom(cameraOptions?.zoom?.times(0.95f))
                .center(cameraOptions?.center)
                .padding(cameraOptions?.padding)
                .extraOptions(extraCameraOptions)
        }

    }
}

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
    bottomSheetState: BottomSheetState,
    maxCornerRadius: Dp = RoundedCornerRadius,
    minCornerRadius: Dp = 0.dp,
    threshold: Float = BottomSheetPartialExpendedFraction,
    fraction: Float = 0f,
): Dp {
    return if (bottomSheetState.isCollapsedOrWillBe()) {
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

fun refreshCameraPadding() {
    val screenHeight = screenHeight.value
    val bottomSpace = screenHeight - absoluteBottom.value
    val topSpace = absoluteTop.value
    val sheetOffset = sheetState.getOffsetAsDp(density.value)
    _cameraPadding.value = PaddingValues(
        bottom = max(0.dp, screenHeight + bottomSpace + topSpace - sheetOffset)
    )
}

@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    context: Context = LocalContext.current,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.stopDanglingOngoingSessions()
        viewModel.loadLastLocation()
    }
    val cameraPaddingValues by cameraPadding.collectAsStateWithLifecycle()
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    DisposableEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.requestLocationUpdates()
            mapView.value?.location?.turnOnWithDefaultPuck(context)
        } else {
            mapView.value?.location?.enabled = false
        }
        onDispose { viewModel.dispose() }
    }
    val density = LocalDensity.current.density
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    LaunchedEffect(density) { _density.value = density }
    LaunchedEffect(screenHeightDp) { _screenHeight.value = screenHeightDp }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                var topSet = false
                val absoluteTopPosition = (coordinates.positionInWindow().y / density).dp
                if (_absoluteTop.value != absoluteTopPosition) {
                    _absoluteTop.value = absoluteTopPosition
                    topSet = true
                }
                var bottomSet = false
                val absoluteBottomPosition =
                    ((coordinates.positionInWindow().y + coordinates.size.height) / density).dp
                if (_absoluteBottom.value != absoluteBottomPosition) {
                    bottomSet = true
                    _absoluteBottom.value = absoluteBottomPosition
                }
                if (topSet || bottomSet) {
                    refreshCameraPadding()
                    Timber.d(
                        "Camera bottom padding: ${
                            absoluteBottomPosition - sheetState.getOffsetAsDp(
                                density
                            )
                        }"
                    )
                }
            }
    ) {
        val (searchBar, scaffold) = createRefs()
        val scaffoldState = rememberBottomSheetScaffoldState()
        val bottomSheetState = scaffoldState.bottomSheetState
        sheetState = bottomSheetState
        var isTextFieldFocused by remember { mutableStateOf(false) }
        var roundDp by remember { mutableStateOf(RoundedCornerRadius) }
        var shouldTriggerBottomSheetOnDrag by remember { mutableStateOf(true) }
        val softwareKeyboardController = LocalSoftwareKeyboardController.current
        val sheetCollapsing = bottomSheetState.isCollapsing()
        val focusManager = LocalFocusManager.current
        BackPressHandler {
            onHomeBackPress(isTextFieldFocused, focusManager, context)
        }
        LaunchedEffect(bottomSheetState.offset) { refreshCameraPadding() }
        LaunchedEffect(sheetCollapsing) {
            onSheetStateChanged(
                isTextFieldFocused,
                bottomSheetState,
                softwareKeyboardController
            )
        }
        // When the bottom sheet reaches its target state, reset onDrag trigger
        LaunchedEffect(bottomSheetState.targetValue, bottomSheetState.currentValue) {
            if (bottomSheetState.targetValue == bottomSheetState.currentValue) {
                // Resetting the trigger to enable bottom sheet toggle
                shouldTriggerBottomSheetOnDrag = true
            }
        }
        var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
        ProfileDialog(
            isDialogOpen = isProfileDialogShowing,
            onDialogClosed = { isProfileDialogShowing = false }
        )
        val isUserSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
        val userPhotoUrl by viewModel.userPhotoUrl.collectAsStateWithLifecycle()
        var searchQuery by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(searchQuery) {
            if (searchQuery.isBlank()) return@LaunchedEffect

            // Wait 400ms before querying anything
            delay(400)
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(
                    searchQuery,
                    KeySearchQuery,
                    Intent.ACTION_SEARCH
                )
        }
        BottomSearchBar(
            modifier = Modifier
                .zIndex(1f) // Search bar is in front of everything else
                .constrainAs(searchBar) {
                    bottom.linkTo(scaffold.bottom)
                }
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding(),
            isUserSignedIn = isUserSignedIn,
            userPhotoUrl = userPhotoUrl,
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
            },
            onSearchQueryChanged = {searchQuery = it },
            onSearchQueried = {
                LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(
                        it,
                        SearchViewModel.KeySearchSelected,
                        SearchViewModel.ActionSearchSelected
                    )
            },
            onShowProfile = { isProfileDialogShowing = true }
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
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = SearchBarHeight - RoundedCornerRadius / 4f)
            ) {
                val initialLocationLoaded by viewModel.initialLocationLoaded.collectAsStateWithLifecycle()
                val cameraOptionsBuilder by viewModel.cameraOptionsBuilder.collectAsStateWithLifecycle()
                // Grace period is useful when we would like to initialize the map
                // with the user's location in focus.
                // If it ends, it defaults to the middle of the Earth.

                var didLoadInLocation by rememberSaveable { mutableStateOf(false) }
                var didLoadInLocationWithoutPermissions by rememberSaveable { mutableStateOf(false) }
                var isMapInitialized by rememberSaveable { mutableStateOf(false) }
                val sheetContentHeight by sheetContentHeight.collectAsStateWithLifecycle()
                LaunchedEffect(
                    bottomSheetState.getOffsetAsDp(density),
                    isMapInitialized,
                    initialLocationLoaded
                ) {
                    refreshCameraPadding()
                    // Permissions probably granted because there is a location to focus on
                    if (bottomSheetState.isExpanded &&
                        !didLoadInLocation &&
                        cameraOptionsBuilder != null &&
                        initialLocationLoaded &&
                        isMapInitialized &&
                        bottomSheetState.progress.to == BottomSheetValue.Expanded &&
                        sheetContentHeight >= 20.dp &&
                        _cameraPadding.value.calculateBottomPadding() >= 20.dp
                    ) {
                        Timber.d(
                            "Focusing camera to location\n" +
                                    "Current sheetHeight: ${bottomSheetState.getOffsetAsDp(density)}\n" +
                                    "Current sheetState:\n${sheetState.asString()}" +
                                    "Sheet content height = $sheetContentHeight"
                        )
                        didLoadInLocation = true
                        mapView.value?.camera?.flyTo(
                            cameraOptionsBuilder!!
                                .center(
                                    viewModel.initialLocation.value?.let {
                                        Point.fromLngLat(
                                            it.longitude,
                                            it.latitude
                                        )
                                    }
                                )
                                .padding(
                                    _cameraPadding.value, context
                                )
                                .build()
                        )
                    }
                    // Permissions not granted
                    if (bottomSheetState.isExpanded &&
                        !didLoadInLocationWithoutPermissions &&
                        !locationPermissionState.status.isGranted &&
                        isMapInitialized &&
                        bottomSheetState.progress.to == BottomSheetValue.Expanded &&
                        sheetContentHeight >= 20.dp &&
                        _cameraPadding.value.calculateBottomPadding() >= 20.dp
                    ) {
                        Timber.d(
                            "Focusing camera to location" +
                                    "Current sheetHeight: ${bottomSheetState.getOffsetAsDp(density)}\n" +
                                    "Current sheetState:\n${sheetState.asString()}\n" +
                                    "Sheet content height = $sheetContentHeight"
                        )
                        didLoadInLocationWithoutPermissions = true
                        mapView.value?.camera?.flyTo(
                            CameraOptions.Builder()
                                .zoom(4.0)
                                .center(
                                    Point.fromLngLat(
                                        BmeK.longitude,
                                        BmeK.latitude
                                    )
                                )
                                .padding(
                                    _cameraPadding.value, context
                                )
                                .build()
                        )
                    }
                }
                if (initialLocationLoaded || cameraOptionsBuilder != null) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (foreground, map) = createRefs()
                        var isMapVisible by rememberSaveable { mutableStateOf(false) }
                        androidx.compose.animation.AnimatedVisibility(
                            modifier = Modifier
                                .zIndex(1f)
                                .constrainAs(foreground) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                },
                            visible = !isMapVisible,
                            exit = fadeOut(animationSpec = tween(800))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                        val styleUrl by mapStyleUrl.collectAsStateWithLifecycle()
                        MapboxMap(
                            // Budapest University of Technology and Economics
                            modifier = Modifier
                                .fillMaxSize()
                                .constrainAs(map) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                },
                            cameraOptionsBuilder = cameraOptionsBuilder?.padding(
                                cameraPaddingValues, context
                            ) ?: CameraOptions.Builder()
                                .center(
                                    Point.fromLngLat(
                                        BmeK.longitude,
                                        BmeK.latitude
                                    )
                                )
                                .zoom(4.0),
                            resourceOptions = ResourceOptions.Builder().applyDefaultParams(context)
                                .accessToken(BuildConfig.MapboxAccessToken)
                                .build(),
                            onMapFullyLoaded = { isMapVisible = true },
                            onMapInitialized = {
                                isMapInitialized = true
                                _mapView.value = it
                                when (locationPermissionState.status) {
                                    is PermissionStatus.Granted -> {
                                        it.location.turnOnWithDefaultPuck(context)
                                    }

                                    is PermissionStatus.Denied -> {
                                        it.location.enabled = false
                                    }
                                }
                            },
                            styleUri = { styleUrl },
                        )
                    }
                }
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
    if (sheetState.isCollapsedOrWillBe()) (context as MainActivity).moveTaskToBack(false)
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

@Composable
fun BottomSearchBar(
    modifier: Modifier = Modifier,
    onDrag: (Float) -> Unit = {},
    bottomSheetState: BottomSheetState? = null,
    onTextFieldFocusChanged: (FocusState) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchQueried: (String) -> Unit = {},
    isUserSignedIn: Boolean = false,
    userPhotoUrl: Uri? = null,
    onShowProfile: () -> Unit = {},
    onDragAreaOffset: Dp = 48.dp,
) {
    val elevation = 8.dp
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = elevation,
        disabledElevation = elevation,
        draggedElevation = elevation,
        focusedElevation = elevation,
        hoveredElevation = elevation,
        pressedElevation = elevation
    )
    val cardColors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val sheetCollapsing = bottomSheetState?.isCollapsing() ?: false
    LaunchedEffect(sheetCollapsing) {
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
    var searchFieldFocusState by rememberSaveable { mutableStateOf<FocusState?>(null) }
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
        onClick = { focusRequester.requestFocus() },
        colors = cardColors,
        elevation = cardElevation
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SearchBarHeight),
                horizontalArrangement = Arrangement.spacedBy(SearchBarSpaceBetween),
            ) {
                IconButton(
                    onClick = { focusRequester.requestFocus() },
                    modifier = Modifier.padding(SearchMarkerPaddingValues)
                ) {
                    Image(
                        modifier = Modifier
                            .zIndex(1f)
                            .size(RoundedCornerRadius * 1.5f),
                        painter = painterResource(R.drawable.jay_marker_icon_v3_round),
                        contentDescription = stringResource(R.string.search_marker_icon)
                    )
                }
                val colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
                var searchPlaceText by rememberSaveable { mutableStateOf("") }
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(SearchFieldPaddingValues)
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            searchFieldFocusState = it
                            onTextFieldFocusChanged(it)
                        },
                    value = searchPlaceText,
                    onValueChange = {
                        searchPlaceText = it
                        onSearchQueryChanged(searchPlaceText)
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
                            onSearchQueried(searchPlaceText)
                        }
                    ),
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
                                )
                            }
                        }
                    }
                )
                IconButton(
                    onClick = onShowProfile,
                    modifier = Modifier.padding(AvatarPaddingValues),
                ) {
                    AvatarAsyncImage(
                        modifier = Modifier
                            .zIndex(1f)
                            .size(RoundedCornerRadius * 2)
                            .clip(CircleShape),
                        placeholderEnabled = !isUserSignedIn || userPhotoUrl == null,
                        userPhotoUrl = userPhotoUrl
                    )
                }
            }
            Spacer(modifier = Modifier.height(onDragAreaOffset))
        }
    }
}

@PreviewLightDarkTheme
@Composable
fun BottomSearchBarPreview() {
    JayTheme {
        BottomSearchBar()
    }
}

@Composable
fun BottomSheetScreen(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    onBottomSheetFractionChange: (Float) -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp))
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                _sheetContentHeight.value = (placeable.height / density).dp
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            },
    ) {
        val fraction by bottomSheetFraction.collectAsStateWithLifecycle()
        // TODO: maybe A/B test these two animations or idk
        // V1 is sliding in and out the whole width of the screen
        AnimatedVisibility(visible = fraction < 0.999f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // V2 is a more basic *appear* *disappear* animation
                AnimatedVisibility(visible = true) {
                    Surface(
                        modifier = Modifier
                            .width(24.dp)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {}
                }
            }
        }
        val screenHeight by _screenHeight.collectAsStateWithLifecycle()
        val offset by sheetState.offset
        val density = LocalDensity.current
        val bottomSheetFraction = 1 - offset / (screenHeight.value * density.density)
        LaunchedEffect(bottomSheetFraction) {
            _bottomSheetFraction.value = bottomSheetFraction
        }
        onBottomSheetFractionChange(bottomSheetFraction)
        ConstraintLayout(
            modifier = modifier
        ) {
            val (menu, search) = createRefs()
            SheetNavHost(
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
private fun SheetNavHost(
    modifier: Modifier = Modifier,
    isSearching: Boolean,
) {
    val sheetAlpha by animateFloatAsState(
        targetValue = if (isSearching) {
            0f
        } else {
            1f
        },
        animationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val density = LocalDensity.current.density
    DestinationsNavHost(
        navGraph = NavGraphs.sheet,
        modifier = modifier
            .heightIn(max = sheetMaxHeight)
            .navigationBarsPadding()
            .padding(bottom = SearchBarHeight - RoundedCornerRadius)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val height = placeable.measuredHeight.toDp()
                if (sheetState.isExpanded &&
                    sheetState.progress.fraction == 1f &&
                    height >= sheetMinHeight &&
                    height != sheetState.getOffsetAsDp(density)
                ) {
                    refreshCameraPadding()
                }
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
            .alpha(alpha = sheetAlpha),
        engine = rememberAnimatedNavHostEngine(
            rootDefaultAnimations = RootNavGraphDefaultAnimations(
                enterTransition = {
                    slideInVertically(tween(200)) + fadeIn(tween(200))
                },
                exitTransition = {
                    slideOutVertically(tween(200)) + fadeOut(tween(200))
                }
            )
        )
    )
}

@Composable
private fun SearchNavHost(
    modifier: Modifier = Modifier,
    isSearching: Boolean,
    fullScreenFraction: Float = BottomSheetPartialMaxFraction,
) {
    val coroutineScope = rememberCoroutineScope()
    BackPressHandler(
        customDisposableEffectKey = isSearching,
        isEnabled = { isSearching }
    ) {
        if (isSearching) {
            coroutineScope.launch {
                sheetState.collapse()
            }
        }
    }
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
            .padding(bottom = SearchBarHeight - RoundedCornerRadius)
            .navigationBarsPadding(),
    )
}

fun BottomSheetState.asString(density: Float = 2.75f): String {
    return "isExpanded ${isExpanded}\n" +
            "isExpanding ${isExpanding()}\n" +
            "isCollapsed ${isCollapsed}\n" +
            "isCollapsing ${isCollapsing()}\n" +
            "targetValue ${targetValue.name}\n" +
            "currentValue ${currentValue.name}\n" +
            "direction ${direction}\n" +
            "isAnimationRunning ${isAnimationRunning}\n" +
            "offset ${getOffsetAsDp(density)}\n" +
            "overflow ${overflow.value}\n" +
            "progress.fraction ${progress.fraction}\n" +
            "progress.from ${progress.from.name}\n" +
            "progress.to ${progress.to.name}\n"
}

fun BottomSheetState.getOffsetAsDp(density: Float): Dp {
    return (offset.value / density).dp
}