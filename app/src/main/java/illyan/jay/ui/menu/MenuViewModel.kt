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

package illyan.jay.ui.menu

import android.content.IntentFilter
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.service.BaseReceiver
import illyan.jay.ui.navigation.model.Place
import illyan.jay.ui.search.SearchViewModel.Companion.KeyPlaceQuery
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val localBroadcastManager: LocalBroadcastManager
) : ViewModel() {
    // TODO: make some items worth storing, or delete this later if not used
    var menuItems = mutableStateListOf<String>()
        private set

    var onReceived: (Place) -> Unit = {}

    private val receiver: BaseReceiver = BaseReceiver { intent ->
        intent.getParcelableExtra(KeyPlaceQuery, Place::class.java)?.let {
            onReceived(it)
        }
    }

    fun load(
        onReceived: (Place) -> Unit
    ) {
        this.onReceived = onReceived
        localBroadcastManager.registerReceiver(
            receiver,
            IntentFilter.create(
                ACTION_QUERY_PLACE,
                "text/*"
            )
        )
        Timber.d("Registered $ACTION_QUERY_PLACE receiver!")
    }

    fun dispose() {
        localBroadcastManager.unregisterReceiver(receiver)
        Timber.d("Menu broadcast receiver disposed!")
    }

    companion object {
        const val ACTION_QUERY_PLACE = "illyan.jay.action.QUERY_PLACE"
    }
}
