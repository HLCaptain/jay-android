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

package illyan.jay.domain.interactor

import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.CompletionCallback
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
import illyan.jay.ui.poi.model.Place
import illyan.jay.ui.search.SearchViewModel
import illyan.jay.ui.search.SearchViewModel.Companion.ActionSearchSelected
import illyan.jay.ui.sheet.SheetViewModel.Companion.ACTION_QUERY_PLACE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchInteractor @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
    private val searchEngine: SearchEngine,
    private val historyDataProvider: HistoryDataProvider,
    private val favoritesDataProvider: FavoritesDataProvider,
) {
    private val _historyRecords = MutableStateFlow(listOf<HistoryRecord>())
    val historyRecords = _historyRecords.asStateFlow()

    private val _favoriteRecords = MutableStateFlow(listOf<FavoriteRecord>())
    val favoriteRecords = _favoriteRecords.asStateFlow()

    private val historyDataChangedListener =
        object : LocalDataProvider.OnDataChangedListener<HistoryRecord> {
            override fun onDataChanged(newData: List<HistoryRecord>) {
                Timber.v("History records size = ${newData.size}")
                _historyRecords.value = newData
            }
        }

    private val favoritesDataChangedListener =
        object : LocalDataProvider.OnDataChangedListener<FavoriteRecord> {
            override fun onDataChanged(newData: List<FavoriteRecord>) {
                Timber.v("Favorite records size = ${newData.size}")
                _favoriteRecords.value = newData
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
                    _favoriteRecords.value = result
                }

                override fun onError(e: Exception) {
                    Timber.e(e, "Error occured while getting favorite records: ${e.message}")
                }
            }
        )
    }

    private fun loadHistory() {
        historyDataProvider.addOnDataChangedListener(historyDataChangedListener)
        historyDataProvider.getAll(
            object : CompletionCallback<List<HistoryRecord>> {
                override fun onComplete(result: List<HistoryRecord>) {
                    _historyRecords.value = result
                }

                override fun onError(e: Exception) {
                    Timber.e(e, "Error occured while getting history records: ${e.message}")
                }
            }
        )
    }

    fun addRecordToFavorites(
        favoriteRecord: FavoriteRecord,
        onError: (Exception) -> Unit = { Timber.e(it) },
        onComplete: () -> Unit = {},
    ) = addRecordsToFavorites(
        favoriteRecords = listOf(favoriteRecord),
        onError = onError,
        onComplete = onComplete,
    )

    fun addRecordsToFavorites(
        favoriteRecords: List<FavoriteRecord>,
        onError: (Exception) -> Unit = { Timber.e(it) },
        onComplete: () -> Unit = {},
    ) {
        Timber.i(
            "Adding ${favoriteRecords.size} records to favorites with IDs: ${
                favoriteRecords.joinToString {
                    it.id.take(
                        4
                    )
                }
            }"
        )
        favoritesDataProvider.upsertAll(
            records = favoriteRecords,
            callback = object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    Timber.i("Successfully added ${favoriteRecords.size} records to favorites")
                    onComplete()
                }

                override fun onError(e: Exception) = onError(e)
            }
        )
    }

    fun removeRecordFromFavorites(
        id: String,
        onError: (Exception) -> Unit = { Timber.e(it) },
        onComplete: (Boolean) -> Unit = {},
    ) {
        Timber.i("Removing ${id.take(4)} record from favorites")
        favoritesDataProvider.remove(
            id = id,
            callback = object : CompletionCallback<Boolean> {
                override fun onComplete(result: Boolean) {
                    if (result) {
                        Timber.i("Successfully removed ${id.take(4)} record from favorites")
                    } else {
                        Timber.i("Not removed ${id.take(4)} record from favorites")
                    }
                    onComplete(result)
                }

                override fun onError(e: Exception) = onError(e)
            }
        )
    }

    fun removeRecordsFromFavorites(
        ids: List<String>,
        onError: (Exception) -> Unit = { Timber.e(it) },
        onComplete: (Boolean) -> Unit = {},
    ) {
        ids.forEach {
            removeRecordFromFavorites(
                id = it,
                onError = onError,
                onComplete = onComplete
            )
        }
    }

    fun navigateTo(searchSuggestion: SearchSuggestion) {
        select(
            searchSuggestion,
            onResults = { _, results, _ ->
                results.firstOrNull()?.let { navigateTo(it) }
            }
        )
    }

    fun navigateTo(searchResult: SearchResult) {
        searchResult.coordinate.let {
            navigateTo(
                Place(
                    name = searchResult.name,
                    type = searchResult.types.first(),
                    longitude = it.longitude(),
                    latitude = it.latitude()
                )
            )
        }
    }

    fun navigateTo(record: IndexableRecord) {
        record.coordinate.let {
            navigateTo(
                Place(
                    name = record.name,
                    type = record.type,
                    longitude = it.longitude(),
                    latitude = it.latitude()
                )
            )
        }
    }

    fun navigateTo(place: Place) {
        localBroadcastManager.sendBroadcast(
            Place(
                name = place.name,
                type = place.type,
                latitude = place.latitude,
                longitude = place.longitude
            ),
            SearchViewModel.KeyPlaceQuery,
            ACTION_QUERY_PLACE
        )
    }

    fun select(
        selectedSuggestion: SearchSuggestion,
        onResults: (
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo,
        ) -> Unit,
        onError: (Exception) -> Unit = {
            Timber.e(it, "Error selecting suggestion \"${selectedSuggestion.name}\": ${it.message}")
        },
        onSuggestions: (
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo,
        ) -> Unit = { _, _ -> },
    ) {
        searchEngine.select(
            selectedSuggestion,
            object : SearchSelectionCallback {
                override fun onError(e: Exception) = onError(e)

                override fun onResult(
                    suggestion: SearchSuggestion,
                    result: SearchResult,
                    responseInfo: ResponseInfo,
                ) = onResults(suggestion, listOf(result), responseInfo)

                override fun onResults(
                    suggestion: SearchSuggestion,
                    results: List<SearchResult>,
                    responseInfo: ResponseInfo,
                ) = onResults(suggestion, results, responseInfo)

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo,
                ) = onSuggestions(suggestions, responseInfo)
            }
        )
    }

    fun search(
        reverseGeoOptions: ReverseGeoOptions,
        onError: (Exception) -> Unit = {
            Timber.e(it, "Error searching for point ${reverseGeoOptions.center}: ${it.message}")
        },
        onSuggestions: (
            results: List<SearchResult>,
            responseInfo: ResponseInfo,
        ) -> Unit,
    ) {
        try {
            searchEngine.search(
                options = reverseGeoOptions,
                callback = object : SearchCallback {
                    override fun onError(e: Exception) = onError(e)
                    override fun onResults(
                        results: List<SearchResult>,
                        responseInfo: ResponseInfo,
                    ) = onSuggestions(results, responseInfo)
                }
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun search(
        query: String,
        options: SearchOptions = SearchOptions(),
        onError: (Exception) -> Unit = {
            Timber.e(it, "Error searching for query \"$query\": ${it.message}")
        },
        onSuggestions: (
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo,
        ) -> Unit,
    ) {
        searchEngine.search(
            query = query,
            options = options,
            object : SearchSuggestionsCallback {
                override fun onError(e: Exception) = onError(e)
                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo,
                ) = onSuggestions(suggestions, responseInfo)
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


