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

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import illyan.compose.scrollbar.drawVerticalScrollbar
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.PreviewAll
import illyan.jay.ui.destinations.LibraryDialogScreenDestination
import illyan.jay.ui.libraries.model.UiLibrary
import illyan.jay.ui.profile.ProfileNavGraph
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
    libraries: List<UiLibrary> = emptyList(),
    onSelectLibrary: (UiLibrary) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val maxHeight = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> configuration.screenHeightDp
        Configuration.ORIENTATION_LANDSCAPE -> configuration.screenWidthDp
        else -> configuration.screenHeightDp
    }
    JayDialogContent(
        modifier = modifier,
        textModifier = Modifier.heightIn(max = (maxHeight * 0.55f).dp),
        title = { LibrariesTitle() },
        text = {
            LibrariesScreen(
                libraries = libraries,
                onSelectLibrary = onSelectLibrary,
            )
        },
        textPaddingValues = PaddingValues()
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
    libraries: List<UiLibrary> = emptyList(),
    onSelectLibrary: (UiLibrary) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    val verticalContentPadding = 12.dp
    LazyColumn(
        modifier = modifier
            .drawVerticalScrollbar(state = lazyListState)
            .clip(RoundedCornerShape(verticalContentPadding))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
    ) {
        items(libraries) { item ->
            val cardColors = CardDefaults.cardColors(containerColor = Color.Transparent)
            LibraryItem(
                library = item,
                onClick = { onSelectLibrary(item) },
                cardColors = cardColors,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItem(
    modifier: Modifier = Modifier,
    library: UiLibrary,
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
                .padding(horizontal = 8.dp, vertical = 6.dp),
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
                    Column {
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = AlertDialogDefaults.titleContentColor,
                        )
                        Crossfade(
                            targetState = library.repositoryUrl to library.moreInfoUrl,
                            label = "Library URLs"
                        ) { repositoryAndMoreInfoUrls ->
                            val shownText = repositoryAndMoreInfoUrls.run {
                                // Show Repo URL, then More Info URL, then null
                                if (first != null) first else if (second != null) second else null
                            }
                            shownText?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
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

@PreviewAll
@Composable
private fun LibrariesDialogContentPreview() {
    val libraries = LibrariesViewModel.Libraries
    JayTheme {
        JayDialogContent {
            LibrariesDialogContent(libraries = libraries)
        }
    }
}
