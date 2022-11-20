/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.domain.interactor.AuthInteractor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authInteractor: AuthInteractor
): ViewModel() {
    val isUserSignedIn = authInteractor.isUserSignedInStateFlow
    val isUserSigningOut = authInteractor.isUserSigningOut
    val userPhotoUrl = authInteractor.userPhotoUrlStateFlow

    val userEmail = authInteractor.currentUserStateFlow
        .map { it?.email }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            authInteractor.currentUserStateFlow.value?.email
        )

    val userPhoneNumber = authInteractor.currentUserStateFlow
        .map { it?.phoneNumber }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            authInteractor.currentUserStateFlow.value?.phoneNumber
        )

    val userName = authInteractor.currentUserStateFlow
        .map { it?.displayName }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            authInteractor.currentUserStateFlow.value?.displayName
        )

    fun signOut() = authInteractor.signOut()
}