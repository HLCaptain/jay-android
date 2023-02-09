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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.search.SearchOptions
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.SearchInteractor
import illyan.jay.domain.toFavoriteRecord
import illyan.jay.service.BaseReceiver
import illyan.jay.ui.search.model.UiRecord
import illyan.jay.ui.search.model.toUiModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchInteractor: SearchInteractor,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchSuggestions = MutableStateFlow(listOf<SearchSuggestion>())
    private val _favoriteRecords = searchInteractor.favoriteRecords
    private val _historyRecords = searchInteractor.historyRecords

    private val _allRecords = combine(
        _historyRecords,
        _favoriteRecords,
        _searchSuggestions
    ) { history, favorite, search ->
        val records = mutableListOf<UiRecord>()
        records.addAll(
            history
                .sortedByDescending { it.timestamp }.take(4)
                .map { it.toUiModel(isFavorite(it.id)) }
        )
        records.addAll(favorite.take(4).map { it.toUiModel(isFavorite(it.id)) })
        records.addAll(search.map { it.toUiModel(isFavorite(it.id)) })
        records.groupBy { it.id }.values.map { it.first() }
    }

    val favoriteRecords = _favoriteRecords.map { favorites ->
        favorites.reversed().take(4).map { it.toUiModel() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val historyRecords = combine(
        _historyRecords,
        _favoriteRecords,
    ) { history, _ ->
        history
            .sortedByDescending { it.timestamp }.take(4)
            .map { it.toUiModel(isFavorite(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val searchSuggestions = combine(
        _searchSuggestions,
        _favoriteRecords,
    ) { suggestions, _ ->
        suggestions.map { it.toUiModel(isFavorite(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions = _isLoadingSuggestions.asStateFlow()

    private val queryReceiver: BaseReceiver = BaseReceiver {
        _searchQuery.value = it.getStringExtra(KeySearchQuery) ?: ""
        _isLoadingSuggestions.value = true
        searchInteractor.search(
            _searchQuery.value,
            SearchOptions(
                limit = 8
            )
        ) { suggestions, _ ->
            _searchSuggestions.value = suggestions
            _isLoadingSuggestions.value = false
        }
    }

    private val searchSelectedReceiver = BaseReceiver {
        _searchSuggestions.value.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
        _favoriteRecords.value.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
        _historyRecords.value.firstOrNull()?.let {
            navigateTo(it)
            return@BaseReceiver
        }
    }

    fun load() {
        loadReceivers()
    }

    private fun isFavorite(id: String) = _favoriteRecords.value.any { it.id == id }

    private fun addRecordToFavorites(id: String) {
        viewModelScope.launch(dispatcherIO) {
            _allRecords.first { records ->
                records.firstOrNull { it.id == id }?.let { _ ->
                    if (_searchSuggestions.value.any { it.id == id } &&
                        _historyRecords.value.none { it.id == id }
                    ) {
                        val suggestion = _searchSuggestions.value.first()
                        searchInteractor.select(
                            selectedSuggestion = suggestion,
                            onResult = { _, result, _ ->
                                searchInteractor.addRecordToFavorites(result.toFavoriteRecord())
                            }
                        )
                    }
                    val historyRecord = _historyRecords.value.firstOrNull { it.id == id }
                    if (historyRecord != null) {
                        searchInteractor.addRecordToFavorites(historyRecord.toFavoriteRecord())
                        return@let
                    }
                    val favoriteRecord = _favoriteRecords.value.firstOrNull { it.id == id }
                    if (favoriteRecord != null) {
                        searchInteractor.addRecordToFavorites(favoriteRecord)
                        return@let
                    }
                }
                true
            }
        }
    }

    private fun removeRecordFromFavorites(id: String) {
        searchInteractor.removeRecordFromFavorites(id)
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch(dispatcherIO) {
            _favoriteRecords.first { records ->
                if (records.any { it.id == id }) {
                    removeRecordFromFavorites(id)
                } else {
                    addRecordToFavorites(id)
                }
                true
            }
        }
    }

    private fun loadReceivers() {
        searchInteractor.registerSearchQueryReceiver(queryReceiver)
        searchInteractor.registerSearchSelectedReceiver(searchSelectedReceiver)
    }

    fun navigateTo(searchSuggestion: SearchSuggestion) {
        searchInteractor.navigateTo(searchSuggestion)
    }

    fun navigateTo(id: String) {
        val historyRecord = _historyRecords.value.firstOrNull { it.id == id }
        if (historyRecord != null) {
            navigateTo(historyRecord)
            return
        }
        val favoriteRecord = _favoriteRecords.value.firstOrNull { it.id == id }
        if (favoriteRecord != null) {
            navigateTo(favoriteRecord)
            return
        }
        val searchSuggestion = _searchSuggestions.value.firstOrNull { it.id == id }
        if (searchSuggestion != null) {
            navigateTo(searchSuggestion)
            return
        }
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
