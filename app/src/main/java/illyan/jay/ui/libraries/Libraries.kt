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

package illyan.jay.ui.libraries

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import illyan.jay.ui.about.AboutNavGraph
import illyan.jay.ui.libraries.model.Library

@AboutNavGraph
@Destination
@Composable
fun Libraries(
    viewModel: LibrariesViewModel = hiltViewModel(),
) {
    val libraries by viewModel.libraries.collectAsStateWithLifecycle()
    LibrariesScreen(
        libraries = libraries
    )
}

@Composable
fun LibrariesScreen(
    modifier: Modifier = Modifier,
    libraries: List<Library> = emptyList()
) {
    LibrariesList(
        libraries = libraries
    )
}

@Composable
fun LibrariesList(
    modifier: Modifier = Modifier,
    libraries: List<Library> = emptyList()
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(libraries) {
            LibraryItem(it)
        }
    }
}

@Composable
fun LibraryItem(
    library: Library
) {
    Card {
        Column {
            Text(text = library.name)
            library.repositoryUrl?.let { Text(text = it) }
        }
    }
}