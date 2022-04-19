package illyan.jay.data.disk.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class RoomSession(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val startTime: Long,
	var endTime: Long? = null,
	var distance: Double = 0.0
)