package illyan.jay.util

import android.os.SystemClock
import java.time.Instant

fun sensorTimestampToAbsoluteTime(timestamp: Long): Long {
    return Instant.now()
        .toEpochMilli() - (SystemClock.elapsedRealtimeNanos() - timestamp) / 1000000L
}