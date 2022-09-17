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

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mapbox.search.CompletionCallback
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.service.BaseReceiver
import illyan.jay.ui.home.sendBroadcast
import illyan.jay.ui.menu.MenuViewModel
import illyan.jay.ui.navigation.model.Place
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
    private val searchEngine: SearchEngine,
    private val historyDataProvider: HistoryDataProvider,
    private val favoritesDataProvider: FavoritesDataProvider
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    var historyRecords = mutableStateListOf<HistoryRecord>()
        private set

    var favoriteRecords = mutableStateListOf<FavoriteRecord>()
        private set

    var searchSuggestions = mutableStateListOf<SearchSuggestion>()
        private set

    private val queryReceiver: BaseReceiver = BaseReceiver {
        searchQuery = it.getStringExtra(KeySearchQuery) ?: ""
        searchEngine.search(
            searchQuery,
            SearchOptions(
                limit = 8
            ),
            object : SearchSuggestionsCallback {
                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                    searchSuggestions.clear()
                    searchSuggestions.addAll(suggestions)
                }

                override fun onError(e: Exception) {
                    Timber.d("Error message: ${e.message}")
                    e.printStackTrace()
                }
            }
        )
    }

    private val navigationCallReceiver = BaseReceiver {
        searchSuggestions.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
        favoriteRecords.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
        historyRecords.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
    }

    private val searchSelectionCallback = object : SearchSelectionCallback {
        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            results.firstOrNull()?.let { navigateTo(it) }
        }

        override fun onError(e: Exception) {
            Timber.d("Error occured while getting search result!")
            e.printStackTrace()
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            navigateTo(result)
        }

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            // Not needed
        }
    }

    fun load() {
        loadFavorites()
        loadHistory()
        loadReceiver()
    }

    private fun loadFavorites() {
        favoritesDataProvider.getAll(
            object : CompletionCallback<List<FavoriteRecord>> {
                override fun onComplete(result: List<FavoriteRecord>) {
                    favoriteRecords.clear()
                    favoriteRecords.addAll(result.takeLast(4))
                }

                override fun onError(e: Exception) {
                    Timber.d("Error occured while getting favorite records!")
                }
            }
        )
    }

    private fun loadHistory() {
        historyDataProvider.getAll(
            object : CompletionCallback<List<HistoryRecord>> {
                override fun onComplete(result: List<HistoryRecord>) {
                    historyRecords.clear()
                    historyRecords.addAll(result.sortedBy { it.timestamp }.takeLast(4))
                }

                override fun onError(e: Exception) {
                    Timber.d("Error occured while getting history records!")
                }
            }
        )
    }

    private fun loadReceiver() {
        localBroadcastManager.registerReceiver(
            queryReceiver,
            IntentFilter(Intent.ACTION_SEARCH)
        )
        localBroadcastManager.registerReceiver(
            navigationCallReceiver,
            IntentFilter(ActionSelectFirst)
        )
    }

    fun navigateTo(searchSuggestion: SearchSuggestion) {
        searchEngine.select(
            searchSuggestion,
            searchSelectionCallback
        )
    }

    fun navigateTo(searchResult: SearchResult) {
        searchResult.coordinate?.let {
            navigateTo(
                Place(
                    longitude = it.longitude(),
                    latitude = it.latitude()
                )
            )
        }
    }

    fun navigateTo(record: IndexableRecord) {
        record.coordinate?.let {
            navigateTo(
                Place(
                    longitude = it.longitude(),
                    latitude = it.latitude()
                )
            )
        }
    }

    private fun navigateTo(place: Place) {
        localBroadcastManager.sendBroadcast(
            Place(
                latitude = place.latitude,
                longitude = place.longitude
            ),
            KeyPlaceQuery,
            MenuViewModel.ACTION_QUERY_PLACE
        )
    }

    fun dispose() {
        localBroadcastManager.unregisterReceiver(queryReceiver)
        localBroadcastManager.unregisterReceiver(navigationCallReceiver)
    }

    companion object {
        const val KeyPlaceQuery = "KEY_PLACE_QUERY"
        const val KeySearchQuery = "KEY_SEARCH_QUERY"
        const val ActionSelectFirst = "ACTION_SELECT_FIRST"
    }
}
