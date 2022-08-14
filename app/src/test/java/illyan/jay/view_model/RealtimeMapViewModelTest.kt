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
import illyan.jay.ui.realtime_map.Initial
import illyan.jay.ui.realtime_map.Loading
import illyan.jay.ui.realtime_map.Ready
import illyan.jay.ui.realtime_map.RealtimeMapPresenter
import illyan.jay.ui.realtime_map.RealtimeMapViewModel
import illyan.jay.ui.realtime_map.model.UiLocation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RealtimeMapViewModelTest : ViewModelTest() {
    private lateinit var mockPresenter: RealtimeMapPresenter
    private lateinit var viewModel: RealtimeMapViewModel

    @Before
    fun initEach() {
        mockPresenter = mockk()
        viewModel = RealtimeMapViewModel(mockPresenter)
    }

    @Test
    fun `Load adds location listener which sets location when invoked`() {
        val mockLocation: UiLocation = mockk()

        every { mockPresenter.setLocationListener(any()) } answers {
            firstArg<(UiLocation) -> Unit>().invoke(mockLocation)
        }

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, Loading, Ready(mockLocation))
        }

        verify(exactly = 1) { mockPresenter.setLocationListener(any()) }
    }

    @Test
    fun `Unload stops listening`() {
        viewModel.unload()

        verify(exactly = 1) { mockPresenter.stopListening() }
    }
}
