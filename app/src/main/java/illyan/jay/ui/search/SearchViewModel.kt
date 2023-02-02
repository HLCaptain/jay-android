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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.search.SearchOptions
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.SearchInteractor
import illyan.jay.service.BaseReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchInteractor: SearchInteractor
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    private val _historyRecords = MutableStateFlow(
        searchInteractor.historyRecords.value.sortedByDescending { it.timestamp }.take(4)
    )
    val historyRecords = _historyRecords.asStateFlow()
    private val _favoriteRecords = MutableStateFlow(searchInteractor.favoriteRecords.value)
    val favoriteRecords = _favoriteRecords.asStateFlow()

    init {
        viewModelScope.launch {
            searchInteractor.historyRecords.collectLatest { records ->
                _historyRecords.value = records.sortedByDescending { it.timestamp }.take(4)
            }
            searchInteractor.favoriteRecords.collectLatest {
                _favoriteRecords.value = it.take(4)
            }
        }
    }

    var searchSuggestions = mutableStateListOf<SearchSuggestion>()
        private set

    private val queryReceiver: BaseReceiver = BaseReceiver {
        searchQuery = it.getStringExtra(KeySearchQuery) ?: ""
        searchInteractor.search(
            searchQuery,
            SearchOptions(
                limit = 8
            )
        ) { suggestions, _ ->
            searchSuggestions.clear()
            searchSuggestions.addAll(suggestions)
        }
    }

    private val searchSelectedReceiver = BaseReceiver {
        searchSuggestions.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
        favoriteRecords.value.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
        historyRecords.value.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
    }

    fun load() {
        loadReceivers()
    }

    private fun loadReceivers() {
        searchInteractor.registerSearchQueryReceiver(queryReceiver)
        searchInteractor.registerSearchSelectedReceiver(searchSelectedReceiver)
    }

    fun navigateTo(searchSuggestion: SearchSuggestion) {
        searchInteractor.navigateTo(searchSuggestion)
    }

    fun navigateTo(record: IndexableRecord) {
        searchInteractor.navigateTo(record)
    }

    fun dispose() {
        searchInteractor.unregisterReceiver(queryReceiver)
        searchInteractor.unregisterReceiver(searchSelectedReceiver)
    }

    companion object {
        const val KeyPlaceQuery = "KEY_PLACE_QUERY"
        const val KeySearchQuery = "KEY_SEARCH_QUERY"
        const val KeySearchSelected = "KEY_SEARCH_SELECTED"
        const val ActionSearchSelected = "ACTION_SEARCH_SELECTED"
    }
}
