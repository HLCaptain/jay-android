package illyan.jay.ui.sessions.list.model

import java.util.*

data class UiSession(
    val id: Int = -1,
    val startTime: Date,
    var endTime: Date? = null, // in millis
    var distance: Double = 0.0
)
