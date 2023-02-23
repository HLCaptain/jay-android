package illyan.jay.data.disk.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(
    tableName = "preferences",
)
data class RoomPreferences(
    @PrimaryKey
    val userUUID: String = UUID.randomUUID().toString(),
    val freeDriveAutoStart: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val lastUpdate: Long = ZonedDateTime.now().toInstant().toEpochMilli()
)
