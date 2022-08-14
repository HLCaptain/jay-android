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

package illyan.jay.view_model

import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import illyan.jay.ui.sessions.session_info.Initial
import illyan.jay.ui.sessions.session_info.Loading
import illyan.jay.ui.sessions.session_info.NotFound
import illyan.jay.ui.sessions.session_info.Ready
import illyan.jay.ui.sessions.session_info.SessionInfoPresenter
import illyan.jay.ui.sessions.session_info.SessionInfoViewModel
import illyan.jay.ui.sessions.session_info.model.UiSession
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SessionInfoViewModelTest : ViewModelTest() {
    private lateinit var mockPresenter: SessionInfoPresenter
    private lateinit var viewModel: SessionInfoViewModel

    @Before
    fun initEach() {
        mockPresenter = mockk()
        viewModel = SessionInfoViewModel(mockPresenter)
    }

    @Test
    fun `Load session loads session if it exists`() = runTest {
        val mockSession: UiSession = mockk()

        every { mockPresenter.getSession(any()) } returns flowOf(mockSession)

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.loadSession(0)
            advanceUntilIdle()
            stateObserver.assertObserved(Initial, Loading, Ready(mockSession))
        }

        verify(exactly = 1) { mockPresenter.getSession(any()) }
    }

    @Test
    fun `Load session does not load session if it does not exist`() = runTest {
        every { mockPresenter.getSession(any()) } returns flowOf(null)

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.loadSession(0)
            advanceUntilIdle()
            stateObserver.assertObserved(Initial, Loading, NotFound)
        }

        verify(exactly = 1) { mockPresenter.getSession(any()) }
    }
}
