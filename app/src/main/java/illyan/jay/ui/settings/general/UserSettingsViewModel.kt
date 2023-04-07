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

package illyan.jay.ui.settings.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.ui.settings.general.model.toUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val authInteractor: AuthInteractor
) : ViewModel() {
    val userPreferences = combine(
        settingsInteractor.userPreferences,
        settingsInteractor.appSettingsFlow
    ) { preferences, appSettings ->
        preferences?.toUiModel(appSettings.clientUUID)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val arePreferencesSynced = settingsInteractor.arePreferencesSynced

    val isUserSignedIn = authInteractor.isUserSignedInStateFlow

    val shouldSyncPreferences = settingsInteractor.shouldSyncPreferences

    val canSyncPreferences = settingsInteractor.canSyncPreferences

    fun setPreferencesSync(shouldSync: Boolean) {
        settingsInteractor.shouldSync = shouldSync
    }

    fun setAnalytics(enabled: Boolean) {
        settingsInteractor.analyticsEnabled = enabled
    }

    fun setFreeDriveAutoStart(enabled: Boolean) {
        settingsInteractor.freeDriveAutoStart = enabled
    }

    fun setAdVisibility(visible: Boolean) {
        settingsInteractor.showAds = visible
    }
}