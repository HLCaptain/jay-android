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
import illyan.jay.ui.sessions.list.Initial
import illyan.jay.ui.sessions.list.Loading
import illyan.jay.ui.sessions.list.Ready
import illyan.jay.ui.sessions.list.SessionsPresenter
import illyan.jay.ui.sessions.list.SessionsViewModel
import illyan.jay.ui.sessions.list.model.UiSession
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
class SessionsViewModelTest : ViewModelTest() {
    private lateinit var mockPresenter: SessionsPresenter
    private lateinit var viewModel: SessionsViewModel

    @Before
    fun initEach() {
        mockPresenter = mockk()
        viewModel = SessionsViewModel(mockPresenter)
    }

    @Test
    fun `Load adds session listener which sets session when invoked`() = runTest {
        val mockSessions: List<UiSession> = mockk()

        every { mockPresenter.getSessions() } returns flowOf(mockSessions)

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            advanceUntilIdle()
            stateObserver.assertObserved(Initial, Loading, Ready(mockSessions))
        }

        verify(exactly = 1) { mockPresenter.getSessions() }
    }
}
