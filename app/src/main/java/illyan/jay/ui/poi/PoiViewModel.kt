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

package illyan.jay.ui.poi

import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mapbox.geojson.Point
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.SearchInteractor
import illyan.jay.service.BaseReceiver
import illyan.jay.ui.poi.model.Place
import illyan.jay.ui.poi.model.toUiModel
import illyan.jay.ui.search.SearchViewModel
import illyan.jay.ui.sheet.SheetViewModel.Companion.ACTION_QUERY_PLACE
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PoiViewModel @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager,
    private val searchInteractor: SearchInteractor
) : ViewModel() {

    private val _places = MutableStateFlow(persistentListOf<Place>())
    val place = _places
        .map { it.lastOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    var isNewPlace by mutableStateOf(true)

    private val _infoForPlace = MutableStateFlow(persistentHashMapOf<Place, SearchResult>())
    val placeInfo = combine(
        _infoForPlace,
        _places
    ) { infoForPlace, places ->
        places.lastOrNull()?.let { lastPlace ->
            infoForPlace[lastPlace]?.toUiModel()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val shouldShowAddress = place.map {
        if (it == null) {
            true
        } else {
            if (it.type == null) {
                true
            } else {
                when (it.type) {
                    SearchResultType.REGION -> false
                    SearchResultType.POSTCODE -> false
                    SearchResultType.COUNTRY -> false
                    else -> true
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    private val receiver: BaseReceiver = BaseReceiver { intent ->
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(SearchViewModel.KeyPlaceQuery, Place::class.java)?.let {
                _places.value = _places.value.add(it)
                searchPlace(it)
                isNewPlace = true
            }
        } else {
            intent.getParcelableExtra<Place>(SearchViewModel.KeyPlaceQuery)?.let {
                _places.value = _places.value.add(it)
                searchPlace(it)
                isNewPlace = true
            }
        }
    }

    fun load(
        place: Place
    ) {
        _places.value = _places.value.add(place)
        searchPlace(place)
        isNewPlace = true
        localBroadcastManager.registerReceiver(
            receiver,
            IntentFilter(ACTION_QUERY_PLACE)
        )
        Timber.d("Registered $ACTION_QUERY_PLACE receiver!")
    }

    private fun searchPlace(place: Place) {
        searchInteractor.search(
            ReverseGeoOptions(
                center = Point.fromLngLat(place.longitude, place.latitude),
                limit = 1,
            )
        ) { results, _ ->
            results.firstOrNull()?.let {
                _infoForPlace.value = _infoForPlace.value.put(place, it)
            }
        }
    }

    fun dispose() {
        localBroadcastManager.unregisterReceiver(receiver)
        Timber.d("Navigation broadcast receiver disposed!")
    }
}
