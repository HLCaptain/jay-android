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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import illyan.jay.ui.home.RoundedCornerRadius
import illyan.jay.ui.search.model.SearchResult

@RootNavGraph
@NavGraph
annotation class SearchNavGraph(
    val start: Boolean = false
)

val SearchPadding = 8.dp
val SearchItemsCornerRadius = 24.dp

@Preview(showBackground = true)
@SearchNavGraph(start = true)
@Destination
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
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
        itemsIndexed(viewModel.searchResults) { index, item ->
            val roundedCornerTop = if (index == 0) {
                SearchItemsCornerRadius
            } else {
                0.dp
            }
            val roundedCornerBottom = if (index == viewModel.searchResults.lastIndex) {
                SearchItemsCornerRadius
            } else {
                0.dp
            }
            SearchResultCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = roundedCornerTop,
                            topEnd = roundedCornerTop,
                            bottomStart = roundedCornerBottom,
                            bottomEnd = roundedCornerBottom
                        )
                    ),
                result = item
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultCard(
    modifier: Modifier = Modifier,
    result: SearchResult
) {
    val cardColors = CardDefaults.elevatedCardColors(
        containerColor = Color.Transparent
    )
    // Can be widely generalized. Height is prefered
    // to be limited and the same between list items.
    Surface(
        modifier = modifier,
        color = Color.LightGray
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 4.dp),
            onClick = {
                // Do some navigation to whatever where
            },
            colors = cardColors
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(text = result.title)
                Text(text = result.description)
            }
        }
    }
}
