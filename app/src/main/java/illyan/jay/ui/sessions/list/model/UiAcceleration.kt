package illyan.jay.ui.sessions.list.model

import java.util.*


data class UiAcceleration(
    val id: Int = -1,
    val sessionId: Int,
    val time: Date,
    val accuracy: Int, // enum
    val x: Float,
    val y: Float,
    val z: Float
)
