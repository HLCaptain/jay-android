package illyan.jay.view_model

import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import illyan.jay.ui.sessions.list.*
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
class SessionsViewModelTest: ViewModelTest() {
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