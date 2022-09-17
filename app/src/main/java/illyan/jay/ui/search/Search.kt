/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mapbox.search.result.SearchSuggestion
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.home.sendBroadcast
import illyan.jay.ui.map.ButeK
import illyan.jay.ui.menu.MenuViewModel.Companion.ACTION_QUERY_PLACE
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.search.SearchViewModel.Companion.KeyPlaceQuery
import illyan.jay.ui.theme.Neutral90
import illyan.jay.ui.theme.Neutral95

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
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val localBroadcastManager = LocalBroadcastManager.getInstance(context)
    DisposableEffect(key1 = true) {
        viewModel.load()
        onDispose { viewModel.dispose() }
    }
    // LazyColumn with SearchResultCards.
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SearchPadding),
        contentPadding = PaddingValues(
            top = SearchPadding,
            bottom = RoundedCornerRadius + SearchPadding
        )
    ) {
        itemsIndexed(viewModel.searchSuggestions) { index, item ->
            val roundedCornerTop = if (index == 0) {
                SearchItemsCornerRadius
            } else {
                0.dp
            }
            val roundedCornerBottom = if (index == viewModel.searchSuggestions.lastIndex) {
                SearchItemsCornerRadius
            } else {
                0.dp
            }
            Surface(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = roundedCornerTop,
                            topEnd = roundedCornerTop,
                            bottomStart = roundedCornerBottom,
                            bottomEnd = roundedCornerBottom
                        )
                    ),
                color = Neutral95
            ) {
                if (index > 0) {
                    Divider(
                        thickness = DividerThickness,
                        color = Neutral90,
                        modifier = Modifier
                            .padding(start = DividerStartPadding)
                            .clip(
                                RoundedCornerShape(
                                    topStart = DividerThickness / 2f,
                                    bottomStart = DividerThickness / 2f
                                )
                            )
                    )
                }
                Column {
                    SearchSuggestionCard(
                        modifier = Modifier.fillMaxWidth(),
                        result = item,
                        onClick = {
                            focusManager.clearFocus()
                            localBroadcastManager.sendBroadcast(
                                Place(
                                    latitude = ButeK.latitude,
                                    longitude = ButeK.longitude
                                ),
                                KeyPlaceQuery,
                                ACTION_QUERY_PLACE
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * This search result should only be a POI
 * TODO: make several other cards for different types of results
 */
@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSuggestionCard(
    modifier: Modifier = Modifier,
    result: SearchSuggestion? = null,
    onClick: () -> Unit = {}
) {
    val cardColors = CardDefaults.elevatedCardColors(
        containerColor = Color.Transparent
    )
    // Can be widely generalized. Height is prefered
    // to be limited and the same between list items.
    Card(
        onClick = onClick,
        colors = cardColors
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
                    imageVector = Icons.Rounded.LocalCafe,
                    contentDescription = "Placeholder POI Icon"
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    text = result?.name ?: "Suggestion Title",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = result?.descriptionText ?: "Suggestion description",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
