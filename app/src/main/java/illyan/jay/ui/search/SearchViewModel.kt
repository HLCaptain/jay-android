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
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.service.BaseReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
    private val searchEngine: SearchEngine
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    var searchSuggestions = mutableStateListOf<SearchSuggestion>()
        private set

    private val receiver: BaseReceiver = BaseReceiver {
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

    fun load() {
        localBroadcastManager.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_SEARCH)
        )
    }

    fun dispose() {
        localBroadcastManager.unregisterReceiver(receiver)
    }

    companion object {
        const val KeyPlaceQuery = "KEY_PLACE_QUERY"
        const val KeySearchQuery = "KEY_SEARCH_QUERY"
    }
}
