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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.YoutubeSearchedFor
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.R
import illyan.jay.ui.components.MediumCircularProgressIndicator
import illyan.jay.ui.components.PreviewThemesScreensFonts
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.search.model.UiRecord
import illyan.jay.ui.theme.JayTheme
import illyan.jay.ui.theme.signaturePink
import java.util.UUID

@RootNavGraph
@NavGraph
annotation class SearchNavGraph(
    val start: Boolean = false,
)

val SearchPadding = 8.dp
val SearchItemsCornerRadius = 24.dp
val DividerStartPadding = 56.dp
val DividerThickness = 1.dp

@SearchNavGraph(start = true)
@Destination
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    DisposableEffect(Unit) {
        viewModel.load()
        onDispose { viewModel.dispose() }
    }
    val focusManager = LocalFocusManager.current
    val favoriteItems by viewModel.favoriteRecords.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyRecords.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val isLoadingSuggestions by viewModel.isLoadingSuggestions.collectAsStateWithLifecycle()
    val statusBarTopPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    SearchList(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SearchPadding),
        contentPadding = PaddingValues(
            top = SearchPadding + statusBarTopPadding,
            bottom = RoundedCornerRadius + SearchPadding
        ),
        favoriteItems = favoriteItems,
        historyItems = historyItems,
        searchSuggestions = searchSuggestions,
        isLoadingSuggestions = isLoadingSuggestions,
        onClickSearchSuggestion = { suggestion, _ ->
            focusManager.clearFocus()
            viewModel.navigateTo(suggestion.id)
        },
        onClickFavoriteItem = { favoriteItem, _ ->
            focusManager.clearFocus()
            viewModel.navigateTo(favoriteItem.id)
        },
        onClickHistoryItem = { historyItem, _ ->
            focusManager.clearFocus()
            viewModel.navigateTo(historyItem.id)
        },
        onToggleFavorite = {
            viewModel.toggleFavorite(it)
        }
    )
}

