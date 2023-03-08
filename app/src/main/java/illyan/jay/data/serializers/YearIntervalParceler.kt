/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.data.serializers

import android.os.Parcel
import kotlinx.parcelize.Parceler

/**
 * Writes and reads nullable [IntRange] values.
 * Only saves [IntRange.first] and [IntRange.last] values.
 * Returns null if [IntRange.first] or [IntRange.last] value is [Int.MIN_VALUE].
 */
object YearIntervalParceler : Parceler<IntRange?> {
    override fun create(parcel: Parcel): IntRange? {
        val first: Int = parcel.readInt()
        val last: Int = parcel.readInt()
        return if (first == Int.MIN_VALUE || last == Int.MIN_VALUE) null else IntRange(first, last)
    }
    override fun IntRange?.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(this?.first ?: Int.MIN_VALUE)
        parcel.writeInt(this?.last ?: Int.MIN_VALUE)
    }
}