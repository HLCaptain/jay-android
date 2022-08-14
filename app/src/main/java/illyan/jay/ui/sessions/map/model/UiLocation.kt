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
 */

package illyan.jay.ui.sessions.map.model

import android.graphics.ColorSpace
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * @property argb alpha, red, green, blue channels' value from 0f to 1f.
 * @property colorSpace ColorSpace.Named enumeration value.
 */
data class UiLocation(
    val id: Long = -1,
    val latLng: LatLng,
    val sessionId: Long,
    val time: Date,
    val argb: FloatArray,
    val colorSpace: ColorSpace.Named = ColorSpace.Named.SRGB
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UiLocation

        if (id != other.id) return false
        if (latLng != other.latLng) return false
        if (sessionId != other.sessionId) return false
        if (time != other.time) return false
        if (!argb.contentEquals(other.argb)) return false
        if (colorSpace != other.colorSpace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + latLng.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + argb.contentHashCode()
        result = 31 * result + colorSpace.hashCode()
        return result
    }
}
