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

package illyan.jay.data.firestore.datasource

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import illyan.jay.data.firestore.model.FirestoreSensorEvents
import illyan.jay.data.firestore.toDomainSensorEvents
import illyan.jay.di.UserPathsSnapshotHandler
import illyan.jay.domain.model.DomainSensorEvent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class UserSensorEventsFirestoreDataFlow @Inject constructor(
    firestore: FirebaseFirestore,
    appLifecycle: Lifecycle,
    coroutineScopeIO: CoroutineScope,
    @UserPathsSnapshotHandler snapshotHandler: FirestoreSnapshotHandler<List<FirestoreSensorEvents>, QuerySnapshot>,
) : FirestoreDataFlow<List<FirestoreSensorEvents>, List<List<DomainSensorEvent>>>(
    firestore = firestore,
    appLifecycle = appLifecycle,
    coroutineScopeIO = coroutineScopeIO,
    toDomainModel = { events ->
        events
            ?.groupBy { it.sessionUUID }
            ?.map { it.value.toDomainSensorEvents() }
    },
    snapshotHandler = snapshotHandler,
)