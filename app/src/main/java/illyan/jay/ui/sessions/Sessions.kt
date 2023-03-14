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

package illyan.jay.ui.sessions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddChart
import androidx.compose.material.icons.rounded.ArrowRightAlt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.LatLng
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.compose.scrollbar.drawVerticalScrollbar
import illyan.jay.R
import illyan.jay.ui.components.MediumCircularProgressIndicator
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.components.SmallCircularProgressIndicator
import illyan.jay.ui.components.TooltipButton
import illyan.jay.ui.destinations.SessionScreenDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.signatureBlue
import illyan.jay.util.cardPlaceholder
import illyan.jay.util.format
import illyan.jay.util.plus
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.math.RoundingMode
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

val DefaultContentPadding = PaddingValues(
    bottom = RoundedCornerRadius
)

val DefaultScreenOnSheetPadding = PaddingValues(
    top = MenuItemPadding * 2
)

@MenuNavGraph
@Destination
@Composable
fun Sessions(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: SessionsViewModel = hiltViewModel(),
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    val signedInUser by viewModel.signedInUser.collectAsStateWithLifecycle()
    val areThereSessionsNotOwned by viewModel.areThereSessionsNotOwned.collectAsStateWithLifecycle()
    val canDeleteSessions by viewModel.canDeleteSessionsLocally.collectAsStateWithLifecycle()
    val syncedSessions by viewModel.syncedSessions.collectAsStateWithLifecycle()
    val canSyncSessions by viewModel.canSyncSessions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
    val sessionUUIDs by viewModel.allSessionUUIDs.collectAsStateWithLifecycle()
    val syncedSessionsLoading by viewModel.syncedSessionsLoading.collectAsStateWithLifecycle()
    val localSessionsLoading by viewModel.localSessionsLoading.collectAsStateWithLifecycle()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    LaunchedEffect(signedInUser, isSystemInDarkTheme) {
        viewModel.reloadData()
    }
    SessionsScreen(
        isUserSignedIn = isUserSignedIn,
        canSyncSessions = canSyncSessions,
        areThereSyncedSessions = syncedSessions.isNotEmpty(),
        areThereSessionsNotOwned = areThereSessionsNotOwned,
        canDeleteSessions = canDeleteSessions,
        isLoadingSessionsFromCloud = syncedSessionsLoading,
        isLoadingSessionsLocally = localSessionsLoading,
        isLoading = isLoading,
        sessionUUIDs = sessionUUIDs,
        ownSession = viewModel::ownSession,
        syncSessions = viewModel::syncSessions,
        syncSession = viewModel::syncSession,
        deleteSession = viewModel::deleteSession,
        ownAllSessions = viewModel::ownAllSessions,
        deleteAllSyncedData = viewModel::deleteSyncedSessions,
        deleteSessionsLocally = viewModel::deleteSessionsLocally,
        deleteSessionFromCloud = viewModel::deleteSessionFromCloud,
        onSessionSelected = {
            destinationsNavigator.navigate(
                SessionScreenDestination(
                    sessionUUID = it
                )
            )
        },
        disposeSessionStateFlow = viewModel::disposeSessionStateFlow,
        getSessionStateFlow = { viewModel.getSessionStateFlow(it).collectAsStateWithLifecycle() }
    )
}

