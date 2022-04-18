package illyan.jay.ui.realtime_map

import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.data.disk.toDomainModel
import illyan.jay.domain.interactor.SensorInteractor
import javax.inject.Inject

@HiltViewModel
class RealtimeMapViewModel @Inject constructor(
	private val realtimeMapPresenter: RealtimeMapPresenter
) : RainbowCakeViewModel<RealtimeMapViewState>(Initial) {

	fun load() = execute {
		viewState = Loading
		realtimeMapPresenter.setLocationListener {
			viewState = Ready(it)
		}
	}

	fun unload() = execute {
		realtimeMapPresenter.stopListening()
	}
}