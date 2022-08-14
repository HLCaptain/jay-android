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
import illyan.jay.ui.toggle.service.Initial
import illyan.jay.ui.toggle.service.Loading
import illyan.jay.ui.toggle.service.Off
import illyan.jay.ui.toggle.service.On
import illyan.jay.ui.toggle.service.ServiceTogglePresenter
import illyan.jay.ui.toggle.service.ServiceToggleViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ServiceToggleViewModelTest : ViewModelTest() {
    private lateinit var mockPresenter: ServiceTogglePresenter
    private lateinit var viewModel: ServiceToggleViewModel

    @Before
    fun initEach() {
        mockPresenter = mockk()
        viewModel = ServiceToggleViewModel(mockPresenter)
    }

    @Test
    fun `Load sets state to on when service is running`() {
        every { mockPresenter.isJayServiceRunning() } returns true

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, Loading, On)
        }

        verify(exactly = 1) { mockPresenter.isJayServiceRunning() }
    }

    @Test
    fun `Load sets state to off when service is not running`() {
        every { mockPresenter.isJayServiceRunning() } returns false

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, Loading, Off)
        }

        verify(exactly = 1) { mockPresenter.isJayServiceRunning() }
    }

    @Test
    fun `Load changes state to on when service starts running`() {
        every { mockPresenter.isJayServiceRunning() } returns false
        every { mockPresenter.addJayServiceStateListener(any()) } answers {
            firstArg<(Boolean, String?) -> Unit>().invoke(true, "")
        }

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, Loading, Off, On)
        }

        verify(exactly = 1) { mockPresenter.isJayServiceRunning() }
        verify(exactly = 1) { mockPresenter.addJayServiceStateListener(any()) }

        verifySequence {
            mockPresenter.isJayServiceRunning()
            mockPresenter.addJayServiceStateListener(any())
        }
    }

    @Test
    fun `Load changes state to off when service stops running`() {
        every { mockPresenter.isJayServiceRunning() } returns true
        every { mockPresenter.addJayServiceStateListener(any()) } answers {
            firstArg<(Boolean, String) -> Unit>().invoke(false, "")
        }

        viewModel.observeStateAndEvents { stateObserver, _ ->
            viewModel.load()
            stateObserver.assertObserved(Initial, Loading, On, Off)
        }

        verify(exactly = 1) { mockPresenter.isJayServiceRunning() }
        verify(exactly = 1) { mockPresenter.addJayServiceStateListener(any()) }

        verifySequence {
            mockPresenter.isJayServiceRunning()
            mockPresenter.addJayServiceStateListener(any())
        }
    }

    @Test
    fun `Toggle service changes state to on if it was off`() {
        every { mockPresenter.isJayServiceRunning() } returns false

        viewModel.toggleService()

        verify(exactly = 1) { mockPresenter.startService() }
    }

    @Test
    fun `Toggle service changes state to off if it was on`() {
        every { mockPresenter.isJayServiceRunning() } returns true

        viewModel.toggleService()

        verify(exactly = 1) { mockPresenter.stopService() }
    }
}
