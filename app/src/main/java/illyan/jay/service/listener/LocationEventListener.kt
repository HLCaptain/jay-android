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

package illyan.jay.service.listener

import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import illyan.jay.data.disk.toDomainModel
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Location event listener .
 * On registration, it becomes active and saves data
 * via LocationInteractor and SessionInteractor.
 *
 * @property locationInteractor saves data onto this interactor.
 * @property sessionInteractor using the session interactor to properly save
 * location data for each individual session.
 * @constructor Create empty Location event listener
 */
class LocationEventListener @Inject constructor(
    private val locationInteractor: LocationInteractor,
    private val sessionInteractor: SessionInteractor
) : SessionSensorEventListener(sessionInteractor) {

    /**
     * Used to be registered to get location updates.
     * Saves location data for every ongoing session.
     * Property can be set for testing purposes (ie. logging)
     * and still save the data via SessionInteractor and LocationInteractor.
     */
    var locationCallback: LocationCallback = object : LocationCallback() {}
        set(value) {
            field = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    scope.launch {
                        // Saving locations for every ongoing session
                        val locations = mutableListOf<DomainLocation>()
                        ongoingSessionUUIDs.forEach { sessionUUID ->
                            locationResult.lastLocation?.let { lastLocation ->
                                locations += lastLocation.toDomainModel(sessionUUID)
                            }
                        }
                        locationInteractor.saveLocations(locations)
                    }
                    value.onLocationResult(locationResult)
                }

                override fun onLocationAvailability(p0: LocationAvailability) {
                    super.onLocationAvailability(p0)
                    value.onLocationAvailability(p0)
                }
            }
        }

    /**
     * Default location request for use to register updates on LocationCallback.
     */
    var locationRequest = LocationRequest.create()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setInterval(LocationInteractor.LOCATION_REQUEST_INTERVAL_FREQUENT)
        .setSmallestDisplacement(LocationInteractor.LOCATION_REQUEST_DISPLACEMENT_DEFAULT)

    init {
        locationCallback = object : LocationCallback() {}
    }
}
