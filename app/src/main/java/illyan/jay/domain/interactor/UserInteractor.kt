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

package illyan.jay.domain.interactor

import com.google.firebase.firestore.FirebaseFirestore
import illyan.jay.data.firestore.datasource.UserFirestoreDataFlow
import illyan.jay.util.runBatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInteractor @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val userFirestoreDataFlow: UserFirestoreDataFlow,
    private val sessionInteractor: SessionInteractor,
    private val firestore: FirebaseFirestore,
) {
    // TODO: estimate cache size in the future
    val _cachedUserDataSizeInBytes = MutableStateFlow<Long?>(null)
    val cachedUserDataSizeInBytes = _cachedUserDataSizeInBytes.asStateFlow()

    suspend fun deleteAllSyncedData() {
        Timber.v("Deleting synced data requested")
        if (!authInteractor.isUserSignedIn) {
            Timber.e(IllegalStateException("Deleting synced data failed due to user not signed in"))
        } else {
            // Turn off sync, so data won't automatically upload to the cloud when deleted
            settingsInteractor.shouldSync = false
            // 1. delete turn sync preferences off to stop syncing with cloud data
            // 2. delete session data (also locations) from cloud
            // 3. delete other user data, including preferences from cloud
            firestore.runBatch(numberOfOperations = 2) { batch, onOperationFinished ->
                sessionInteractor.deleteAllSyncedData(
                    batch = batch,
                    onWriteFinished = onOperationFinished
                )
                settingsInteractor.localUserPreferences.first {
                    if (it?.shouldSync == false) {
                        userFirestoreDataFlow.deleteData(
                            batch = batch,
                            onWriteFinished = onOperationFinished
                        )
                    }
                    it?.shouldSync == false
                }
            }
        }
    }

    suspend fun deleteAllLocalData() {
        Timber.v("Deleting local user data requested")
        if (authInteractor.isUserSignedIn) {
            sessionInteractor.deleteOwnedSessions()
        }
    }

    suspend fun deleteAllUserData() {
        // 1. delete all synced data
        // 2. delete all local data
        if (authInteractor.isUserSignedIn) {
            deleteAllSyncedData()
        }
        deleteAllLocalData()
    }

    suspend fun deleteAllPublicData() {
        Timber.v("Deleting local not owned data requested")
        sessionInteractor.deleteNotOwnedSessions()
    }
}