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

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.SmallCircularProgressIndicator
import illyan.jay.ui.destinations.SessionScreenDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.theme.Neutral95
import illyan.jay.util.cardPlaceholder
import illyan.jay.util.format
import illyan.jay.util.minus
import java.math.RoundingMode

val DefaultContentPadding = PaddingValues(
    bottom = MenuItemPadding * 2
)

val DefaultScreenOnSheetPadding = PaddingValues(
    top = MenuItemPadding * 2,
    bottom = RoundedCornerRadius
)

@MenuNavGraph
@Destination
@Composable
fun SessionsScreen(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
    viewModel: SessionsViewModel = hiltViewModel(),
) {
    SheetScreenBackPressHandler(destinationsNavigator = destinationsNavigator)
    val context = LocalContext.current
    val signedInUser by viewModel.signedInUser.collectAsState()
    val areThereSessionsNotOwned by viewModel.areThereSessionsNotOwned.collectAsState()
    val canDeleteSessions by viewModel.canDeleteSessionsLocally.collectAsState()
    val syncedSessions by viewModel.syncedSessions.collectAsState()
    val canSyncSessions by viewModel.canSyncSessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    val showButtons = isUserSignedIn &&
            (canSyncSessions || syncedSessions.isNotEmpty() || areThereSessionsNotOwned) ||
            canDeleteSessions
    LaunchedEffect(signedInUser) {
        viewModel.loadLocalSessions()
        viewModel.loadCloudSessions(context as Activity)
    }
    ConstraintLayout(
        modifier = Modifier.padding(
            if (showButtons) {
                DefaultScreenOnSheetPadding - PaddingValues(
                    top = DefaultScreenOnSheetPadding.calculateTopPadding()
                )
            } else {
                DefaultScreenOnSheetPadding
            }
        )
    ) {
        val (column, globalLoadingIndicator) = createRefs()
        AnimatedVisibility(
            modifier = Modifier
                .constrainAs(globalLoadingIndicator) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
            visible = isLoading
        ) {
            SmallCircularProgressIndicator(
                modifier = Modifier.padding(8.dp)
            )
        }
        Column(
            modifier = Modifier
                .constrainAs(column) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    AnimatedVisibility(visible = isUserSignedIn && canSyncSessions) {
                        TextButton(
                            onClick = { viewModel.syncSessions() },
                            enabled = isUserSignedIn && canSyncSessions,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudUpload,
                                    contentDescription = ""
                                )
                                Text(text = stringResource(R.string.sync))
                            }
                        }
                    }
                    AnimatedVisibility(visible = isUserSignedIn && syncedSessions.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.deleteAllSyncedData() },
                            enabled = isUserSignedIn && syncedSessions.isNotEmpty(),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Rounded.CloudOff, contentDescription = "")
                                Text(text = stringResource(R.string.delete_from_cloud))
                            }
                        }
                    }
                    AnimatedVisibility(visible = canDeleteSessions) {
                        TextButton(
                            onClick = { viewModel.deleteSessionsLocally() },
                            enabled = canDeleteSessions
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Rounded.Delete, contentDescription = "")
                                Text(text = stringResource(R.string.delete_locally))
                            }
                        }
                    }
                    AnimatedVisibility(visible = isUserSignedIn && areThereSessionsNotOwned) {
                        TextButton(
                            onClick = { viewModel.ownAllSessions() },
                            enabled = isUserSignedIn && areThereSessionsNotOwned,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Rounded.AddChart, contentDescription = "")
                                Text(text = stringResource(R.string.own_all_sessions))
                            }
                        }
                    }
                }
            }
            SessionsList(
                modifier = Modifier
                    .padding(horizontal = MenuItemPadding * 2)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp
                        )
                    ),
                viewModel = viewModel,
                destinationsNavigator = destinationsNavigator
            )
        }
    }
}

@Composable
fun SessionsList(
    modifier: Modifier = Modifier,
    viewModel: SessionsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator,
) {
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    val noSessionsToShow by viewModel.noSessionsToShow.collectAsState()
    val localSessionsLoaded by viewModel.localSessionsLoaded.collectAsState()
    val syncedSessionsLoaded by viewModel.syncedSessionsLoaded.collectAsState()
    val allSessionUUIDs by viewModel.allSessionUUIDs.collectAsState()
    LazyColumn(
        modifier = modifier,
        contentPadding = DefaultContentPadding,
        verticalArrangement = Arrangement.spacedBy(MenuItemPadding),
        reverseLayout = true
    ) {
        if (noSessionsToShow) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = "")
                    Text(text = stringResource(R.string.no_sessions_to_show))
                }
            }
        }
        if (!syncedSessionsLoaded && isUserSignedIn) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallCircularProgressIndicator()
                        Text(text = stringResource(R.string.loading_sessions_from_cloud))
                    }
                }
            }
        }
        if (!localSessionsLoaded) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallCircularProgressIndicator()
                        Text(text = stringResource(R.string.loading_sessions))
                    }
                }
            }
        }
        items(allSessionUUIDs) {
            val session by viewModel.getSessionStateFlow(it).collectAsState()
            DisposableEffect(true) {
                onDispose {
                    viewModel.disposeSessionStateFlow(it)
                }
            }
            val isPlaceholderVisible = session == null
            SessionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardPlaceholder(isPlaceholderVisible),
                session = session,
                onClick = { sessionUUID ->
                    destinationsNavigator.navigate(
                        SessionScreenDestination(
                            sessionUUID = sessionUUID
                        )
                    )
                }
            ) {
                AnimatedVisibility(
                    visible = session != null && isUserSignedIn && session!!.isNotOwned
                ) {
                    Button(
                        onClick = { viewModel.ownSession(session!!.uuid) },
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
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCard(
    modifier: Modifier = Modifier,
    session: UiSession? = null,
    onClick: (String) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = Neutral95
    )
    Card(
        modifier = modifier,
        onClick = { session?.let { onClick(it.uuid) } },
        colors = cardColors,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MenuItemPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session?.startLocationName ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Icon(imageVector = Icons.Rounded.ArrowRightAlt, contentDescription = "")
                    Crossfade(targetState = session?.endDateTime == null) {
                        if (it) {
                            Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = "")
                        } else {
                            Text(
                                text = session?.endLocationName ?: stringResource(R.string.unknown),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${stringResource(R.string.distance)}: " +
                                if (session == null) {
                                    stringResource(R.string.unknown)
                                } else {
                                    "${
                                        session.totalDistance
                                            .div(1000)
                                            .toBigDecimal()
                                            .setScale(2, RoundingMode.FLOOR)
                                    } " +
                                            stringResource(R.string.kilometers)
                                }
                    )
                    Text(
                        text = "${stringResource(R.string.duration)}: " +
                                (session?.duration?.format(
                                    separator = " ",
                                    second = stringResource(R.string.second_short),
                                    minute = stringResource(R.string.minute_short),
                                    hour = stringResource(R.string.hour_short),
                                    day = stringResource(R.string.day_short)
                                )
                                    ?: stringResource(R.string.unknown))
                    )
                }
                content()
            }
        }
    }
}