@Composable
fun SearchList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    favoriteItems: List<UiRecord> = emptyList(),
    historyItems: List<UiRecord> = emptyList(),
    searchSuggestions: List<UiRecord> = emptyList(),
    isLoadingSuggestions: Boolean = false,
    onClickSearchSuggestion: (UiRecord, Int) -> Unit = { _, _ -> },
    onClickFavoriteItem: (UiRecord, Int) -> Unit = { _, _ -> },
    onClickHistoryItem: (UiRecord, Int) -> Unit = { _, _ -> },
    onToggleFavorite: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            AnimatedVisibility(visible = isLoadingSuggestions || searchSuggestions.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(SearchPadding),
                    text = stringResource(R.string.suggestions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        suggestionItems(
            items = searchSuggestions,
            onClick = onClickSearchSuggestion,
            isLoadingSuggestions = isLoadingSuggestions,
            onToggleFavorite = onToggleFavorite,
        )
        item {
            Text(
                modifier = Modifier.padding(SearchPadding),
                text = stringResource(R.string.favorites),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        favoriteItems(
            items = favoriteItems,
            onClick = onClickFavoriteItem,
            onRemoveFromFavorites = onToggleFavorite,
        )
        item {
            Text(
                modifier = Modifier.padding(SearchPadding),
                text = stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        historyItems(
            items = historyItems,
            onClick = onClickHistoryItem,
            onToggleFavorite = onToggleFavorite,
        )
    }
}

@PreviewThemesScreensFonts
@Composable
private fun SearchListPreview() {
    JayTheme {
        SearchList()
    }
}

fun LazyListScope.historyItems(
    items: List<UiRecord>,
    onClick: (UiRecord, Int) -> Unit = { _, _ -> },
    onToggleFavorite: (String) -> Unit = {},
) {
    searchItems(
        list = items
    ) { historyRecord, index ->
        val roundedCornerTop = if (index == 0) SearchItemsCornerRadius else 0.dp
        val roundedCornerBottom = if (index == items.lastIndex) SearchItemsCornerRadius else 0.dp
        HistoryCard(
            modifier = Modifier.fillMaxWidth(),
            record = historyRecord,
            onClick = { onClick(historyRecord, index) },
            shape = RoundedCornerShape(
                topStart = roundedCornerTop,
                topEnd = roundedCornerTop,
                bottomStart = roundedCornerBottom,
                bottomEnd = roundedCornerBottom
            ),
            onToggleFavorite = { onToggleFavorite(historyRecord.id) },
        )
    }
}

fun LazyListScope.favoriteItems(
    items: List<UiRecord>,
    onClick: (UiRecord, Int) -> Unit = { _, _ -> },
    onRemoveFromFavorites: (String) -> Unit = {},
) {
    searchItems(
        list = items
    ) { favoriteRecord, index ->
        val roundedCornerTop = if (index == 0) SearchItemsCornerRadius else 0.dp
        val roundedCornerBottom = if (index == items.lastIndex) SearchItemsCornerRadius else 0.dp
        FavoriteCard(
            modifier = Modifier.fillMaxWidth(),
            record = favoriteRecord,
            onClick = { onClick(favoriteRecord, index) },
            shape = RoundedCornerShape(
                topStart = roundedCornerTop,
                topEnd = roundedCornerTop,
                bottomStart = roundedCornerBottom,
                bottomEnd = roundedCornerBottom
            ),
            onRemoveFromFavorites = { onRemoveFromFavorites(favoriteRecord.id) },
        )
    }
}

fun LazyListScope.suggestionItems(
    items: List<UiRecord>,
    isLoadingSuggestions: Boolean = false,
    onClick: (UiRecord, Int) -> Unit = { _, _ -> },
    onToggleFavorite: (String) -> Unit = {},
) {
    searchItems(
        list = items,
        emptyListPlaceholder = {
            AnimatedVisibility(visible = isLoadingSuggestions || items.isNotEmpty()) {
                Crossfade(
                    targetState = isLoadingSuggestions,
                    label = "Suggestion initial loading animation",
                ) {
                    if (it) {
                        SearchCard(
                            shape = RoundedCornerShape(SearchItemsCornerRadius),
                            prefixContent = {
                                MediumCircularProgressIndicator(
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                                text = stringResource(R.string.loading),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        SearchCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.list_is_empty),
                            description = stringResource(R.string.list_is_empty_description),
                            icon = Icons.Rounded.Info,
                            shape = RoundedCornerShape(SearchItemsCornerRadius)
                        )
                    }
                }
            }
        }
    ) { suggestion, index ->
        val roundedCornerTop = if (index == 0) SearchItemsCornerRadius else 0.dp
        val roundedCornerBottom = if (index == items.lastIndex) SearchItemsCornerRadius else 0.dp
        SuggestionCard(
            modifier = Modifier.fillMaxWidth(),
            record = suggestion,
            onClick = { onClick(suggestion, index) },
            shape = RoundedCornerShape(
                topStart = roundedCornerTop,
                topEnd = roundedCornerTop,
                bottomStart = roundedCornerBottom,
                bottomEnd = roundedCornerBottom
            ),
            onToggleFavorite = { onToggleFavorite(suggestion.id) },
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
    itemProvider: @Composable (Item, Int) -> Unit,
) {
    item {
        AnimatedVisibility(visible = list.isEmpty()) {
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

@Composable
fun SuggestionCard(
    modifier: Modifier = Modifier,
    record: UiRecord,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    SearchCard(
        modifier = modifier,
        title = record.title,
        description = record.description ?: "Suggestion description",
        icon = Icons.Rounded.Search,
        shape = shape,
        onClick = onClick,
        suffixContent = {
            FavoriteButton(
                isFavorite = record.isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        }
    )
}

@Composable
fun FavoriteButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    IconButton(
        modifier = modifier.padding(horizontal = 4.dp),
        onClick = onToggleFavorite
    ) {
        Crossfade(
            targetState = isFavorite,
            label = "Favorite button animation",
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = if (it) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Rounded.FavoriteBorder
                },
                tint = if (it) {
                    MaterialTheme.signaturePink
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                contentDescription = "Favorite icon"
            )
        }
    }
}

@PreviewThemesScreensFonts
@Composable
private fun SuggestionCardPreview() {
    JayTheme {
        SuggestionCard(
            record = previewRecord
        )
    }
}

@Composable
fun HistoryCard(
    modifier: Modifier = Modifier,
    record: UiRecord,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    SearchCard(
        modifier = modifier,
        title = record.title,
        description = record.description ?: "History record description",
        icon = Icons.Rounded.YoutubeSearchedFor,
        shape = shape,
        onClick = onClick,
        suffixContent = {
            FavoriteButton(
                isFavorite = record.isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        }
    )
}

@PreviewThemesScreensFonts
@Composable
private fun HistoryCardPreview() {
    JayTheme {
        HistoryCard(
            record = previewRecord
        )
    }
}

@Composable
fun FavoriteCard(
    modifier: Modifier = Modifier,
    record: UiRecord,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {},
    onRemoveFromFavorites: (String) -> Unit = {},
) {
    SearchCard(
        modifier = modifier,
        title = record.title,
        description = record.description ?: "Favorite record description",
        icon = Icons.Rounded.Favorite,
        shape = shape,
        onClick = onClick,
        prefixContent = {
            FavoriteButton(
                isFavorite = true,
                onToggleFavorite = { onRemoveFromFavorites(record.id) }
            )
        }
    )
}

@PreviewThemesScreensFonts
@Composable
private fun FavoriteCardPreview() {
    JayTheme {
        FavoriteCard(
            record = previewRecord
        )
    }
}

private val previewRecord = UiRecord(
    id = UUID.randomUUID().toString(),
    title = "Budapest",
    description = "Hungary",
    isFavorite = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    cardColors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    prefixContent: @Composable () -> Unit = {
        IconButton(
            modifier = Modifier.padding(4.dp),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.Search,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "Search Item Icon"
            )
        }
    },
    suffixContent: @Composable () -> Unit = {},
    shape: Shape = CardDefaults.shape,
    content: @Composable () -> Unit = {}
) {
    // Can be widely generalized. Height is prefered
    // to be limited and the same between list items.
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = cardColors,
        shape = shape,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                prefixContent()
                content()
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                suffixContent()
            }
        }
    }
}

@Composable
fun SearchCard(
    modifier: Modifier = Modifier,
    title: String = "Title",
    description: String = "Description",
    icon: ImageVector = Icons.Rounded.Search,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = CardDefaults.shape,
    onClick: () -> Unit = {},
    prefixContent: @Composable () -> Unit = {
        IconButton(
            modifier = Modifier.padding(horizontal = 4.dp),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                tint = tint,
                contentDescription = "Search Item Icon"
            )
        }
    },
    suffixContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp)
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
    },
) = SearchCard(
    modifier = modifier,
    onClick = onClick,
    shape = shape,
    prefixContent = prefixContent,
    suffixContent = suffixContent,
    content = content,
)
