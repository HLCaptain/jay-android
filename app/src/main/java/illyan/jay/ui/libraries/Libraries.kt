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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.destinations.LibraryDialogScreenDestination
import illyan.jay.ui.libraries.model.Library
import illyan.jay.ui.libraries.model.License
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.search.DividerThickness
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun LibrariesDialogScreen(
    viewModel: LibrariesViewModel = hiltViewModel(),
    destinationsNavigator: DestinationsNavigator = EmptyDestinationsNavigator,
) {
    val libraries by viewModel.libraries.collectAsStateWithLifecycle()
    LibrariesDialogContent(
        modifier = Modifier.fillMaxWidth(),
        libraries = libraries,
        onSelectLibrary = { destinationsNavigator.navigate(LibraryDialogScreenDestination(it)) },
    )
}

@Composable
fun LibrariesDialogContent(
    modifier: Modifier = Modifier,
    libraries: List<Library> = emptyList(),
    onSelectLibrary: (Library) -> Unit = {},
) {
    JayDialogContent(
        modifier = modifier,
        title = { LibrariesTitle() },
        text = {
            LibrariesScreen(
                libraries = libraries,
                onSelectLibrary = onSelectLibrary,
            )
        }
    )
}

@Composable
fun LibrariesTitle() {
    Text(
        text = stringResource(id = R.string.libraries),
        style = MaterialTheme.typography.headlineSmall,
        color = AlertDialogDefaults.titleContentColor,
    )
}

@Composable
fun LibrariesScreen(
    modifier: Modifier = Modifier,
    libraries: List<Library> = emptyList(),
    onSelectLibrary: (Library) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(libraries) { index, item ->
            LibraryItem(
                modifier = Modifier.padding(vertical = 2.dp),
                library = item,
                onClick = { onSelectLibrary(item) }
            )
            if (index != libraries.size - 1) {
                Divider(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 36.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = DividerThickness,
                                bottomStart = DividerThickness,
                                topEnd = DividerThickness,
                                bottomEnd = DividerThickness
                            )
                        ),
                    thickness = DividerThickness * 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItem(
    modifier: Modifier = Modifier,
    library: Library,
    cardColors: CardColors = CardDefaults.cardColors(
        containerColor = Color.Transparent
    ),
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = cardColors,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            val (item, icon) = createRefs()
            createHorizontalChain(
                item,
                icon,
                chainStyle = ChainStyle.SpreadInside
            )
            createStartBarrier()
            Icon(
                modifier = Modifier.constrainAs(icon) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                imageVector = Icons.Rounded.ChevronRight, contentDescription = ""
            )
            LazyRow(
                modifier = Modifier.constrainAs(item) {
                    start.linkTo(parent.start)
                    end.linkTo(icon.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = AlertDialogDefaults.titleContentColor,
                        )
                        AnimatedVisibility(visible = library.repositoryUrl != null) {
                            library.repositoryUrl?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AlertDialogDefaults.textContentColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDarkTheme
@Composable
private fun LibrariesDialogContentPreview() {
    val libraries = listOf(
        Library(
            name = "Compose Scrollbar",
            license = License.Builder()
                .setAuthor("Balázs Püspök-Kiss (Illyan)")
                .setYear(2023)
                .setType(License.LicenseType.ApacheV2)
                .build(),
            repositoryUrl = "https://github.com/HLCaptain/compose-scrollbar",
            moreInfoUrl = "https://github.com/HLCaptain/compose-scrollbar"
        ),
        Library(
            name = "Plumber",
            license = License.Builder()
                .setAuthor("Balázs Püspök-Kiss (Illyan)")
                .setYear(2023)
                .setType(License.LicenseType.ApacheV2)
                .build(),
            repositoryUrl = "https://github.com/HLCaptain/plumber"
        ),
        Library(
            name = "swipe",
            license = License.Builder()
                .setAuthor("Saket Narayan")
                .setYear(2022)
                .setType(License.LicenseType.ApacheV2)
                .build(),
            repositoryUrl = "https://github.com/saket/swipe"
        )
    )
    JayTheme {
        JayDialogContent {
            LibrariesDialogContent(libraries = libraries)
        }
    }
}
