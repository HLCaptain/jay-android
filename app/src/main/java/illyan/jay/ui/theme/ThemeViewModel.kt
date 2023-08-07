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

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zotov.phototime.solarized.Solarized
import illyan.jay.domain.interactor.SensorInteractor
import illyan.jay.domain.interactor.SettingsInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    settingsInteractor: SettingsInteractor,
    private val sensorInteractor: SensorInteractor,
) : ViewModel() {
    val theme = settingsInteractor.userPreferences.map { it?.theme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val dynamicColorEnabled = settingsInteractor.userPreferences
        .map { it?.dynamicColorEnabled == true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val currentLocation = MutableStateFlow<Location?>(null)

    val isNight = currentLocation.map { location ->
        location?.let {
            val solarized = Solarized(
                it.latitude,
                it.longitude,
                LocalDateTime.now(),
                TimeZone.getDefault()
            )
            val now = LocalDateTime.now()
            now.isBefore(solarized.day?.start) || now.isAfter(solarized.day?.end)
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation.update { locationResult.lastLocation }
        }
    }

    init {
        sensorInteractor.requestLocationUpdates(locationCallback)
    }

    override fun onCleared() {
        super.onCleared()
        sensorInteractor.removeLocationUpdates(locationCallback)
    }
}
