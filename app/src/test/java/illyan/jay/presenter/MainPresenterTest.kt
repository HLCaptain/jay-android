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

package illyan.jay.presenter

import illyan.jay.MainPresenter
import illyan.jay.TestBase
import illyan.jay.domain.interactor.AuthInteractor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MainPresenterTest : TestBase() {

    private lateinit var mockedAuthInteractor: AuthInteractor
    private lateinit var mainPresenter: MainPresenter

    @BeforeEach
    fun initEach() {
        mockedAuthInteractor = mockk(relaxed = true)
        mainPresenter = MainPresenter(mockedAuthInteractor)
    }

    @ParameterizedTest(name = "User logged in? {0}")
    @ValueSource(booleans = [true, false])
    fun `User is logged in or not`(isUserLoggedIn: Boolean) {
        every { mockedAuthInteractor.isUserLoggedIn() } returns isUserLoggedIn

        val result = mainPresenter.isUserLoggedIn()

        assertEquals(isUserLoggedIn, result)
        verify(exactly = 1) { mockedAuthInteractor.isUserLoggedIn() }
    }

    @Test
    fun `Add authentication state listener`() {
        val stateListener: (Boolean) -> Unit = { _ -> }

        mainPresenter.addAuthStateListener(stateListener)

        verify(exactly = 1) { mockedAuthInteractor.addAuthStateListener(any()) }
    }
}
