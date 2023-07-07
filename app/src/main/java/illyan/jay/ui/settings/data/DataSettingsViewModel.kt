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

package illyan.jay.ui.settings.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.di.CoroutineDispatcherIO
import illyan.jay.domain.interactor.AuthInteractor
import illyan.jay.domain.interactor.UserInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataSettingsViewModel @Inject constructor(
    private val userInteractor: UserInteractor,
    authInteractor: AuthInteractor,
    @CoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val userPhotoUrl = authInteractor.userPhotoUrlStateFlow
    val cachedDataSizeInBytes = userInteractor.cachedUserDataSizeInBytes
    val isUserSignedIn = authInteractor.isUserSignedInStateFlow

    fun deleteSyncedUserData() {
        viewModelScope.launch(dispatcherIO) {
            userInteractor.deleteAllSyncedData()
        }
    }

    fun deletePublicData() {
        viewModelScope.launch(dispatcherIO) {
            userInteractor.deleteAllPublicData()
        }
    }

    fun deleteAllUserData() {
        viewModelScope.launch(dispatcherIO) {
            userInteractor.deleteAllUserData()
        }
    }

    fun deleteCachedUserData() {
        viewModelScope.launch(dispatcherIO) {
            userInteractor.deleteAllLocalData()
        }
    }
}
