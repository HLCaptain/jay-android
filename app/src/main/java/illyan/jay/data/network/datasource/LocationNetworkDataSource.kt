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

package illyan.jay.data.network.datasource

import com.google.firebase.firestore.FirebaseFirestore
import illyan.jay.data.network.toDomainLocations
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainLocation
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor
) {
    fun getLocations(
        sessionUUID: String,
        listener: (List<DomainLocation>) -> Unit
    ) {
        if (authInteractor.isUserSignedIn) {
            firestore
                .collection(SessionNetworkDataSource.PathsCollectionPath)
                .whereEqualTo(
                    "sessionUUID",
                    sessionUUID
                )
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.d("Error while getting path for session $sessionUUID: ${error.message}")
                    } else {
                        listener(snapshot!!.documents.toDomainLocations())
                    }
                }
        } else {
            listener(emptyList())
        }
    }
}