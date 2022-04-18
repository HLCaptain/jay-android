package illyan.jay.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import timber.log.Timber
import javax.inject.Inject

class ServiceStateReceiver @Inject constructor(

) : BroadcastReceiver() {

    // TODO: call invoke on the listeners for every state change, receive broadcasted messages from service
    val serviceStateListeners = mutableListOf<(Boolean, String) -> Unit>()

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when(it.action) {
                // When the service changed states
                BaseService.KEY_SERVICE_STATE_CHANGE -> {
                    val name = it.getStringExtra(BaseService.KEY_SERVICE_NAME).toString()
                    val state = it.getStringExtra(BaseService.KEY_SERVICE_STATE_CHANGE)
                    Timber.i("Service $name state = $state")
                    when(state) {
                        BaseService.SERVICE_RUNNING -> {
                            serviceStateListeners.forEach { listener -> listener.invoke(true, name) }
                        }
                        BaseService.SERVICE_STOPPED -> {
                            serviceStateListeners.forEach { listener -> listener.invoke(false, name) }
                        }
                    }
                }
            }
        }
    }
}