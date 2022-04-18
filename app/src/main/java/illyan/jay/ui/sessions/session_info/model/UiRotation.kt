package illyan.jay.ui.sessions.session_info.model

import java.util.*

data class UiRotation(
    val id: Int = -1,
    val sessionId: Int,
    val time: Date,
    val accuracy: Int, // enum
    val x: Float,
    val y: Float,
    val z: Float
)