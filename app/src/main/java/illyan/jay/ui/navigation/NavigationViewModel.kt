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

package illyan.jay.ui.navigation

import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.service.BaseReceiver
import illyan.jay.ui.map.ButeK
import illyan.jay.ui.menu.MenuViewModel
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.search.SearchViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager
) : ViewModel() {
    var place by mutableStateOf(ButeK)
        private set

    var isNewPlace by mutableStateOf(true)

    private val receiver: BaseReceiver = BaseReceiver { intent ->
        if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(SearchViewModel.KeyPlaceQuery, Place::class.java)?.let {
                place = it
                isNewPlace = true
            }
        } else {
            intent.getParcelableExtra<Place>(SearchViewModel.KeyPlaceQuery)?.let {
                place = it
                isNewPlace = true
            }
        }
    }

    fun load(
        place: Place
    ) {
        this.place = place
        isNewPlace = true
        localBroadcastManager.registerReceiver(
            receiver,
            IntentFilter(MenuViewModel.ACTION_QUERY_PLACE)
        )
        Timber.d("Registered ${MenuViewModel.ACTION_QUERY_PLACE} receiver!")
    }

    fun dispose() {
        localBroadcastManager.unregisterReceiver(receiver)
        Timber.d("Navigation broadcast receiver disposed!")
    }
}
