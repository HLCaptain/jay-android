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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.destinations.LibrariesDestination
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun AboutDialogScreen(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    AboutDialogContent(
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AboutDialogContent(
    modifier: Modifier = Modifier,
) {
    JayDialogContent(
        modifier = modifier,
        title = {
            // TODO: enable ad button on this screen (only showing one ad on this screen)
            Text(text = stringResource(id = R.string.about))
        },
        buttons = {
            // TODO: Support the project
            // TODO: Dismiss dialog (Cancel)
        },
        text = {

        },
    )
}

@Composable
fun AboutScreen(
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator
) {
    LazyColumn {
        item {
            TextButton(
                onClick = {
                    destinationsNavigator.navigate(LibrariesDestination)
                }
            ) {
                Text(text = "Open Library list")
            }
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun AboutDialogScreenPreview() {
    JayTheme {
        AboutDialogContent()
    }
}
