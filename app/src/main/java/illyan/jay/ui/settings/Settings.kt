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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import illyan.jay.data.disk.model.AppSettings
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayDialogSurface
import illyan.jay.ui.components.MediumCircularProgressIndicator
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.components.SmallCircularProgressIndicator
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.settings.model.UiPreferences
import illyan.jay.ui.settings.model.toUiModel
import illyan.jay.ui.theme.JayTheme
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds


@ProfileNavGraph
@Destination
@Composable
fun SettingsDialogScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val arePreferencesSynced by viewModel.arePreferencesSynced.collectAsStateWithLifecycle()
    SettingsDialogContent(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = max(200.dp, screenHeightDp - 256.dp)),
        preferences = preferences,
        arePreferencesSynced = arePreferencesSynced,
        setAnalytics = viewModel::setAnalytics,
        setFreeDriveAutoStart = viewModel::setFreeDriveAutoStart
    )
}

@Composable
fun SettingsDialogContent(
    modifier: Modifier = Modifier,
    preferences: UiPreferences? = null,
    arePreferencesSynced: Boolean = false,
    setAnalytics: (Boolean) -> Unit = {},
    setFreeDriveAutoStart: (Boolean) -> Unit = {},
) {
    JayDialogContent(
        modifier = modifier,
        title = {
            SettingsTitle(
                arePreferencesSynced = arePreferencesSynced,
                lastUpdated = preferences?.lastUpdate
            )
        },
        text = {
            SettingsScreen(
                preferences = preferences,
                setAnalytics = setAnalytics,
                setFreeDriveAutoStart = setFreeDriveAutoStart
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
fun SettingsTitle(
    arePreferencesSynced: Boolean = false,
    lastUpdated: ZonedDateTime? = ZonedDateTime.now()
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = stringResource(id = R.string.settings))
            SyncPreferencesLabel(arePreferencesSynced = arePreferencesSynced)
        }
        LastUpdatedLabel(lastUpdated = lastUpdated)
    }
}

@Composable
private fun SyncPreferencesLabel(
    arePreferencesSynced: Boolean
) {
    Crossfade(targetState = arePreferencesSynced) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (it) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.synced),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(id = R.string.not_synced),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LastUpdatedLabel(
    lastUpdated: ZonedDateTime? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.last_update),
            style = MaterialTheme.typography.labelMedium
        )
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = lastUpdated
        ) {
            if (it != null) {
                Text(
                    text = it
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .minusNanos(it.nano.toLong()) // No millis in formatted time
                        .format(
                            if (it.second.seconds.inWholeDays == ZonedDateTime.now().second.seconds.inWholeDays) {
                                DateTimeFormatter.ISO_LOCAL_TIME
                            } else {
                                DateTimeFormatter.ISO_LOCAL_DATE
                            }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                SmallCircularProgressIndicator()
            }
        }
    }
}

@Composable
fun SettingsScreen(
    preferences: UiPreferences? = null,
    setAnalytics: (Boolean) -> Unit = {},
    setFreeDriveAutoStart: (Boolean) -> Unit = {},
) {
    Crossfade(targetState = preferences != null) {
        if (it && preferences != null) {
            LazyColumn {
                item {
                    AnalyticsSetting(
                        analyticsEnabled = preferences.analyticsEnabled,
                        setAnalytics = setAnalytics
                    )
                    FreeDriveAutoStartSetting(
                        freeDriveAutoStart = preferences.freeDriveAutoStart,
                        setFreeDriveAutoStart = setFreeDriveAutoStart
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(R.string.loading))
                MediumCircularProgressIndicator()
            }
        }
    }
    // TODO: enable ad button on this screen (only showing one ad on this screen)
    Spacer(modifier = Modifier.height(400.dp)) // Fake height
}


@Composable
private fun BooleanSetting(
    value: Boolean,
    setValue: (Boolean) -> Unit,
    settingName: String,
    enabledText: String = stringResource(R.string.enabled),
    disabledText: String = stringResource(R.string.disabled)
) {
    SettingItem(
        modifier = Modifier.fillMaxWidth(),
        name = settingName
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = value
            ) { enabled ->
                Text(text = if (enabled) enabledText else disabledText)
            }
            Switch(
                checked = value,
                onCheckedChange = setValue,
            )
        }
    }
}

@Composable
private fun AnalyticsSetting(
    analyticsEnabled: Boolean = AppSettings.default.preferences.analyticsEnabled,
    setAnalytics: (Boolean) -> Unit = {}
) {
    BooleanSetting(
        settingName = stringResource(R.string.analytics),
        setValue = setAnalytics,
        value = analyticsEnabled
    )
}

@Composable
private fun FreeDriveAutoStartSetting(
    freeDriveAutoStart: Boolean = AppSettings.default.preferences.freeDriveAutoStart,
    setFreeDriveAutoStart: (Boolean) -> Unit = {}
) {
    BooleanSetting(
        settingName = stringResource(R.string.free_drive_auto_start),
        setValue = setFreeDriveAutoStart,
        value = freeDriveAutoStart
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    name: String,
    content: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name)
        content()
    }
}

@PreviewLightDarkTheme
@Composable
fun SettingsDialogScreenPreview() {
    JayTheme {
        JayDialogSurface {
            SettingsDialogContent(
                preferences = AppSettings.default.preferences.toUiModel()
            )
        }
    }
}
