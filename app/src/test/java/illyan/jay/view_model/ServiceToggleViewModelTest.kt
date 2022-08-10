package illyan.jay.view_model

import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import illyan.jay.ui.toggle.service.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ServiceToggleViewModelTest: ViewModelTest() {
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