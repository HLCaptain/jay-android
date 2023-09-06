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

package illyan.jay.di

import androidx.lifecycle.Lifecycle
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import illyan.jay.data.firestore.datasource.FirestoreDocumentSnapshotHandler
import illyan.jay.data.firestore.datasource.FirestoreQuerySnapshotHandler
import illyan.jay.data.firestore.datasource.FirestoreSnapshotHandler
import illyan.jay.data.firestore.datasource.UserPathsFirestoreDataFlow
import illyan.jay.data.firestore.datasource.UserSensorEventsFirestoreDataFlow
import illyan.jay.data.firestore.model.FirestorePath
import illyan.jay.data.firestore.model.FirestoreSensorEvents
import illyan.jay.data.firestore.model.FirestoreUser
import illyan.jay.domain.interactor.AuthInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @UserSnapshotHandler
    fun provideFirestoreUserSnapshotHandler(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
    ): FirestoreSnapshotHandler<FirestoreUser, DocumentSnapshot> {
        return FirestoreDocumentSnapshotHandler(
            snapshotToObject = { it.toObject() },
            snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                if (uuid != null) {
                    firestore
                        .collection(FirestoreUser.CollectionName)
                        .document(uuid)
                        .snapshots(MetadataChanges.INCLUDE)
                } else {
                    null
                }
            },
            initialReference = authInteractor.userUUID?.let { uuid ->
                firestore
                    .collection(FirestoreUser.CollectionName)
                    .document(uuid)
                }
        )
    }

    @Provides
    @UserPathsSnapshotHandler
    fun provideFirestorePathSnapshotHandler(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
    ): FirestoreSnapshotHandler<List<FirestorePath>, QuerySnapshot> {
        return FirestoreQuerySnapshotHandler(
            snapshotToObject = { it.toObjects() },
            snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                if (uuid != null) {
                    firestore
                        .collection(FirestorePath.CollectionName)
                        .whereEqualTo(FirestorePath.FieldOwnerUUID, uuid)
                        .snapshots(MetadataChanges.INCLUDE)
                } else {
                    null
                }
            }
        )
    }

    @Provides
    @UserSensorEventsSnapshotHandler
    fun provideFirestoreSensorEventsSnapshotHandler(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
    ): FirestoreSnapshotHandler<List<FirestoreSensorEvents>, QuerySnapshot> {
        return FirestoreQuerySnapshotHandler(
            snapshotToObject = { it.toObjects() },
            snapshotSourceFlow = authInteractor.userUUIDStateFlow.map { uuid ->
                if (uuid != null) {
                    firestore
                        .collection(FirestoreSensorEvents.CollectionName)
                        .whereEqualTo(FirestoreSensorEvents.FieldOwnerUUID, uuid)
                        .snapshots(MetadataChanges.INCLUDE)
                } else {
                    null
                }
            }
        )
    }

    @Provides
    fun provideUserPathsFirestoreDataFlow(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
        appLifecycle: Lifecycle,
        @CoroutineScopeIO coroutineScopeIO: CoroutineScope
    ): () -> UserPathsFirestoreDataFlow = {
        UserPathsFirestoreDataFlow(
            firestore = firestore,
            appLifecycle = appLifecycle,
            coroutineScopeIO = coroutineScopeIO,
            snapshotHandler = provideFirestorePathSnapshotHandler(
                firestore = firestore,
                authInteractor = authInteractor
            )
        )
    }

    @Provides
    fun provideUserSensorEventsFirestoreDataFlow(
        firestore: FirebaseFirestore,
        authInteractor: AuthInteractor,
        appLifecycle: Lifecycle,
        @CoroutineScopeIO coroutineScopeIO: CoroutineScope
    ): () -> UserSensorEventsFirestoreDataFlow = {
        UserSensorEventsFirestoreDataFlow(
            firestore = firestore,
            appLifecycle = appLifecycle,
            coroutineScopeIO = coroutineScopeIO,
            snapshotHandler = provideFirestoreSensorEventsSnapshotHandler(
                firestore = firestore,
                authInteractor = authInteractor
            )
        )
    }
}