package illyan.jay.view_model

import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import illyan.jay.ui.sessions.session_info.*
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
class SessionInfoViewModelTest: ViewModelTest() {
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