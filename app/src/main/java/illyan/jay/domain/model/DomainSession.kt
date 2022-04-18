package illyan.jay.domain.model

import java.util.*

data class DomainSession(
    val id: Int = -1,
    val startTime: Date,
    var endTime: Date?,
    var distance: Double = 0.0
)
