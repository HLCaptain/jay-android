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
import com.google.maps.android.SphericalUtil
import illyan.jay.data.disk.toDomainModel
import illyan.jay.domain.interactor.LocationInteractor
import illyan.jay.domain.interactor.SessionInteractor
import illyan.jay.domain.model.DomainLocation
import illyan.jay.domain.model.DomainSession
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
                        val sessions = mutableListOf<DomainSession>()
                        ongoingSessionIds.forEach { sessionId ->
                            locationResult.lastLocation?.let { lastLocation ->
                                val newLocation = lastLocation.toDomainModel(sessionId)
                                // Updating distances for each location
                                locationInteractor.getLatestLocations(sessionId, 1)
                                    .flowOn(Dispatchers.IO)
                                    .map { it.firstOrNull() }
                                    .first { location ->
                                        location?.let { lastLocation ->
                                            // Need session to calculate new distance value from the old one
                                            sessionInteractor.getSession(sessionId)
                                                .flowOn(Dispatchers.IO)
                                                .first {
                                                    it?.let { session ->
                                                        // Updating distances
                                                        session.distance += SphericalUtil
                                                            .computeDistanceBetween(
                                                                lastLocation.latLng,
                                                                newLocation.latLng
                                                            )
                                                        // Saving the session with the new distance
                                                        sessions += session
                                                    }
                                                    true
                                                }
                                        }
                                        true
                                    }
                                locations += newLocation
                            }
                        }
                        // Saving data with only one query.
                        locationInteractor.saveLocations(locations)
                        sessionInteractor.saveSessions(sessions)
                    }
                    value.onLocationResult(locationResult)
                }

                override fun onLocationAvailability(p0: LocationAvailability) =
                    value.onLocationAvailability(p0)
            }
        }

    /**
     * Default location request for use to register updates on LocationCallback.
     */
    var locationRequest = LocationRequest
        .create()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setInterval(LocationInteractor.LOCATION_REQUEST_INTERVAL_FREQUENT)
        .setSmallestDisplacement(LocationInteractor.LOCATION_REQUEST_DISPLACEMENT_DEFAULT)

    init {
        locationCallback = object : LocationCallback() {}
    }
}
