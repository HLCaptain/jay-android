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
import illyan.jay.data.firestore.model.FirestorePath
import illyan.jay.data.firestore.toDomainLocations
import illyan.jay.di.UserPathsSnapshotHandler
import illyan.jay.domain.model.DomainLocation
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPathsFirestoreDataFlow @Inject constructor(
    firestore: FirebaseFirestore,
    appLifecycle: Lifecycle,
    coroutineScopeIO: CoroutineScope,
    @UserPathsSnapshotHandler snapshotHandler: FirestoreSnapshotHandler<List<FirestorePath>, QuerySnapshot>,
) : FirestoreDataFlow<List<FirestorePath>, List<List<DomainLocation>>>(
    firestore = firestore,
    appLifecycle = appLifecycle,
    coroutineScopeIO = coroutineScopeIO,
    toDomainModel = { paths ->
        paths
            ?.groupBy { it.sessionUUID }
            ?.map { it.value.toDomainLocations() }
    },
    snapshotHandler = snapshotHandler,
)