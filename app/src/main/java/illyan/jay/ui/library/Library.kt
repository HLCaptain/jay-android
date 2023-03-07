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

package illyan.jay.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import illyan.jay.R
import illyan.jay.domain.model.libraries.Library
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayTextCard
import illyan.jay.ui.components.LicenseOfType
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.libraries.model.UiLibrary
import illyan.jay.ui.libraries.model.toUiModel
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun LibraryDialogScreen(
    library: UiLibrary
) {
    LibraryDialogContent(library = library)
}

@Composable
fun LibraryDialogContent(
    modifier: Modifier = Modifier,
    library: UiLibrary,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    JayDialogContent(
        modifier = modifier,
        textModifier = Modifier.heightIn(max = (screenHeightDp * 0.5f).dp),
        title = {
            LibraryTitle(
                name = library.name,
                licenseType = library.license?.name,
                licenseUrl = library.license?.url
            )
        },
        text = {
            LibraryScreen(library = library)
        },
        buttons = {
            LibraryButtons(
                modifier = Modifier.fillMaxWidth(),
                moreInfoUri = library.moreInfoUrl,
                repositoryUri = library.repositoryUrl,
            )
        },
        titlePaddingValues = PaddingValues(bottom = 4.dp),
        textPaddingValues = PaddingValues(bottom = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTitle(
    name: String,
    licenseType: String? = null,
    licenseUrl: String? = null
) {
    Column {
        Text(text = name)
        AnimatedVisibility(visible = licenseType != null) {
            licenseType?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.license),
                        style = MaterialTheme.typography.titleSmall,
                        color = AlertDialogDefaults.titleContentColor,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = ""
                    )
                    val uriHandler = LocalUriHandler.current
                    OutlinedCard(
                        onClick = { licenseUrl?.let { uriHandler.openUri(it) }}
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            color = AlertDialogDefaults.titleContentColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    library: UiLibrary
) {
    JayTextCard(
        modifier = modifier
    ) {
        // TODO: change license text to button pointing to the exact license of the used library
        LicenseOfType(
            type = library.license?.type,
            authors = library.license?.authors ?: emptyList(),
            year = library.license?.year,
            yearInterval = library.license?.yearInterval
        )
    }
}

@Composable
fun LibraryButtons(
    modifier: Modifier = Modifier,
    moreInfoUri: String? = null,
    repositoryUri: String? = null,
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = repositoryUri != null) {
            TextButton(
                onClick = { uriHandler.openUri(repositoryUri.toString()) }
            ) {
                Text(text = stringResource(R.string.repository))
            }
        }
        Button(
            enabled = moreInfoUri != null,
            onClick = { uriHandler.openUri(moreInfoUri.toString()) }
        ) {
            Text(text = stringResource(R.string.more))
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun LibraryDialogContentPreview() {
    val library = Library.Jay.toUiModel()
    JayTheme {
        JayDialogContent(
            modifier = Modifier.width(300.dp)
        ) {
            LibraryDialogContent(library = library)
        }
    }
}