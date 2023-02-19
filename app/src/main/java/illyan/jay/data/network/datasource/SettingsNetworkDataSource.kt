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
import illyan.jay.domain.model.DomainUserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

class SettingsNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val userNetworkDataSource: UserNetworkDataSource,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val settings: StateFlow<DomainUserSettings?> by lazy {
        combine(
            userNetworkDataSource.user,
            userNetworkDataSource.isLoading
        ) { user, loading ->
            if (user != null) {
                val settings = user.settings.toDomainModel()
                Timber.d("Firebase got user settings for user ${user.uuid}")
                settings
            } else if (loading) {
                null
            } else {
                // TODO: maybe insert settings
                // possible states:
                // - user not signed in: tell user they cannot sync settings?
                // - user does not have settings yet
                // - no user profile in cloud
                DomainUserSettings()
            }
        }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)
    }

    fun setUserSettings(
        settings: DomainUserSettings,
        batch: WriteBatch,
    ) {
        if (!authInteractor.isUserSignedIn) return
        val userRef = firestore
            .collection(FirestoreUser.CollectionName)
            .document(authInteractor.userUUID!!)
        val fieldMapToSet = mapOf(FirestoreUser.FieldSettings to settings.toFirestoreModel())
        batch.set(
            userRef,
            fieldMapToSet,
            SetOptions.merge()
        )
    }

    fun setUserSettings(
        settings: DomainUserSettings,
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while inserting user settings: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Inserting user settings canceled") },
        onSuccess: (DomainUserSettings) -> Unit = { Timber.i("Inserting user settings successful") },
    ) {
        firestore.runBatch {
            setUserSettings(settings, it)
        }.addOnSuccessListener {
            onSuccess(settings)
        }.addOnFailureListener {
            onFailure(it)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    fun resetUserSettings(
        batch: WriteBatch
    ) = setUserSettings(DomainUserSettings(), batch)

    fun resetUserSettings(
        onFailure: (Exception) -> Unit = { Timber.e(it, "Error while resetting user settings: ${it.message}") },
        onCancel: () -> Unit = { Timber.i("Resetting user settings canceled") },
        onSuccess: (DomainUserSettings) -> Unit = { Timber.i("Resetting user settings canceled") },
    ) = setUserSettings(
        settings = DomainUserSettings(),
        onFailure = onFailure,
        onCancel = onCancel,
        onSuccess = onSuccess,
    )
}