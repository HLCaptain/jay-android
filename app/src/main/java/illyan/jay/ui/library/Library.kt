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
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
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
        title = { LibraryTitle(text = library.name) },
        text = {
            LibraryScreen(library = library)
        }
    )
}

@Composable
fun LibraryTitle(text: String) {
    Text(text = text)
}

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    library: Library
) {
    Column(
        modifier = modifier
    ) {
        AnimatedVisibility(visible = library.repositoryUrl != null) {
            library.repositoryUrl?.let { Text(text = it) }
        }
        AnimatedVisibility(visible = library.moreInfoUrl != null) {
            library.moreInfoUrl?.let { Text(text = it) }
        }
        AnimatedVisibility(visible = library.license?.type != null) {
            library.license?.type?.let { Text(text = it) }
        }
        AnimatedVisibility(visible = library.license?.description != null) {
            library.license?.description?.let { Text(text = it) }
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