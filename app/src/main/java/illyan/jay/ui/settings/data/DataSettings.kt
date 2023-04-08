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

package illyan.jay.ui.settings.data

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.AvatarAsyncImage
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewThemesScreensFonts
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.profile.MenuButton
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun DataSettingsDialogScreen(
    viewModel: DataSettingsViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsStateWithLifecycle()
    val cachedDataSizeInBytes by viewModel.cachedDataSizeInBytes.collectAsStateWithLifecycle()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
    DataSettingsDialogContent(
        userPhotoUrl = userPhotoUrl,
        isUserSignedIn = isUserSignedIn,
        cachedDataSizeInBytes = cachedDataSizeInBytes,
        onDeleteCached = viewModel::deleteCachedUserData,
        onDeletePublic = viewModel::deletePublicData,
        onDeleteSynced = viewModel::deleteSyncedUserData,
        onDeleteAll = viewModel::deleteAllUserData,
        onNavigateUp = destinationsNavigator::navigateUp
    )
}

@Composable
fun DataSettingsDialogContent(
    modifier: Modifier = Modifier,
    userPhotoUrl: Uri? = null,
    isUserSignedIn: Boolean = true,
    cachedDataSizeInBytes: Long? = null,
    onDeleteCached: () -> Unit = {},
    onDeletePublic: () -> Unit = {},
    onDeleteSynced: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
    onNavigateUp: () -> Unit = {},
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    JayDialogContent(
        modifier = modifier,
        icon = {
            DataSettingsIconScreen(
                userPhotoUrl = userPhotoUrl,
                isUserSignedIn = isUserSignedIn,
            )
        },
        title = {
            DataSettingsTitleScreen()
        },
        textPaddingValues = PaddingValues(),
        text = {
            DataSettingsScreen(
                modifier = Modifier.heightIn(max = (screenHeightDp * 0.4f).dp),
                onDeleteCached = onDeleteCached,
                onDeletePublic = onDeletePublic,
                onDeleteSynced = onDeleteSynced,
                onDeleteAll = onDeleteAll,
            )
        },
        buttons = {
            DataSettingsButtons(
                cachedDataSizeInBytes = cachedDataSizeInBytes,
                onNavigateUp = onNavigateUp,
            )
        },
        containerColor = Color.Transparent,
    )
}

@Composable
fun DataSettingsIconScreen(
    modifier: Modifier = Modifier,
    isUserSignedIn: Boolean = true,
    userPhotoUrl: Uri? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        AvatarAsyncImage(
            modifier = Modifier
                .size(RoundedCornerRadius * 4)
                .clip(CircleShape),
            placeholderEnabled = !isUserSignedIn || userPhotoUrl == null,
            userPhotoUrl = userPhotoUrl
        )
    }

}

@Composable
fun DataSettingsTitleScreen(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.my_data))
    }
}

@Composable
fun DataSettingsButtons(
    modifier: Modifier = Modifier,
    cachedDataSizeInBytes: Long? = null,
    onNavigateUp: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val byteString = stringResource(R.string.bytes)
        Crossfade(targetState = cachedDataSizeInBytes) {
            when (it != null) {
                true -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = byteString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                else -> {}
            }
        }
        TextButton(onClick = onNavigateUp) {
            Text(text = stringResource(R.string.back))
        }
    }
}

@Composable
fun DataSettingsScreen(
    modifier: Modifier = Modifier,
    onDeleteCached: () -> Unit = {},
    onDeletePublic: () -> Unit = {},
    onDeleteSynced: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.clip(CardDefaults.shape),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            MenuButtonWithDescription(
                onClick = onDeletePublic,
                text = stringResource(R.string.delete_public_data),
                description = stringResource(R.string.delete_public_data_description)
            )
        }
        item {
            MenuButtonWithDescription(
                onClick = onDeleteCached,
                text = stringResource(R.string.delete_from_device),
                description = stringResource(R.string.delete_from_device_description)
            )
        }
        item {
            MenuButtonWithDescription(
                onClick = onDeleteSynced,
                text = stringResource(R.string.delete_from_cloud),
                description = stringResource(R.string.delete_from_cloud_description)
            )
        }
        item {
            MenuButtonWithDescription(
                onClick = onDeleteAll,
                text = stringResource(R.string.delete_all_user_data),
                description = stringResource(R.string.delete_all_user_data_description)
            )
        }
    }
}

@Composable
fun MenuButtonWithDescription(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    description: String,
    showDescriptionInitially: Boolean = false,
) {
    var showDescription by rememberSaveable { mutableStateOf(showDescriptionInitially) }
    Column(
        modifier = modifier,
    ) {
        DescriptionCard(
            onClick = { showDescription = !showDescription },
            text = description,
            showDescription = showDescription,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MenuButton(
                    onClick = onClick,
                    text = text
                )
                IconToggleButton(
                    checked = showDescription,
                    onCheckedChange = { showDescription = it }
                ) {
                    Icon(
                        imageVector = if (showDescription) {
                            Icons.Rounded.ExpandLess
                        } else {
                            Icons.Rounded.ExpandMore
                        },
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    showDescription: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    content: @Composable () -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = modifier.padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
            AnimatedVisibility(visible = showDescription) {
                Text(
                    modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 8.dp),
                    text = text,
                    color = textColor,
                    style = style,
                    fontWeight = fontWeight
                )
            }
        }
    }
}

@PreviewThemesScreensFonts
@Composable
fun PreviewDataSettingsDialogContent() {
    JayTheme {
        DataSettingsDialogContent()
    }
}