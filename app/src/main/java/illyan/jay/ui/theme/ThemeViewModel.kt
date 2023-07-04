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

package illyan.jay.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.data.datastore.datasource.AppSettingsDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val appSettingsDataSource: AppSettingsDataSource
) : ViewModel() {
    val theme = appSettingsDataSource.appSettings.map { it.theme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun toggleTheme() {
        viewModelScope.launch {
            appSettingsDataSource.updateAppSettings {
                it.copy(theme = when (it.theme) {
                    Theme.System -> Theme.Light
                    Theme.Light -> Theme.Dark
                    Theme.Dark -> Theme.System
                })
            }
        }
    }
}