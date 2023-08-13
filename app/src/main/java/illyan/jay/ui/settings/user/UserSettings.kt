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

package illyan.jay.ui.settings.user

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.domain.model.Theme
import illyan.jay.ui.components.CopiedToKeyboardTooltip
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayDialogSurface
import illyan.jay.ui.components.LoadingIndicator
import illyan.jay.ui.components.MediumCircularProgressIndicator
import illyan.jay.ui.components.MenuButton
import illyan.jay.ui.components.PreviewAccessibility
import illyan.jay.ui.components.SmallCircularProgressIndicator
import illyan.jay.ui.components.TooltipElevatedCard
import illyan.jay.ui.destinations.DataSettingsDialogScreenDestination
import illyan.jay.ui.destinations.MLSettingsDialogScreenDestination
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.settings.user.model.UiPreferences
import illyan.jay.ui.theme.JayTheme
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@ProfileNavGraph
@Destination
@Composable
fun UserSettingsDialogScreen(
    viewModel: UserSettingsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val arePreferencesSynced by viewModel.arePreferencesSynced.collectAsStateWithLifecycle()
    val canSyncPreferences by viewModel.canSyncPreferences.collectAsStateWithLifecycle()
    val shouldSyncPreferences by viewModel.shouldSyncPreferences.collectAsStateWithLifecycle()
    val showAnalyticsRequestDialog by viewModel.showAnalyticsRequestDialog.collectAsStateWithLifecycle()

    UserSettingsDialogContent(
        modifier = Modifier.fillMaxWidth(),
        preferences = preferences,
        arePreferencesSynced = arePreferencesSynced,
        canSyncPreferences = canSyncPreferences,
        shouldSyncPreferences = shouldSyncPreferences,
        showAnalyticsRequestDialog = showAnalyticsRequestDialog,
        onShouldSyncChanged = viewModel::setPreferencesSync,
        onThemeChange = viewModel::setTheme,
        setAnalytics = viewModel::setAnalytics,
        setFreeDriveAutoStart = viewModel::setFreeDriveAutoStart,
        setAdVisibility = viewModel::setAdVisibility,
        setDynamicColorEnabled = viewModel::setDynamicColorEnabled,
        navigateToDataSettings = { destinationsNavigator.navigate(DataSettingsDialogScreenDestination) },
        navigateToMLSettings = { destinationsNavigator.navigate(MLSettingsDialogScreenDestination) },
    )
}

@Composable
fun UserSettingsDialogContent(
    modifier: Modifier = Modifier,
    preferences: UiPreferences?,
    arePreferencesSynced: Boolean = false,
    canSyncPreferences: Boolean = false,
    shouldSyncPreferences: Boolean = false,
    showAnalyticsRequestDialog: Boolean = false,
    onShouldSyncChanged: (Boolean) -> Unit = {},
    setAnalytics: (Boolean) -> Unit = {},
    setFreeDriveAutoStart: (Boolean) -> Unit = {},
    setAdVisibility: (Boolean) -> Unit = {},
    setDynamicColorEnabled: (Boolean) -> Unit = {},
    navigateToDataSettings: () -> Unit = {},
    onThemeChange: (Theme) -> Unit = {},
    navigateToMLSettings: () -> Unit = {},
) {
    Crossfade(
        modifier = modifier.animateContentSize(),
        targetState = showAnalyticsRequestDialog,
        label = "User Settings Dialog Content",
    ) {
        if (it) {
            AnalyticsRequestDialogContent(
                analyticsEnabled = preferences?.analyticsEnabled,
                setAnalytics = setAnalytics
            )
        } else {
            JayDialogContent(
                title = {
                    UserSettingsTitle(
                        arePreferencesSynced = arePreferencesSynced,
                        preferences = preferences
                    )
                },
                text = {
                    UserSettingsScreen(
                        preferences = preferences,
                        setAnalytics = setAnalytics,
                        setFreeDriveAutoStart = setFreeDriveAutoStart,
                        setAdVisibility = setAdVisibility,
                        setDynamicColorEnabled = setDynamicColorEnabled,
                        onThemeChange = onThemeChange,
                        navigateToMLSettings = navigateToMLSettings
                    )
                },
                buttons = {
                    UserSettingsButtons(
                        canSyncPreferences = canSyncPreferences,
                        shouldSyncPreferences = shouldSyncPreferences,
                        onShouldSyncChanged = onShouldSyncChanged,
                        navigateToDataSettings = navigateToDataSettings,
                    )
                },
                containerColor = Color.Transparent,
            )
        }
    }
}

