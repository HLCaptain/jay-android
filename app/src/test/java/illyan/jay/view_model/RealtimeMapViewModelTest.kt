package illyan.jay.view_model

import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import illyan.jay.ui.realtime_map.*
import illyan.jay.ui.realtime_map.model.UiLocation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RealtimeMapViewModelTest: ViewModelTest() {
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