@Composable
fun SessionsScreen(
    isUserSignedIn: Boolean = false,
    canSyncSessions: Boolean = false,
    areThereSyncedSessions: Boolean = false,
    areThereSessionsNotOwned: Boolean = false,
    canDeleteSessions: Boolean = false,
    isLoadingSessionsFromCloud: Boolean = false,
    isLoadingSessionsLocally: Boolean = false,
    isLoading: Boolean = isLoadingSessionsFromCloud || isLoadingSessionsLocally,
    sessionUUIDs: List<String> = emptyList(),
    ownSession: (String) -> Unit = {},
    syncSessions: () -> Unit = {},
    syncSession: (String) -> Unit = {},
    deleteSession: (String) -> Unit = {},
    ownAllSessions: () -> Unit = {},
    deleteAllSyncedData: () -> Unit = {},
    deleteSessionsLocally: () -> Unit = {},
    deleteSessionFromCloud: (String) -> Unit = {},
    onSessionSelected: (String) -> Unit = {},
    disposeSessionStateFlow: (String) -> Unit = {},
    getSessionStateFlow: @Composable (String) -> State<UiSession?> = {
        remember { mutableStateOf(null) }
    },
) {
    val showButtons = isUserSignedIn &&
            (canSyncSessions || areThereSyncedSessions || areThereSessionsNotOwned) ||
            canDeleteSessions
    ConstraintLayout(
        modifier = Modifier.padding(
            DefaultContentPadding + if (!showButtons) {
                DefaultScreenOnSheetPadding
            } else PaddingValues()
        )
    ) {
        val (column, globalLoadingIndicator, buttons) = createRefs()
        AnimatedVisibility(
            modifier = Modifier
                .constrainAs(globalLoadingIndicator) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
            visible = isLoading
        ) {
            MediumCircularProgressIndicator(modifier = Modifier.padding(end = MenuItemPadding * 2))
        }
        SessionsInteractorButtonList(
            modifier = Modifier
                .zIndex(2f)
                .constrainAs(buttons) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .padding(
                    start = MenuItemPadding,
                    bottom = MenuItemPadding,
                ),
            showSyncButton = isUserSignedIn && canSyncSessions,
            showOwnAllSessionsButton = isUserSignedIn && areThereSessionsNotOwned,
            showDeleteSessionsFromCloudButton = isUserSignedIn && areThereSyncedSessions,
            showDeleteSessionsLocallyButton = canDeleteSessions,
            onSyncSessions = syncSessions,
            onOwnAllSession = ownAllSessions,
            onDeleteSessionsFromCloud = deleteAllSyncedData,
            onDeleteSessionsLocally = deleteSessionsLocally,
        )
        Column(
            modifier = Modifier
                .constrainAs(column) {
                    top.linkTo(parent.top)
                    bottom.linkTo(buttons.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(
                    top = MenuItemPadding,
                    bottom = MenuItemPadding + RoundedCornerRadius,
                )
        ) {
            SessionsList(
                modifier = Modifier.fillMaxWidth(),
                onSessionSelected = onSessionSelected,
                contentPadding = PaddingValues(horizontal = MenuItemPadding * 2),
                isUserSignedIn = isUserSignedIn,
                sessionUUIDs = sessionUUIDs,
                ownSession = ownSession,
                syncSession = syncSession,
                deleteSession = deleteSession,
                deleteSessionFromCloud = deleteSessionFromCloud,
                showNoSessionPrompt = sessionUUIDs.isEmpty() && !isLoading,
                loadingSessionsFromCloud = isLoadingSessionsFromCloud,
                loadingSessionsLocally = isLoadingSessionsLocally,
                disposeSessionStateFlow = disposeSessionStateFlow,
                getSessionStateFlow = getSessionStateFlow,
            )
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun SessionsScreenPreview() {
    val sessions = generateUiSessions(10)
    val areThereSessionsNotOwned = sessions.any { !it.isOwned }
    val areThereSyncedSessions = sessions.any { it.isSynced }
    val canDeleteSessions = sessions.any { it.isLocal }
    JayTheme {
        SessionsScreen(
            isUserSignedIn = true,
            canSyncSessions = areThereSyncedSessions,
            areThereSyncedSessions = areThereSyncedSessions,
            areThereSessionsNotOwned = areThereSessionsNotOwned,
            canDeleteSessions = canDeleteSessions,
            isLoadingSessionsFromCloud = false,
            isLoadingSessionsLocally = false,
            sessionUUIDs = sessions.map { it.uuid },
            getSessionStateFlow = { uuid -> remember { mutableStateOf(sessions.first { it.uuid == uuid }) } },
        )
    }
}

private fun generateUiSessions(number: Int): List<UiSession> {
    return List(number) {
        val now = ZonedDateTime.now()
        val startTime = now.minusSeconds(Random.nextLong(5000, 10000))
        val endTime = if (Random.nextInt(3) == 0) null else now.minusSeconds(Random.nextLong(1000, 4000))
        val ownerUUID = UUID.randomUUID().toString()
        UiSession(
            uuid = UUID.randomUUID().toString(),
            startDateTime = startTime,
            endDateTime = endTime,
            isLocal = Random.nextBoolean(),
            ownerUUID = if (Random.nextBoolean()) null else ownerUUID,
            clientUUID = if (Random.nextBoolean()) ownerUUID else UUID.randomUUID().toString(),
            isSynced = Random.nextBoolean(),
            startLocationName = "City number $it",
            endLocationName = "City number ${Random.nextInt(it + 1)}",
            totalDistance = Random.nextDouble(100.0, 10000.0),
            duration = ((endTime?.toEpochSecond() ?: now.toEpochSecond()) - startTime.toEpochSecond()).seconds,
            endCoordinate = LatLng(Random.nextDouble(-90.0, 90.0), Random.nextDouble(-90.0, 90.0)),
            startCoordinate = LatLng(
                Random.nextDouble(-90.0, 90.0),
                Random.nextDouble(-90.0, 90.0)
            ),
        )
    }
}

@Composable
fun SessionsInteractorButtonList(
    modifier: Modifier = Modifier,
    showSyncButton: Boolean = false,
    showOwnAllSessionsButton: Boolean = false,
    showDeleteSessionsFromCloudButton: Boolean = false,
    showDeleteSessionsLocallyButton: Boolean = false,
    onSyncSessions: () -> Unit = {},
    onOwnAllSession: () -> Unit = {},
    onDeleteSessionsFromCloud: () -> Unit = {},
    onDeleteSessionsLocally: () -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
    ) {
        item {
            SessionInteractionButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = stringResource(R.string.sync),
                imageVector = Icons.Rounded.CloudUpload,
                visibility = showSyncButton,
                enabled = showSyncButton,
                onClick = onSyncSessions,
            )
        }
        item {
            SessionInteractionButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = stringResource(R.string.delete_from_cloud),
                imageVector = Icons.Rounded.CloudOff,
                visibility = showDeleteSessionsFromCloudButton,
                enabled = showDeleteSessionsFromCloudButton,
                onClick = onDeleteSessionsFromCloud,
            )
        }
        item {
            SessionInteractionButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = stringResource(R.string.delete_locally),
                imageVector = Icons.Rounded.Delete,
                visibility = showDeleteSessionsLocallyButton,
                enabled = showDeleteSessionsLocallyButton,
                onClick = onDeleteSessionsLocally,
            )
        }
        item {
            SessionInteractionButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = stringResource(R.string.own_all_sessions),
                imageVector = Icons.Rounded.AddChart,
                visibility = showOwnAllSessionsButton,
                enabled = showOwnAllSessionsButton,
                onClick = onOwnAllSession,
            )
        }
    }
}

