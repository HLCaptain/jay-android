package illyan.jay.ui.sessions.list.model

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class UiLocation(
	val id: Long = -1,
	val latLng: LatLng,
	val speed: Float,
	val sessionId: Long,
	val time: Date,
	val accuracy: Float,
	val bearing: Float,
	val bearingAccuracy: Float, // in degrees
	val altitude: Double,
	val speedAccuracy: Float, // in meters per second
	val verticalAccuracy: Float // in meters
)
