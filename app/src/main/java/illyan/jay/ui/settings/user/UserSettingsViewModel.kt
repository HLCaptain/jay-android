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

package illyan.jay.ui.settings.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import illyan.jay.domain.model.Theme
import illyan.jay.ui.settings.user.model.UiPreferences
import illyan.jay.ui.settings.user.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val authInteractor: AuthInteractor
) : ViewModel() {

    private val _showAnalyticsRequestDialog = MutableStateFlow(false)
    val showAnalyticsRequestDialog = _showAnalyticsRequestDialog.asStateFlow()

    val preferences = combine(
        settingsInteractor.userPreferences,
        settingsInteractor.appSettings
    ) { preferences, appSettings ->
        val uiPreferences = preferences?.toUiModel(clientUUID = appSettings.clientUUID)
        updateAnalyticsRequestDialogVisibility(uiPreferences)
        uiPreferences
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val arePreferencesSynced = settingsInteractor.arePreferencesSynced
    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val shouldSyncPreferences = settingsInteractor.shouldSyncPreferences
    val canSyncPreferences = settingsInteractor.canSyncPreferences

    private fun updateAnalyticsRequestDialogVisibility(uiPreferences: UiPreferences?) {
        _showAnalyticsRequestDialog.update {
            // If analytics has not yet been updated, show dialog,
            // or it was turned off a while ago, show the dialog again
            val shouldShowAnalyticsRequest = uiPreferences?.let {
                if (it.lastUpdateToAnalytics == null) return@let true
                val thresholdTime = it.lastUpdateToAnalytics.plusDays(DaysToWaitForRequest)
                val isAnalyticsSetLongTimeAgo = thresholdTime < ZonedDateTime.now()
                isAnalyticsSetLongTimeAgo && !it.analyticsEnabled
            } ?: true
            Timber.v("Should show Analytics Request on User Settings Screen? $shouldShowAnalyticsRequest")
            shouldShowAnalyticsRequest
        }
    }

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

    fun setTheme(theme: Theme) {
        settingsInteractor.theme = theme
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        settingsInteractor.dynamicColorEnabled = enabled
    }

    companion object {
        const val DaysToWaitForRequest = 30L // days
    }
}