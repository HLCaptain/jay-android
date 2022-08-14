/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth interactor is an abstraction layer between higher level logic
 * and lower level implementation.
 * Based on Firebase's Authentication system.
 *
 * @constructor Create empty Auth interactor
 */
@Singleton
class AuthInteractor @Inject constructor() {
    /**
     * Gets if the user is logged in or not.
     *
     * @return true if the current user is not null, otherwise returns false.
     */
    fun isUserLoggedIn(): Boolean = Firebase.auth.currentUser != null

    /**
     * Add authentication state listener
     *
     * @param listener listener to add to state changes.
     * @receiver receives a copy of current FirebaseAuth object as a state.
     */
    fun addAuthStateListener(listener: (FirebaseAuth) -> Unit) {
        Firebase.auth.addAuthStateListener {
            listener(it)
        }
    }
}
