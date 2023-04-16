/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.domain.model.DomainPreferences
import illyan.jay.util.FirebaseRemoteConfigKeys
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val remoteConfig: FirebaseRemoteConfig,
) : ViewModel() {

    val showAds = settingsInteractor.userPreferences.map {
        it?.showAds ?: DomainPreferences.Default.showAds
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DomainPreferences.Default.showAds)

    val aboutBannerAdUnitId = remoteConfig[FirebaseRemoteConfigKeys.BannerOnAboutScreenAdUnitIdKey].asString()

    fun setAdVisibility(visible: Boolean) {
        settingsInteractor.showAds = visible
    }
}
