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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import illyan.jay.data.DataStatus
import illyan.jay.data.firestore.model.FirestoreUser
import illyan.jay.di.CoroutineScopeIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.util.runBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class UserNetworkDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authInteractor: AuthInteractor,
    private val appLifecycle: Lifecycle,
    @CoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) : DefaultLifecycleObserver {

    private val _userListenerJob = MutableStateFlow<Job?>(null)
    private val _userReference = MutableStateFlow<DocumentSnapshot?>(null)
    private val _userStatus = MutableStateFlow(DataStatus<FirestoreUser>())
    private val _cloudUserStatus = MutableStateFlow(DataStatus<FirestoreUser>())

    val userStatus: StateFlow<DataStatus<FirestoreUser>> by lazy {
        if (_userListenerJob.value == null && _userStatus.value.isLoading != true) {
            Timber.d("User StateFlow requested, but listener registration is null, reloading it")
            refreshUser()
        }
        _userStatus.asStateFlow()
    }

    val user = userStatus.map {
        it.data
    }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, userStatus.value.data)

    val cloudUserStatus: StateFlow<DataStatus<FirestoreUser>> by lazy {
        if (_userListenerJob.value == null && _cloudUserStatus.value.isLoading != true) {
            Timber.d("User StateFlow requested, but listener registration is null, reloading it")
            refreshUser()
        }
        _cloudUserStatus.asStateFlow()
    }

    val cloudUser = cloudUserStatus.map {
        it.data
    }.stateIn(coroutineScopeIO, SharingStarted.Eagerly, cloudUserStatus.value.data)

    init {
        authInteractor.addAuthStateListener { state ->
            if (authInteractor.isUserSignedIn) {
                if (authInteractor.userUUID != null &&
                    authInteractor.userUUID != _userStatus.value.data?.uuid
                ) {
                    Timber.d("Reloading snapshot listener for user ${state.currentUser?.uid?.take(4)}")
                    resetUserListenerData()
                    refreshUser()
                } else {
                    Timber.d("User not changed from ${_userStatus.value.data?.uuid?.take(4)}, not reloading snapshot listener on auth state change")
                }
            } else {
                Timber.d("Removing snapshot listener for user ${_userStatus.value.data?.uuid?.take(4)}")
                resetUserListenerData()
            }
        }
        appLifecycle.addObserver(this)
    }

    private fun resetUserListenerData() {
        Timber.v("Resetting user listener state")
        _userListenerJob.value?.cancel(CancellationException("User listener reset requested, cancelling ongoing job"))
        if (_userListenerJob.value != null) _userListenerJob.update { null }
        if (_userReference.value != null) _userReference.update { null }
        _userStatus.update { DataStatus() }
        _cloudUserStatus.update { DataStatus() }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("Reload user on App Lifecycle Start")
        resetUserListenerData()
        refreshUser()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.d("Remove user listener on App Lifecycle Stop")
        resetUserListenerData()
    }

    private fun refreshUser(
        userUUID: String = authInteractor.userUUID.toString(),
        onSuccess: (FirestoreUser?) -> Unit = {},
    ) {
        Timber.v("Refreshing user ${userUUID.take(4)} requested")
        if (!authInteractor.isUserSignedIn || _cloudUserStatus.value.isLoading == true) {
            Timber.d("Not refreshing user, due to another being loaded in or user is not signed in")
            return
        }
        resetUserListenerData()
        _userStatus.update { it.copy(isLoading = true) }
        _cloudUserStatus.update { it.copy(isLoading = true) }
        Timber.d("Connecting snapshot listener to Firebase to get ${userUUID.take(4)} user's data")
        _userListenerJob.value?.cancel(CancellationException("Refreshing user, launching new job to handle updates, old job is cancelled"))
        _userListenerJob.update {
            coroutineScopeIO.launch {
                firestore
                    .collection(FirestoreUser.CollectionName)
                    .document(userUUID)
                    .snapshots(MetadataChanges.INCLUDE).collectLatest { snapshot ->
                        Timber.v("New snapshot regarding user ${userUUID.take(4)}")
                        val user = snapshot.toObject<FirestoreUser>()
                        onSuccess(user)
                        // Update _userReference value with snapshot when snapshot is not null
                        _userReference.update { snapshot }
                        processUser(
                            user = user,
                            isFromCache = snapshot.metadata.isFromCache,
                            userUUID = userUUID,
                            updateCachedUserStatus = { status -> _userStatus.update { status } },
                            updateCloudUserStatus = { status -> _cloudUserStatus.update { status } },
                        )
                    }
            }
        }
    }

    private fun processUser(
        user: FirestoreUser?,
        isFromCache: Boolean,
        userUUID: String,
        updateCachedUserStatus: (DataStatus<FirestoreUser>) -> Unit,
        updateCloudUserStatus: (DataStatus<FirestoreUser>) -> Unit,
    ) {
        // Cache
        if (user != null) {
            if (_userStatus.value.data != null) {
                Timber.v("Refreshing Cached ${userUUID.take(4)} user's data")
            } else {
                Timber.d("Firestore loaded ${userUUID.take(4)} user's data from Cache")
            }
            updateCachedUserStatus(DataStatus(data = user, isLoading = false))
        } else {
            updateCachedUserStatus(DataStatus(data = null, isLoading = false))
        }

        // Cloud
        if (!isFromCache) {
            if (user != null) {
                if (_cloudUserStatus.value.data != null) {
                    Timber.v("Firestore loaded fresh ${userUUID.take(4)} user's data from Cloud")
                } else {
                    Timber.d("Firestore loaded ${userUUID.take(4)} user's data from Cloud")
                }
                updateCloudUserStatus(DataStatus(data = user, isLoading = false))
            } else {
                updateCloudUserStatus(DataStatus(data = null, isLoading = false))
            }
        }
    }

    private fun processUser(user: FirestoreUser) {

    }

    suspend fun deleteUserData(
        onCancel: () -> Unit = { Timber.i("User data deletion canceled") },
        onFailure: (Exception) -> Unit = { Timber.e(it) },
        onSuccess: () -> Unit = { Timber.i("User data deletion successful") },
    ) {
        firestore.runBatch(1) { batch, onOperationFinished ->
            deleteUserData(
                batch = batch,
                onWriteFinished = onOperationFinished
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it)
        }.addOnCanceledListener {
            onCancel()
        }
    }

    suspend fun deleteUserData(
        batch: WriteBatch,
        onWriteFinished: () -> Unit = {},
    ) {
        _userReference.first { snapshot ->
            snapshot?.let { batch.delete(it.reference) }
            onWriteFinished()
            true
        }
    }
}
