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

package illyan.jay.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayDialogSurface
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.settings.model.UiPreferences
import illyan.jay.ui.theme.JayTheme


@ProfileNavGraph
@Destination
@Composable
fun SettingsDialogScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    SettingsDialogContent(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = max(200.dp, screenHeightDp - 256.dp)),
        preferences = preferences
    )
}

@Composable
fun SettingsDialogContent(
    modifier: Modifier = Modifier,
    preferences: UiPreferences? = null,
) {
    JayDialogContent(
        modifier = modifier,
        title = {
            SettingsTitle()
        },
        text = {
            SettingsScreen(
                preferences = preferences
            )
        },
        buttons = {
            // TODO: Toggle Settings Sync
            // TODO: Dismiss dialog (Cancel)
        },
        containerColor = Color.Transparent,
    )
}

@Composable
fun SettingsTitle() {
    Text(text = stringResource(id = R.string.settings))
}

@Composable
fun SettingsScreen(
    preferences: UiPreferences? = null
) {
    LazyColumn {
        items(
            listOf(
                "Analytics enabled" to preferences?.analyticsEnabled,
                "Free drive auto" to preferences?.freeDriveAutoStart,
                "User UUID" to preferences?.userUUID,
                "Last update" to preferences?.lastUpdate
            )
        ) {
            Text(text = it.first)
            Spacer(modifier = Modifier.width(40.dp))
            Text(text = it.second.toString())
        }
    }
    // TODO: enable ad button on this screen (only showing one ad on this screen)
    Spacer(modifier = Modifier.height(400.dp)) // Fake height
}

@PreviewLightDarkTheme
@Composable
fun SettingsDialogScreenPreview() {
    JayTheme {
        JayDialogSurface {
            SettingsDialogContent()
        }
    }
}
