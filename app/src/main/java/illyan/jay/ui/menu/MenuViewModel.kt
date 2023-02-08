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

package illyan.jay.ui.menu

import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.mapbox.search.result.SearchResultType
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.ui.home.sendBroadcast
import illyan.jay.ui.map.BmeK
import illyan.jay.ui.poi.model.Place
import illyan.jay.ui.search.SearchViewModel
import illyan.jay.ui.sheet.SheetViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val localBroadcastManager: LocalBroadcastManager,
) : ViewModel() {
    private fun onClickButton(buttonName: String) {
        Timber.i("Clicked \"$buttonName\" button")
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, buttonName)
        }
    }
    fun onClickFreeDriveButton() {
        onClickButton("FreeDrive")
    }

    fun onClickSessionsButton() {
        onClickButton("Sessions")
    }

    fun onClickNavigateToBmeButton(localizedNameOfBme: String) {
        onClickButton("Navigating to BME")
        localBroadcastManager.sendBroadcast(
            Place(
                name = localizedNameOfBme,
                type = SearchResultType.POI,
                latitude = BmeK.latitude,
                longitude = BmeK.longitude
            ),
            SearchViewModel.KeyPlaceQuery,
            SheetViewModel.ACTION_QUERY_PLACE
        )
    }
}
