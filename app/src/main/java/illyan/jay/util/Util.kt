/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

@file:OptIn(ExperimentalMaterialApi::class)

package illyan.jay.util

import android.os.SystemClock
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ktx.utils.sphericalPathLength
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import illyan.jay.domain.model.DomainLocation
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.time.Duration
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

fun Duration.format(
    separator: String = " ",
    second: String = "s",
    minute: String = "m",
    hour: String = "h",
    day: String = "d",
): String {
    return toComponents { days, hours, minutes, seconds, _ ->
        val builder = StringBuilder()
        if (days > 0) {
            builder.append(days.toString() + day + separator)
        }
        if (days > 0 || hours > 0) {
            builder.append(hours.toString() + hour + separator)
        }
        if (days > 0 || hours > 0 || minutes > 0) {
            builder.append(minutes.toString() + minute + separator)
        }
        builder.append(seconds.toString() + second)
        builder.toString()
    }
}

fun BottomSheetState.isExpanding() =
    isAnimationRunning && targetValue == BottomSheetValue.Expanded

fun BottomSheetState.isCollapsing() =
    isAnimationRunning && targetValue == BottomSheetValue.Collapsed

fun BottomSheetState.isExpandedOrWillBe() =
    isExpanding() || isExpanded

fun BottomSheetState.isCollapsedOrWillBe() =
    isCollapsing() || isCollapsed

fun CameraOptions.Builder.extraOptions(
    extraOptions: (CameraOptions.Builder) -> CameraOptions.Builder = { it },
) = extraOptions(this)

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

operator fun PaddingValues.minus(paddingValues: PaddingValues): PaddingValues {
    val direction = LayoutDirection.Ltr
    return PaddingValues(
        start = calculateStartPadding(direction) - paddingValues.calculateStartPadding(direction),
        top = calculateTopPadding() - paddingValues.calculateTopPadding(),
        end = calculateEndPadding(direction) - paddingValues.calculateEndPadding(direction),
        bottom = calculateBottomPadding() - paddingValues.calculateBottomPadding(),
    )
}

fun Point.equals(point: Point, accuracyInMeters: Double): Boolean {
    val thisLatLng = LatLng(latitude(), longitude())
    val latLng = LatLng(point.latitude(), point.longitude())
    return thisLatLng.equals(latLng, accuracyInMeters)
}

fun LatLng.equals(latLng: LatLng, accuracyInMeters: Double): Boolean {
    return SphericalUtil.computeDistanceBetween(this, latLng) <= accuracyInMeters
}

fun LatLng.toGeoPoint() = GeoPoint(latitude, longitude)

fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

fun Point.toLatLng() = LatLng(latitude(), longitude())

fun Instant.toTimestamp() = Timestamp(epochSecond, nano)

fun ZonedDateTime.toTimestamp() = toInstant().toTimestamp()

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

fun Timestamp.toZonedDateTime() = toInstant().atZone(ZoneOffset.UTC)

fun List<DomainLocation>.sphericalPathLength() = sortedBy {
    it.zonedDateTime.toInstant().toEpochMilli()
}.map { it.latLng }.sphericalPathLength()

fun Modifier.textPlaceholder(
    visible: Boolean,
    placeholderHighlight: PlaceholderHighlight? = null,
    shape: Shape = RoundedCornerShape(4.dp)
) = composed {
    Modifier.placeholder(
        visible = visible,
        highlight = placeholderHighlight ?: PlaceholderHighlight.shimmer(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    )
}

fun Modifier.largeTextPlaceholder(
    visible: Boolean,
    placeholderHighlight: PlaceholderHighlight? = null,
    shape: Shape = RoundedCornerShape(8.dp)
) = composed {
    Modifier.placeholder(
        visible = visible,
        highlight = placeholderHighlight ?: PlaceholderHighlight.shimmer(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    )
}

fun Modifier.cardPlaceholder(
    visible: Boolean,
    placeholderHighlight: PlaceholderHighlight? = null,
    shape: Shape = RoundedCornerShape(12.dp)
) = composed {
    Modifier.placeholder(
        visible = visible,
        highlight = placeholderHighlight ?: PlaceholderHighlight.shimmer(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    )
}
