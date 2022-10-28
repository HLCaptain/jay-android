/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */ // ktlint-disable filename

package illyan.jay.util

import android.os.SystemClock
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import com.mapbox.maps.EdgeInsets
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

/**
 * Sensor timestamp to absolute time.
 * Converts a SensorEvent's timestamp Long value to another Long value which
 * counts from Instant.EPOCH instead of system reboot time.
 *
 * @param timestamp timestamp counted from system reboot converted into
 * counted from Instant.EPOCH.
 * @return timestamp counted from Instant.EPOCH.
 */
fun sensorTimestampToAbsoluteTime(timestamp: Long) = Instant.now()
    .toEpochMilli() - (SystemClock.elapsedRealtimeNanos() - timestamp) / 1.seconds.inWholeMicroseconds

operator fun EdgeInsets.plus(edgeInsets: EdgeInsets): EdgeInsets {
    return EdgeInsets(
        top + edgeInsets.top,
        left + edgeInsets.left,
        bottom + edgeInsets.bottom,
        right + edgeInsets.right
    )
}

operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
    val direction = LayoutDirection.Ltr
    return PaddingValues(
        start = calculateStartPadding(direction) + paddingValues.calculateStartPadding(direction),
        top = calculateTopPadding() + paddingValues.calculateTopPadding(),
        end = calculateEndPadding(direction) + paddingValues.calculateEndPadding(direction),
        bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding(),
    )
}