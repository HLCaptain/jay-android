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

package illyan.jay.domain.interactor

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.mutableStateListOf
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
import com.mapbox.search.record.LocalDataProvider
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import illyan.jay.service.BaseReceiver
import illyan.jay.ui.home.sendBroadcast
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.search.SearchViewModel
import illyan.jay.ui.search.SearchViewModel.Companion.ActionSearchSelected
import illyan.jay.ui.sheet.SheetViewModel.Companion.ACTION_QUERY_PLACE
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchInteractor @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
    private val searchEngine: SearchEngine,
    private val historyDataProvider: HistoryDataProvider,
    private val favoritesDataProvider: FavoritesDataProvider
) {
    var historyRecords = mutableStateListOf<HistoryRecord>()
        private set

    var favoriteRecords = mutableStateListOf<FavoriteRecord>()
        private set

    private val historyDataChangedListener =
        object : LocalDataProvider.OnDataChangedListener<HistoryRecord> {
            override fun onDataChanged(newData: List<HistoryRecord>) {
                historyRecords.clear()
                historyRecords.addAll(newData)
            }
        }

    private val favoritesDataChangedListener =
        object : LocalDataProvider.OnDataChangedListener<FavoriteRecord> {
            override fun onDataChanged(newData: List<FavoriteRecord>) {
                favoriteRecords.clear()
                favoriteRecords.addAll(newData)
            }
        }

    init {
        loadFavorites()
        loadHistory()
    }

    private fun loadFavorites() {
        favoritesDataProvider.addOnDataChangedListener(favoritesDataChangedListener)
        favoritesDataProvider.getAll(
            object : CompletionCallback<List<FavoriteRecord>> {
                override fun onComplete(result: List<FavoriteRecord>) {
                    favoriteRecords.clear()
                    favoriteRecords.addAll(result)
                }

                override fun onError(e: Exception) {
                    Timber.d("Error occured while getting favorite records!")
                }
            }
        )
    }

    private fun loadHistory() {
        historyDataProvider.addOnDataChangedListener(historyDataChangedListener)
        historyDataProvider.getAll(
            object : CompletionCallback<List<HistoryRecord>> {
                override fun onComplete(result: List<HistoryRecord>) {
                    historyRecords.clear()
                    historyRecords.addAll(result)
                }

                override fun onError(e: Exception) {
                    Timber.d("Error occured while getting history records!")
                }
            }
        )
    }

    fun navigateTo(searchSuggestion: SearchSuggestion) {
        select(
            searchSuggestion,
            onResult = { _, result, _ ->
                navigateTo(result)
            }
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

    fun navigateTo(place: Place) {
        localBroadcastManager.sendBroadcast(
            Place(
                latitude = place.latitude,
                longitude = place.longitude
            ),
            SearchViewModel.KeyPlaceQuery,
            ACTION_QUERY_PLACE
        )
    }

    fun select(
        suggestionSelected: SearchSuggestion,
        onResult: (
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) -> Unit,
        onCategoryResult: (
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) -> Unit = { suggestion, results, responseInfo ->
            results.firstOrNull()?.let {
                onResult(suggestion, it, responseInfo)
            }
        },
        onError: (e: Exception) -> Unit = {
            Timber.d("Error selecting suggestion \"${suggestionSelected.name}\"")
            it.printStackTrace()
        },
        onSuggestions: (
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) -> Unit = { _, _ -> }
    ) {
        searchEngine.select(
            suggestionSelected,
            object : SearchSelectionCallback {
                override fun onCategoryResult(
                    suggestion: SearchSuggestion,
                    results: List<SearchResult>,
                    responseInfo: ResponseInfo
                ) {
                    onCategoryResult(suggestion, results, responseInfo)
                }

                override fun onError(e: Exception) {
                    onError(e)
                }

                override fun onResult(
                    suggestion: SearchSuggestion,
                    result: SearchResult,
                    responseInfo: ResponseInfo
                ) {
                    onResult(suggestion, result, responseInfo)
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                    onSuggestions(suggestions, responseInfo)
                }
            }
        )
    }

    fun search(
        query: String,
        options: SearchOptions = SearchOptions(),
        onError: (e: Exception) -> Unit = {
            Timber.d("Error searching for query \"$query\"")
            it.printStackTrace()
        },
        onSuggestions: (
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) -> Unit
    ) {
        searchEngine.search(
            query = query,
            options = options,
            object : SearchSuggestionsCallback {
                override fun onError(e: Exception) {
                    onError(e)
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                    onSuggestions(suggestions, responseInfo)
                }
            }
        )
    }

    fun registerSearchSelectedReceiver(listener: BaseReceiver) {
        localBroadcastManager.registerReceiver(
            listener,
            IntentFilter(ActionSearchSelected)
        )
    }

    fun registerSearchQueryReceiver(listener: BaseReceiver) {
        localBroadcastManager.registerReceiver(
            listener,
            IntentFilter(Intent.ACTION_SEARCH)
        )
    }

    fun unregisterReceiver(listener: BaseReceiver) {
        localBroadcastManager.unregisterReceiver(listener)
    }
}