@Composable
fun AnalyticsRequestDialogContent(
    modifier: Modifier = Modifier,
    analyticsEnabled: Boolean? = null,
    setAnalytics: (Boolean) -> Unit = {},
) {
    var analyticsSet by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            if (!analyticsSet) setAnalytics(analyticsEnabled ?: false)
        }
    }
    JayDialogContent(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopCenter),
                imageVector = Icons.Rounded.Insights,
                contentDescription = ""
            )
        },
        title = {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = stringResource(R.string.turn_on_analytics),
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn {
                item {
                    Text(text = stringResource(R.string.turn_on_analytics_description))
                }
            }
        },
        buttons = {
            AnalyticsRequestButtons(
                modifier = Modifier.align(Alignment.BottomEnd),
                onDismiss = {
                    analyticsSet = true
                    setAnalytics(false)
                },
                onTurnOnAnalytics = {
                    analyticsSet = true
                    setAnalytics(true)
                }
            )
        }
    )
}

@Composable
fun AnalyticsRequestButtons(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onTurnOnAnalytics: () -> Unit = {}
) {
    Row(
        modifier = modifier
    ) {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(R.string.dismiss))
        }
        Button(onClick = onTurnOnAnalytics) {
            Text(text = stringResource(R.string.turn_on))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSettingsTitle(
    arePreferencesSynced: Boolean = false,
    preferences: UiPreferences? = null,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = stringResource(R.string.settings))
            SyncPreferencesLabel(arePreferencesSynced = arePreferencesSynced)
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Crossfade(
                targetState = preferences != null,
                label = "User Settings Title",
            ) {
                if (it && preferences != null) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        ClientLabel(clientUUID = preferences.clientUUID)
                        LastUpdateLabel(lastUpdate = preferences.lastUpdate)
                    }
                } else {
                    MediumCircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun SyncPreferencesLabel(
    arePreferencesSynced: Boolean,
) {
    Crossfade(
        targetState = arePreferencesSynced,
        label = "Sync Preferences Label",
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (it) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.synced),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.not_synced),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun UserSettingsButtons(
    modifier: Modifier = Modifier,
    canSyncPreferences: Boolean = false,
    shouldSyncPreferences: Boolean = false,
    onShouldSyncChanged: (Boolean) -> Unit = {},
    navigateToDataSettings: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuButton(
            text = stringResource(R.string.data_settings),
            onClick = navigateToDataSettings
        )
        SyncPreferencesButton(
            canSyncPreferences = canSyncPreferences,
            onShouldSyncChanged = onShouldSyncChanged,
            shouldSyncPreferences = shouldSyncPreferences,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SyncPreferencesButton(
    canSyncPreferences: Boolean,
    onShouldSyncChanged: (Boolean) -> Unit,
    shouldSyncPreferences: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = canSyncPreferences,
        onClick = { onShouldSyncChanged(!shouldSyncPreferences) }
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.animateContentSize(),
                text = stringResource(
                    if (shouldSyncPreferences) {
                        R.string.syncing
                    } else {
                        R.string.not_syncing
                    }
                )
            )
            FilledIconToggleButton(
                checked = shouldSyncPreferences,
                onCheckedChange = onShouldSyncChanged,
                enabled = canSyncPreferences
            ) {
                Icon(
                    imageVector = if (shouldSyncPreferences) {
                        Icons.Rounded.Cloud
                    } else {
                        Icons.Rounded.CloudOff
                    },
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun LastUpdateLabel(
    lastUpdate: ZonedDateTime,
) {
    val time = lastUpdate
        .withZoneSameInstant(ZoneId.systemDefault())
        .minusNanos(lastUpdate.nano.toLong()) // No millis in formatted time
        .format(DateTimeFormatter.ISO_LOCAL_TIME)
    val date = lastUpdate
        .withZoneSameInstant(ZoneId.systemDefault())
        .minusNanos(lastUpdate.nano.toLong()) // No millis in formatted time
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
    val isDateVisible by remember {
        derivedStateOf {
            lastUpdate.toEpochSecond().seconds.inWholeDays !=
                    ZonedDateTime.now().toEpochSecond().seconds.inWholeDays
        }
    }
    val textStyle = MaterialTheme.typography.bodyMedium
    SettingLabel(
        settingName = stringResource(R.string.last_update),
        settingNameStyle = textStyle.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
        settingIndicator = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(visible = isDateVisible) {
                    Text(
                        text = date,
                        style = textStyle,
                    )
                }
                Text(
                    text = time,
                    style = textStyle
                )
            }
        },
    )
}

@Composable
fun ClientLabel(
    clientUUID: String?,
) {
    val clipboard = LocalClipboardManager.current
    AnimatedVisibility(visible = clientUUID != null) {
        TooltipElevatedCard(
            tooltip = { CopiedToKeyboardTooltip() },
            showTooltipOnClick = true,
            onShowTooltip = { clientUUID?.let { clipboard.setText(AnnotatedString(text = it)) } }
        ) {
            SettingLabel(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                settingName = stringResource(R.string.client_id),
                settingText = clientUUID?.take(8),
                settingTextStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SettingLabel(
    modifier: Modifier = Modifier,
    settingText: String? = null,
    settingName: String,
    settingTextStyle: TextStyle = LocalTextStyle.current,
    settingNameStyle: TextStyle = settingTextStyle.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
    settingValueTextAlign: TextAlign? = null,
) {
    SettingLabel(
        modifier = modifier,
        settingIndicator = {
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = settingText,
                label = "Setting label text loading indicator",
            ) {
                if (it != null) {
                    Text(
                        text = it,
                        style = settingTextStyle,
                        textAlign = settingValueTextAlign,
                    )
                } else {
                    SmallCircularProgressIndicator()
                }
            }
        },
        settingName = settingName,
        settingNameStyle = settingNameStyle
    )
}

@Composable
fun SettingLabel(
    modifier: Modifier = Modifier,
    settingName: String,
    settingNameStyle: TextStyle = LocalTextStyle.current.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
    settingIndicator: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = settingName,
            style = settingNameStyle,
        )
        settingIndicator()
    }
}

@Composable
fun UserSettingsScreen(
    modifier: Modifier = Modifier,
    preferences: UiPreferences? = null,
    setAnalytics: (Boolean) -> Unit = {},
    setFreeDriveAutoStart: (Boolean) -> Unit = {},
    setAdVisibility: (Boolean) -> Unit = {},
    setDynamicColorEnabled: (Boolean) -> Unit = {},
    onThemeChange: (Theme) -> Unit = {},
    navigateToMLSettings: () -> Unit = {},
) {
    Crossfade(
        modifier = modifier,
        targetState = preferences != null,
        label = "User Settings Screen"
    ) {
        if (it && preferences != null) {
            val configuration = LocalConfiguration.current
            val maxHeight = when (configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> configuration.screenHeightDp
                Configuration.ORIENTATION_LANDSCAPE -> configuration.screenWidthDp
                else -> configuration.screenHeightDp
            }
            LazyColumn(
                modifier = Modifier.heightIn(max = (maxHeight * 0.55f).dp)
            ) {
                item {
                    BooleanSetting(
                        settingName = stringResource(R.string.analytics),
                        setValue = setAnalytics,
                        value = preferences.analyticsEnabled
                    )
                }
                item {
                    BooleanSetting(
                        settingName = stringResource(R.string.free_drive_auto_start),
                        setValue = setFreeDriveAutoStart,
                        value = preferences.freeDriveAutoStart
                    )
                }
                item {
                    BooleanSetting(
                        settingName = stringResource(R.string.show_ads),
                        setValue = setAdVisibility,
                        value = preferences.showAds
                    )
                }
                item {
                    DropdownSetting(
                        settingName = stringResource(R.string.theme),
                        selectValue = onThemeChange,
                        selectedValue = preferences.theme,
                        values = Theme.values().toList(),
                        getValueName = { theme ->
                            when (theme) {
                                Theme.System -> stringResource(R.string.system)
                                Theme.Light -> stringResource(R.string.light)
                                Theme.Dark -> stringResource(R.string.dark)
                                Theme.DayNightCycle -> stringResource(R.string.day_night_cycle)
                            }
                        },
                        getValueLeadingIcon = { theme ->
                            when (theme) {
                                Theme.System -> Icons.Rounded.Settings
                                Theme.Light -> Icons.Rounded.LightMode
                                Theme.Dark -> Icons.Rounded.DarkMode
                                Theme.DayNightCycle -> Icons.Rounded.Schedule
                            }
                        }
                    )
                }
                item {
                    BooleanSetting(
                        value = preferences.dynamicColorEnabled,
                        setValue = setDynamicColorEnabled,
                        settingName = stringResource(R.string.dynamic_color),
                        enabledText = stringResource(R.string.enabled),
                        disabledText = stringResource(R.string.disabled),
                        enabled = preferences.canUseDynamicColor,
                    )
                }
                item {
                    BasicSetting(
                        screenName = stringResource(R.string.ml_models),
                        label = "${preferences.downloadedModels} ${stringResource(R.string.downloaded)}",
                        onClick = navigateToMLSettings
                    )
                }
            }
        } else {
            LoadingIndicator()
        }
    }
}

@Composable
fun BooleanSetting(
    value: Boolean,
    setValue: (Boolean) -> Unit,
    settingName: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    enabledText: String = stringResource(R.string.on),
    disabledText: String = stringResource(R.string.off),
    enabled: Boolean = true,
) {
    SettingItem(
        modifier = Modifier.fillMaxWidth(),
        settingName = settingName,
        onClick = { setValue(!value) },
        titleStyle = textStyle,
        titleWeight = fontWeight,
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = value,
                label = "Boolean setting text"
            ) { enabled ->
                Text(
                    text = if (enabled) enabledText else disabledText,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = value,
                onCheckedChange = setValue,
                enabled = enabled
            )
        }
    }
}

@Composable
fun <T : Any> DropdownSetting(
    selectedValue: T? = null,
    values: Iterable<T>,
    getValueName: @Composable (T) -> String = { it.toString() },
    getValueLeadingIcon: (T) -> ImageVector? = { null },
    getValueTrailingIcon: (T) -> ImageVector? = { null },
    selectValue: (T) -> Unit,
    settingName: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    SettingItem(
        modifier = Modifier.fillMaxWidth(),
        settingName = settingName,
        onClick = { isDropdownOpen = !isDropdownOpen },
        titleStyle = textStyle,
        titleWeight = fontWeight,
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = selectedValue,
                label = "Dropdown setting text",
            ) { state ->
                state?.let {
                    Text(
                        text = getValueName(it),
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            IconToggleButton(
                checked = isDropdownOpen,
                onCheckedChange = { isDropdownOpen = it }
            ) {
                Icon(
                    imageVector = if (isDropdownOpen) {
                        Icons.Rounded.ExpandLess
                    } else {
                        Icons.Rounded.ExpandMore
                    },
                    contentDescription = ""
                )
            }
        }
        DropdownMenu(
            expanded = isDropdownOpen,
            onDismissRequest = { isDropdownOpen = false },
        ) {
            values.forEach { value ->
                val leadingIcon = remember { getValueLeadingIcon(value) }
                val leadingComposable = @Composable {
                    leadingIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = ""
                        )
                    }
                }
                val trailingIcon = remember {
                    val icon = getValueTrailingIcon(value)
                    if (value == selectedValue) Icons.Rounded.Check else icon
                }
                val trailingComposable = @Composable {
                    trailingIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = ""
                        )
                    }
                }
                DropdownMenuItem(
                    modifier = if (value == selectedValue) {
                        Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 1.dp))
                    } else {
                        Modifier
                    },
                    text = { Text(text = getValueName(value)) },
                    leadingIcon = (if (leadingIcon != null) leadingComposable else null) as? @Composable (() -> Unit),
                    trailingIcon = (if (trailingIcon != null) trailingComposable else null) as? @Composable (() -> Unit),
                    onClick = { selectValue(value); isDropdownOpen = false },
                    colors = if (value == selectedValue) {
                        MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.primary,
                            leadingIconColor = MaterialTheme.colorScheme.primary,
                            trailingIconColor = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        MenuDefaults.itemColors()
                    }
                )
            }
        }
    }
}

