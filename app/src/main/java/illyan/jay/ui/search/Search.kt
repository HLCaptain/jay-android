/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.YoutubeSearchedFor
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchSuggestion
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.R
import illyan.jay.ui.components.LightDarkThemePreview
import illyan.jay.ui.home.RoundedCornerRadius

@RootNavGraph
@NavGraph
annotation class SearchNavGraph(
    val start: Boolean = false
)

val SearchPadding = 8.dp
val SearchItemsCornerRadius = 24.dp
val DividerStartPadding = 56.dp
val DividerThickness = 1.dp

@SearchNavGraph(start = true)
@Destination
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val statusBarTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { viewModel.dispose() }
    }
    val focusManager = LocalFocusManager.current
    val favoriteItems by viewModel.favoriteRecords.collectAsState()
    val historyItems by viewModel.historyRecords.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SearchPadding),
        contentPadding = PaddingValues(
            top = SearchPadding + statusBarTopPadding,
            bottom = RoundedCornerRadius + SearchPadding
        )
    ) {
        if (viewModel.searchQuery.isNotBlank()) {
            item {
                Text(
                    modifier = Modifier.padding(SearchPadding),
                    text = stringResource(R.string.suggestions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            suggestionItems(
                items = viewModel.searchSuggestions
            ) { suggestion, _ ->
                focusManager.clearFocus()
                viewModel.navigateTo(suggestion)
            }
        }
        item {
            Text(
                modifier = Modifier.padding(SearchPadding),
                text = stringResource(R.string.favorites),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        favoriteItems(
            items = favoriteItems
        ) { favoriteRecord, _ ->
            focusManager.clearFocus()
            viewModel.navigateTo(favoriteRecord)
        }
        item {
            Text(
                modifier = Modifier.padding(SearchPadding),
                text = stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        historyItems(
            items = historyItems
        ) { favoriteRecord, _ ->
            focusManager.clearFocus()
            viewModel.navigateTo(favoriteRecord)
        }
    }
}

fun LazyListScope.historyItems(
    items: List<HistoryRecord>,
    onClick: (HistoryRecord, Int) -> Unit = { _, _ -> }
) {
    searchItems(
        list = items
    ) { historyRecord, index ->
        val roundedCornerTop = if (index == 0) SearchItemsCornerRadius else 0.dp
        val roundedCornerBottom = if (index == items.lastIndex) SearchItemsCornerRadius else 0.dp
        HistoryCard(
            modifier = Modifier.fillMaxWidth(),
            result = historyRecord,
            onClick = { onClick(historyRecord, index) },
            shape = RoundedCornerShape(
                topStart = roundedCornerTop,
                topEnd = roundedCornerTop,
                bottomStart = roundedCornerBottom,
                bottomEnd = roundedCornerBottom
            )
        )
    }
}

fun LazyListScope.favoriteItems(
    items: List<FavoriteRecord>,
    onClick: (FavoriteRecord, Int) -> Unit = { _, _ -> }
) {
    searchItems(
        list = items
    ) { favoriteRecord, index ->
        val roundedCornerTop = if (index == 0) SearchItemsCornerRadius else 0.dp
        val roundedCornerBottom = if (index == items.lastIndex) SearchItemsCornerRadius else 0.dp
        FavoriteCard(
            modifier = Modifier.fillMaxWidth(),
            result = favoriteRecord,
            onClick = { onClick(favoriteRecord, index) },
            shape = RoundedCornerShape(
                topStart = roundedCornerTop,
                topEnd = roundedCornerTop,
                bottomStart = roundedCornerBottom,
                bottomEnd = roundedCornerBottom
            )
        )
    }
}

fun LazyListScope.suggestionItems(
    items: List<SearchSuggestion>,
    onClick: (SearchSuggestion, Int) -> Unit = { _, _ -> }
) {
    searchItems(
        list = items
    ) { suggestion, index ->
        val roundedCornerTop = if (index == 0) SearchItemsCornerRadius else 0.dp
        val roundedCornerBottom = if (index == items.lastIndex) SearchItemsCornerRadius else 0.dp
        SuggestionCard(
            modifier = Modifier.fillMaxWidth(),
            result = suggestion,
            onClick = { onClick(suggestion, index) },
            shape = RoundedCornerShape(
                topStart = roundedCornerTop,
                topEnd = roundedCornerTop,
                bottomStart = roundedCornerBottom,
                bottomEnd = roundedCornerBottom
            )
        )
    }
}

fun <Item> LazyListScope.searchItems(
    list: List<Item> = SnapshotStateList(),
    emptyListPlaceholder: @Composable () -> Unit = {
        SearchCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.list_is_empty),
            description = stringResource(R.string.list_is_empty_description),
            icon = Icons.Rounded.Info,
            shape = RoundedCornerShape(SearchItemsCornerRadius)
        )
    },
    itemProvider: @Composable (Item, Int) -> Unit
) {
    if (list.isEmpty()) {
        item {
            emptyListPlaceholder()
        }
    }
    itemsIndexed(list) { index, item ->
        if (index > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                Divider(
                    modifier = Modifier
                        .padding(start = DividerStartPadding)
                        .clip(
                            RoundedCornerShape(
                                topStart = DividerThickness / 2f,
                                bottomStart = DividerThickness / 2f
                            )
                        ),
                    thickness = DividerThickness,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
                )
            }
        }
        itemProvider(item, index)
    }
}

@LightDarkThemePreview
@Composable
fun SuggestionCard(
    modifier: Modifier = Modifier,
    result: SearchSuggestion? = null,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {}
) {
    SearchCard(
        modifier = modifier,
        title = result?.name ?: "Suggestion Title",
        description = result?.address?.region ?: "Suggestion description",
        icon = Icons.Rounded.Search,
        shape = shape,
        onClick = onClick
    )
}

@LightDarkThemePreview
@Composable
fun HistoryCard(
    modifier: Modifier = Modifier,
    result: HistoryRecord? = null,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {}
) {
    SearchCard(
        modifier = modifier,
        title = result?.name ?: "History Record Title",
        description = result?.address?.region ?: "History record description",
        icon = Icons.Rounded.YoutubeSearchedFor,
        shape = shape,
        onClick = onClick
    )
}

@LightDarkThemePreview
@Composable
fun FavoriteCard(
    modifier: Modifier = Modifier,
    result: FavoriteRecord? = null,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {}
) {
    SearchCard(
        modifier = modifier,
        title = result?.name ?: "Favorite Record Title",
        description = result?.address?.region ?: "Favorite record description",
        icon = Icons.Rounded.Favorite,
        shape = shape,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCard(
    modifier: Modifier = Modifier,
    title: String = "Title",
    description: String = "Description",
    icon: ImageVector = Icons.Rounded.Search,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {}
) {
    // Can be widely generalized. Height is prefered
    // to be limited and the same between list items.
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        contentColor = tint
    )
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = cardColors,
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = icon,
                    tint = tint,
                    contentDescription = "Search Item Icon"
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
