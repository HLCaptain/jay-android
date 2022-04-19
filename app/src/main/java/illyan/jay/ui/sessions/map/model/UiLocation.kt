package illyan.jay.ui.sessions.map.model

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import java.util.*

data class UiLocation(
	val id: Long = -1,
	val latLng: LatLng,
	val sessionId: Long,
	val time: Date,
	val color: Color
)