@Composable
fun BasicSetting(
    modifier: Modifier = Modifier,
    screenName: String,
    label: String,
    onClick: () -> Unit = {}
) = BasicSetting(
    modifier = modifier,
    title = screenName,
    label = {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = ""
            )
        }
    },
    onClick = onClick
)

@Composable
fun BasicSetting(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.labelLarge,
    titleWeight: FontWeight = FontWeight.Normal,
    label: @Composable RowScope.() -> Unit = {},
    onClick: () -> Unit = {}
) {
    SettingItem(
        modifier = modifier,
        onClick = onClick,
        title = {
            Text(
                modifier = Modifier.animateContentSize(),
                text = title,
                style = titleStyle,
                fontWeight = titleWeight,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        content = label,
    )
}

@Composable
fun BasicSetting(
    modifier: Modifier = Modifier,
    title: @Composable RowScope.() -> Unit = {},
    label: @Composable RowScope.() -> Unit = {},
    onClick: () -> Unit = {}
) {
    SettingItem(
        modifier = modifier,
        onClick = onClick,
        title = title,
        content = label,
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    settingName: String,
    titleStyle: TextStyle = MaterialTheme.typography.labelLarge,
    titleWeight: FontWeight = FontWeight.Normal,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit = {},
) = SettingItem(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
    title = {
        Text(
            modifier = Modifier.animateContentSize(),
            text = settingName,
            style = titleStyle,
            fontWeight = titleWeight,
            color = MaterialTheme.colorScheme.onSurface
        )
    },
    content = content
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    title: @Composable RowScope.() -> Unit = {},
    content: @Composable RowScope.() -> Unit = {},
) {
    Card(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            title()
            content()
        }
    }
}

private fun generateRandomUserPreferences(): UiPreferences {
    return UiPreferences(
        userUUID = UUID.randomUUID().toString(),
        clientUUID = UUID.randomUUID().toString(),
        lastUpdate = ZonedDateTime.now().minusDays(if (Random.nextBoolean()) 1 else 0),
        analyticsEnabled = Random.nextBoolean(),
        freeDriveAutoStart = Random.nextBoolean(),
        showAds = Random.nextBoolean(),
    )
}

@PreviewAccessibility
@Composable
fun UserSettingsDialogScreenPreview() {
    JayTheme {
        JayDialogSurface {
            val preferences = generateRandomUserPreferences()
            val canSyncPreferences = Random.nextBoolean()
            val arePreferencesSynced = if (canSyncPreferences) Random.nextBoolean() else false
            val shouldSyncPreferences = if (arePreferencesSynced) true else Random.nextBoolean()
            UserSettingsDialogContent(
                preferences = preferences,
                canSyncPreferences = canSyncPreferences,
                arePreferencesSynced = arePreferencesSynced,
                shouldSyncPreferences = shouldSyncPreferences
            )
        }
    }
}

@PreviewAccessibility
@Composable
fun AnalyticsRequestDialogContentPreview() {
    JayTheme {
        JayDialogSurface {
            val preferences = generateRandomUserPreferences()
            AnalyticsRequestDialogContent(
                analyticsEnabled = preferences.analyticsEnabled,
            )
        }
    }
}
