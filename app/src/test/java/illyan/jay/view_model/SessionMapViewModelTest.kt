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
import com.google.android.gms.maps.model.LatLng
import illyan.jay.ui.sessions.map.Initial
import illyan.jay.ui.sessions.map.Loading
import illyan.jay.ui.sessions.map.Ready
import illyan.jay.ui.sessions.map.SessionMapPresenter
import illyan.jay.ui.sessions.map.SessionMapViewModel
import illyan.jay.ui.sessions.map.model.UiLocation
import illyan.jay.util.Color
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SessionMapViewModelTest : ViewModelTest() {
    private lateinit var mockPresenter: SessionMapPresenter
    private lateinit var viewModel: SessionMapViewModel

    @Before
    fun initEach() {
        mockPresenter = mockk()
        viewModel = SessionMapViewModel(mockPresenter)
    }

    @Test
    fun `Load path loads existing session`() = runTest {
        val mockLocations = listOf(
            UiLocation(0, LatLng(0.0, 0.0), 0, Date(), Color.RED)
        )

        every { mockPresenter.getLocations(any()) } returns flowOf(mockLocations)

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.loadPath(0)
            advanceUntilIdle()
            stateObserver.assertObserved(Initial, Loading, Ready(mockLocations, true))
        }

        verify(exactly = 1) { mockPresenter.getLocations(0) }
    }

    @Test
    fun `Load path does not load missing session`() = runTest {
        every { mockPresenter.getLocations(any()) } returns flowOf(listOf())

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.loadPath(0)
            advanceUntilIdle()
            stateObserver.assertObserved(Initial, Loading)
        }

        verify(exactly = 1) { mockPresenter.getLocations(0) }
    }
}
