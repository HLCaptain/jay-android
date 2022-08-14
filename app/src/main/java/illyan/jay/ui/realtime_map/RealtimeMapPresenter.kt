/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.realtime_map

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import illyan.jay.domain.interactor.SensorInteractor
import illyan.jay.ui.realtime_map.model.UiLocation
import javax.inject.Inject

class RealtimeMapPresenter @Inject constructor(
    private val sensorInteractor: SensorInteractor
) {

    private val locationRequest = LocationRequest
        .create()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setInterval(200)
        .setSmallestDisplacement(1f)

    private var locationCallback: LocationCallback = object : LocationCallback() {}

    fun setLocationListener(listener: (UiLocation) -> Unit) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.lastLocation?.let { listener.invoke(it.toUiLocation()) }
            }
        }
        sensorInteractor.requestLocationUpdates(
            locationRequest,
            locationCallback
        )
    }

    fun stopListening() = sensorInteractor.removeLocationUpdates(locationCallback)
}

private fun Location.toUiLocation() = UiLocation(
    latLng = LatLng(latitude, longitude)
)
