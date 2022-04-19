package illyan.jay.domain.model

import java.util.*


data class DomainAcceleration(
	val id: Long = -1,
	val sessionId: Long,
	val time: Date,
	val accuracy: Int, // enum
	val x: Float,
	val y: Float,
	val z: Float
)
