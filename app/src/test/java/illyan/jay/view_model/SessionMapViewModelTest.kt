package illyan.jay.view_model

import co.zsmb.rainbowcake.test.assertObserved
import co.zsmb.rainbowcake.test.base.ViewModelTest
import co.zsmb.rainbowcake.test.observeStateAndEvents
import com.google.android.gms.maps.model.LatLng
import illyan.jay.ui.sessions.map.*
import illyan.jay.ui.sessions.map.model.UiLocation
import illyan.jay.util.Color
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class SessionMapViewModelTest: ViewModelTest() {
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