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

package illyan.jay.ui.sessions

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
import androidx.compose.material.icons.rounded.ArrowRightAlt
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.destinations.SessionScreenDestination
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.menu.MenuItemPadding
import illyan.jay.ui.menu.MenuNavGraph
import illyan.jay.ui.menu.SheetScreenBackPressHandler
import illyan.jay.ui.sessions.model.UiSession
import illyan.jay.ui.theme.Neutral95
import illyan.jay.util.format
import java.math.RoundingMode

val DefaultContentPadding = PaddingValues(
    bottom = MenuItemPadding * 2
)

val DefaultScreenOnSheetPadding = PaddingValues(
    top = MenuItemPadding * 2,
    start = MenuItemPadding * 2,
    end = MenuItemPadding * 2,
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
    LaunchedEffect(Unit) {
        viewModel.load(context)
    }
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    Column(
        modifier = Modifier.padding(DefaultScreenOnSheetPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.syncSessions() },
                enabled = isUserSignedIn
            ) {
                Text(text = "Sync sessions to the cloud!")
            }
            Button(
                onClick = { viewModel.deleteAllSyncedData() },
                enabled = isUserSignedIn
            ) {
                Text(text = "PURGE IT ALL!")
            }
        }
        SessionsList(
            modifier = Modifier
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

@Composable
fun SessionsList(
    modifier: Modifier = Modifier,
    viewModel: SessionsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator,
) {
    val sessionIds by viewModel.sessionIds.collectAsState()
    val syncedSessions by viewModel.syncedSessions.collectAsState()
    LazyColumn(
        modifier = modifier,
        contentPadding = DefaultContentPadding,
        verticalArrangement = Arrangement.spacedBy(MenuItemPadding)
    ) {
        item {
            Text(text = "Synced sessions")
        }
        items(syncedSessions) {
            SessionCard(
                modifier = Modifier.fillMaxWidth(),
                session = it
            )
        }
        item {
            Text(text = "Local sessions")
        }
        items(sessionIds) {
            val session by viewModel.getSessionStateFlow(it).collectAsState()
            val isPlaceholderVisible = session == null
            val placeholderHighlight = PlaceholderHighlight.shimmer()
            SessionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = isPlaceholderVisible,
                        highlight = placeholderHighlight,
                        shape = RoundedCornerShape(12.dp)
                    ),
                session = session,
                onClick = { id ->
                    destinationsNavigator.navigate(SessionScreenDestination(id))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCard(
    modifier: Modifier = Modifier,
    session: UiSession?,
    onClick: (Long) -> Unit = {},
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = Neutral95
    )
    Card(
        modifier = modifier,
        onClick = { session?.let { onClick(it.id) } },
        colors = cardColors,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MenuItemPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            Column {
                Text(
                    text = "${stringResource(R.string.distance)}: " +
                            "${session?.totalDistance?.div(1000)?.toBigDecimal()?.setScale(2, RoundingMode.FLOOR) ?:
                            stringResource(R.string.unknown)} " +
                            stringResource(R.string.kilometers)
                )
                Text(
                    text = "${stringResource(R.string.duration)}: " +
                            session?.duration?.format(
                                separator = " ",
                                second = stringResource(R.string.second_short),
                                minute = stringResource(R.string.minute_short),
                                hour = stringResource(R.string.hour_short),
                                day = stringResource(R.string.day_short)
                            )
                )
            }
        }
    }
}
