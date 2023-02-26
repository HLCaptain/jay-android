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

package illyan.jay.data.network.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import illyan.jay.data.network.model.FirestoreUser
import illyan.jay.data.network.toDomainModel
import illyan.jay.data.network.toFirestoreModel
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

class PreferencesNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val userNetworkDataSource: UserNetworkDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val isLoading = userNetworkDataSource.isLoading
    val isLoadingFromCloud = userNetworkDataSource.isLoadingFromCloud

    val preferences: StateFlow<DomainPreferences?> by lazy {
        combine(
            userNetworkDataSource.user,
            userNetworkDataSource.isLoading
        ) { user, loading ->
            if (user?.preferences != null) {
                val preferences = user.preferences.toDomainModel(userUUID = user.uuid)
                Timber.d("Firebase got preferences for user ${user.uuid.take(4)}")
                preferences
            } else if (loading) {
                null
            } else {
                null
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    val cloudPreferences: StateFlow<DomainPreferences?> by lazy {
        combine(
            userNetworkDataSource.cloudUser,
            userNetworkDataSource.isLoadingFromCloud
        ) { user, loading ->
            if (user?.preferences != null) {
                val preferences = user.preferences.toDomainModel(userUUID = user.uuid)
                Timber.d("Firebase got cloud preferences for user ${user.uuid.take(4)}")
                preferences
            } else if (loading) {
                null
            } else {
                null
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    fun setPreferences(
        preferences: DomainPreferences,
        batch: WriteBatch,
    ) {
        if (!authInteractor.isUserSignedIn) return
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(authInteractor.userUUID!!)
        val fieldMapToSet = mapOf(FirestoreUser.FieldPreferences to preferences.toFirestoreModel())
        batch.set(
            userRef,
            fieldMapToSet,
            SetOptions.merge()
        )
    }

    fun setPreferences(
        preferences: DomainPreferences,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting user preferences: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Inserting user preferences canceled") },
        onSuccess: (DomainPreferences) -> Unit = { Timber.i("Inserting user preferences successful") },
    ) {
        firestore.runBatch {
            setPreferences(preferences, it)
        }.addOnSuccessListener {
            onSuccess(preferences)
        }.addOnFailureListener {
            onFailure(it)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun resetPreferences(
        batch: WriteBatch
    ) = setPreferences(DomainPreferences(userUUID = authInteractor.userUUID), batch)

    fun resetPreferences(
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while resetting user preferences: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Resetting user preferences canceled") },
        onSuccess: (DomainPreferences) -> Unit = { Timber.i("Resetting user preferences canceled") },
    ) = setPreferences(
        preferences = DomainPreferences(userUUID = authInteractor.userUUID),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess,
    )
}