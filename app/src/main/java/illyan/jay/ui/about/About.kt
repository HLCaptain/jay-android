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

package illyan.jay.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.domain.model.libraries.Library
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.destinations.LibrariesDialogScreenDestination
import illyan.jay.ui.destinations.LibraryDialogScreenDestination
import illyan.jay.ui.libraries.model.toUiModel
import illyan.jay.ui.profile.ProfileMenuItem
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun AboutDialogScreen(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    AboutDialogContent(
        modifier = Modifier.fillMaxWidth(),
        onNavigateToLibraries = {
            destinationsNavigator.navigate(LibrariesDialogScreenDestination)
        },
        onNavigateToJayLicense = {
            destinationsNavigator.navigate(LibraryDialogScreenDestination(Library.Jay.toUiModel()))
        }
    )
}

@Composable
fun AboutDialogContent(
    modifier: Modifier = Modifier,
    onNavigateToLibraries: () -> Unit = {},
    onNavigateToJayLicense: () -> Unit = {},
) {
    JayDialogContent(
        modifier = modifier,
        title = { AboutTitle() },
        text = {
            AboutScreen(
                onNavigateToLibraries = onNavigateToLibraries,
                onNavigateToJayLicense = onNavigateToJayLicense
            )
        },
        buttons = {
            // TODO: Support the project
            // TODO: Dismiss dialog (Cancel)
        },
        containerColor = Color.Transparent,
    )
}

@Composable
fun AboutTitle() {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.about))
            Text(text = stringResource(id = R.string.app_name))
        }
        Text(
            text = stringResource(id = R.string.app_description_brief),
            style = MaterialTheme.typography.bodySmall,
            color = AlertDialogDefaults.textContentColor
        )
    }
}

@Composable
fun AboutScreen(
    onNavigateToLibraries: () -> Unit = {},
    onNavigateToJayLicense: () -> Unit = {}
) {
    // TODO: enable ad button on this screen (only showing one ad on this screen)
    Column(
        verticalArrangement = Arrangement.spacedBy((-12).dp)
    ) {
        ProfileMenuItem(
            text = stringResource(id = R.string.libraries),
            onClick = onNavigateToLibraries
        )
        ProfileMenuItem(
            text = stringResource(id = R.string.jay_license),
            onClick = onNavigateToJayLicense
        )
        Spacer(modifier = Modifier.height(240.dp)) // Fake height
    }
}

@PreviewLightDarkTheme
@Composable
private fun AboutDialogScreenPreview() {
    JayTheme {
        JayDialogContent {
            AboutDialogContent(modifier = Modifier.fillMaxWidth())
        }
    }
}