@Composable
fun SessionInteractionButton(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.unknown),
    imageVector: ImageVector? = null,
    visibility: Boolean = true,
    enabled: Boolean = visibility,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visibility && enabled,
        modifier = modifier,
    ) {
        TooltipButton(
            onClick = onClick,
            tooltip = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge
                )
            },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(visible = imageVector != null) {
                    if (imageVector != null) {
                        Icon(imageVector = imageVector, contentDescription = "")
                    }
                }
                Text(text = text)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionsList(
    modifier: Modifier = Modifier,
    onSessionSelected: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    isUserSignedIn: Boolean = false,
    loadingSessionsFromCloud: Boolean = false,
    loadingSessionsLocally: Boolean = false,
    sessionUUIDs: List<String> = emptyList(),
    showNoSessionPrompt: Boolean = sessionUUIDs.isEmpty() && !loadingSessionsFromCloud && !loadingSessionsLocally,
    ownSession: (String) -> Unit = {},
    deleteSession: (String) -> Unit = {},
    deleteSessionFromCloud: (String) -> Unit = {},
    syncSession: (String) -> Unit = {},
    disposeSessionStateFlow: (String) -> Unit = {},
    getSessionStateFlow: @Composable (String) -> State<UiSession?> = { remember { mutableStateOf(null) } },
    emptyListPlaceholder: @Composable () -> Unit = {
        AnimatedVisibility(visible = showNoSessionPrompt) {
            NoSessionPrompt(modifier = Modifier.padding(start = MenuItemPadding * 2, bottom = 8.dp))
        }
    },
) {
    val lazyListState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current
    Column(
        modifier = modifier
            .drawVerticalScrollbar(
                state = lazyListState,
                reverseScrolling = true,
                topPadding = DefaultContentPadding.calculateBottomPadding() + DefaultScreenOnSheetPadding.calculateTopPadding() / 2 + contentPadding.calculateTopPadding(),
                bottomPadding = contentPadding.calculateBottomPadding(),
            )
    ) {
        AnimatedVisibility(visible = showNoSessionPrompt) {
            emptyListPlaceholder()
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    top = DefaultContentPadding.calculateBottomPadding() + DefaultScreenOnSheetPadding.calculateTopPadding() / 2
                )
                .clip(RoundedCornerShape(12.dp)),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(MenuItemPadding),
            reverseLayout = true,
            state = lazyListState,
        ) {
            if (loadingSessionsFromCloud) {
                item {
                    SessionLoadingIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = stringResource(R.string.loading_sessions_from_cloud)
                    )
                }
            }
            if (loadingSessionsLocally) {
                item {
                    SessionLoadingIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = stringResource(R.string.loading_sessions)
                    )
                }
            }
            items(sessionUUIDs, key = { it }) {
                val session by getSessionStateFlow(it)
                DisposableEffect(Unit) {
                    onDispose {
                        disposeSessionStateFlow(it)
                    }
                }
                val isPlaceholderVisible = session == null
                SessionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardPlaceholder(isPlaceholderVisible)
                        .animateItemPlacement(),
                    session = session,
                    onClick = onSessionSelected,
                    onSync = { syncSession(it) },
                    onDelete = { deleteSession(it) },
                    onDeleteFromCloud = { deleteSessionFromCloud(it) }
                ) {
                    OwnButton(
                        visible = session != null && isUserSignedIn && session!!.isNotOwned,
                        onClick = { ownSession(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoSessionPrompt(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.no_sessions_to_show),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun OwnButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible
    ) {
        Button(
            modifier = modifier,
            onClick = onClick,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = "")
                Text(text = stringResource(R.string.own))
            }
        }
    }
}

