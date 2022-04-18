package illyan.jay.ui.toggle.service

import android.content.Context
import co.zsmb.rainbowcake.withIOContext
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ActivityContext
import illyan.jay.domain.interactor.ServiceInteractor
import javax.inject.Inject

class ServiceTogglePresenter @Inject constructor(
    private val serviceInteractor: ServiceInteractor
) {
    fun startService() = serviceInteractor.startJayService()
    fun stopService() = serviceInteractor.stopJayService()
    fun isJayServiceRunning() = serviceInteractor.isJayServiceRunning()
    fun addJayServiceStateListener(listener: (isRunning: Boolean, name: String) -> Unit) {
        serviceInteractor.addServiceStateListener(listener)
    }
}