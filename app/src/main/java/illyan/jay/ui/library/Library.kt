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

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.libraries.model.Library
import illyan.jay.ui.libraries.model.License
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun LibraryDialogScreen(
    library: Library
) {
    LibraryDialogContent(library = library)
}

@Composable
fun LibraryDialogContent(
    modifier: Modifier = Modifier,
    library: Library,
) {
    JayDialogContent(
        modifier = modifier,
        title = {
            LibraryTitle(
                name = library.name,
                licenseType = library.license?.type
            )
        },
        text = {
            LibraryScreen(library = library)
        },
        buttons = {
            LibraryButtons(
                modifier = Modifier.fillMaxWidth(),
                moreInfoUri = if (library.moreInfoUrl != null) Uri.parse(library.moreInfoUrl) else null,
                repositoryUri = if (library.repositoryUrl != null) Uri.parse(library.repositoryUrl) else null,
            )
        }
    )
}

@Composable
fun LibraryTitle(
    name: String,
    licenseType: String? = null
) {
    Column {
        Text(text = name)
        AnimatedVisibility(visible = licenseType != null) {
            licenseType?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.license),
                        style = MaterialTheme.typography.titleSmall,
                        color = AlertDialogDefaults.titleContentColor,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = ""
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = AlertDialogDefaults.titleContentColor,
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    library: Library
) {
    Column(
        modifier = modifier,
    ) {
        
        AnimatedVisibility(visible = library.license?.description != null) {
            library.license?.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlertDialogDefaults.textContentColor
                )
            }
        }
    }
}

@Composable
fun LibraryButtons(
    modifier: Modifier = Modifier,
    moreInfoUri: Uri? = null,
    repositoryUri: Uri? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = repositoryUri != null) {
            repositoryUri?.let {
                TextButton(onClick = { /* TODO: Open browser with link */ }) {
                    Text(text = stringResource(id = R.string.repository))
                }
            }
        }
        AnimatedVisibility(visible = moreInfoUri != null) {
            moreInfoUri?.let {
                Button(onClick = { /* TODO: Open browser with link */ }) {
                    Text(text = stringResource(id = R.string.more))
                }
            }
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun LibraryDialogContentPreview() {
    val library = Library(
        name = "Compose Scrollbar",
        license = License(
            type = "Apache v2",
            description = "Cool license",
        ),
        moreInfoUrl = "https://github.com/HLCaptain/compose-scrollbar",
        repositoryUrl = "https://github.com/HLCaptain/compose-scrollbar"
    )
    JayTheme {
        JayDialogContent {
            LibraryDialogContent(library = library)
        }
    }
}