@Composable
fun SessionLoadingIndicator(
    modifier: Modifier = Modifier,
    text: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallCircularProgressIndicator()
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SessionLoadingIndicatorPreview() {
    JayTheme {
        SessionLoadingIndicator(text = stringResource(id = R.string.loading_sessions))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCard(
    modifier: Modifier = Modifier,
    session: UiSession? = null,
    onClick: (String) -> Unit = {},
    onDelete: () -> Unit = {},
    onDeleteFromCloud: () -> Unit = {},
    onSync: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val isDeleteFromCloudEnabled = session?.isSynced == true && session.isNotOngoing
    val deleteFromCloudAction = sessionSwipeAction(
        icon = Icons.Rounded.CloudOff,
        enabled = isDeleteFromCloudEnabled,
        enabledBackgroundColor = MaterialTheme.colorScheme.tertiary,
        onSwipe = onDeleteFromCloud
    )
    val isDeleteEnabled = session?.canDelete == true && session.isNotOngoing
    val deleteAction = sessionSwipeAction(
        icon = Icons.Rounded.Delete,
        enabled = isDeleteEnabled,
        enabledBackgroundColor = MaterialTheme.colorScheme.error,
        onSwipe = onDelete
    )
    val isSyncEnabled = session?.isSynced == false && session.isNotOngoing
    val syncAction = sessionSwipeAction(
        icon = Icons.Rounded.SyncAlt,
        enabled = isSyncEnabled,
        enabledBackgroundColor = MaterialTheme.signatureBlue,
        onSwipe = onSync
    )
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    Card(
        modifier = modifier,
        onClick = { session?.let { onClick(it.uuid) } },
        colors = cardColors,
    ) {
        SwipeableActionsBox(
            startActions = listOf(syncAction),
            endActions = listOf(deleteFromCloudAction, deleteAction),
            backgroundUntilSwipeThreshold = containerColor,
            swipeThreshold = 80.dp,
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            start = 8.dp,
                            end = 6.dp,
                            top = 4.dp,
                            bottom = 4.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Crossfade(
                                modifier = Modifier.animateContentSize(),
                                targetState = session?.startLocationName
                            ) {
                                Text(
                                    text = it ?: stringResource(R.string.unknown),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.ArrowRightAlt, contentDescription = "",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                            Crossfade(
                                modifier = Modifier.animateContentSize(),
                                targetState = (session?.endDateTime == null) to session?.endLocationName
                            ) {
                                if (it.first) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreHoriz,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                } else {
                                    Text(
                                        text = it.second ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.End
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AnimatedVisibility(visible = session?.isLocal == true) {
                                    Icon(imageVector = Icons.Rounded.Save, contentDescription = "")
                                }
                                AnimatedVisibility(visible = session?.isSynced == true) {
                                    Icon(imageVector = Icons.Rounded.CloudSync, contentDescription = "")
                                }
                                AnimatedVisibility(visible = session?.isNotOwned == true) {
                                    Icon(imageVector = Icons.Rounded.PersonOff, contentDescription = "")
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SessionDetailsList(
                            details = listOf(
                                stringResource(R.string.distance) to if (session?.totalDistance == null) {
                                    stringResource(R.string.unknown)
                                } else {
                                    "${
                                        session.totalDistance
                                            .div(1000)
                                            .toBigDecimal()
                                            .setScale(2, RoundingMode.FLOOR)
                                    } " + stringResource(R.string.kilometers)
                                },
                                stringResource(R.string.duration) to (session?.duration?.format(
                                    separator = " ",
                                    second = stringResource(R.string.second_short),
                                    minute = stringResource(R.string.minute_short),
                                    hour = stringResource(R.string.hour_short),
                                    day = stringResource(R.string.day_short)
                                ) ?: stringResource(R.string.unknown))
                            ),
                        )
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun sessionSwipeAction(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onSwipe: () -> Unit = {},
    enabledBackgroundColor: Color = Color.Transparent,
    disabledBackgroundColor: Color = MaterialTheme.colorScheme.surface
): SwipeAction {
    return SwipeAction(
        icon = {
            Icon(
                modifier = modifier.padding(horizontal = 32.dp),
                imageVector = icon,
                contentDescription = "",
                tint = tint
            )
        },
        background = if (enabled) enabledBackgroundColor else disabledBackgroundColor,
        onSwipe = if (enabled) onSwipe else {{}},
    )
}

@Composable
fun SessionDetailsList(
    details: List<Pair<String, String>> = emptyList()
) {
    Column {
        details.forEach {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${it.first}:",
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = it.second,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun SessionCardPreview() {
    JayTheme {
        SessionCard()
    }
}
