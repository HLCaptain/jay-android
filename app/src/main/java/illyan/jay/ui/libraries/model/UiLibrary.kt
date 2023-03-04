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

package illyan.jay.ui.libraries.model

import android.os.Parcelable
import illyan.jay.data.serializers.YearIntervalParceler
import illyan.jay.domain.model.libraries.Library
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<IntRange?, YearIntervalParceler>
data class UiLibrary(
    val name: String = "",
    val license: UiLicense? = null,
    val repositoryUrl: String? = null,
    val moreInfoUrl: String? = null,
    val authors: List<String> = emptyList(),
) : Parcelable

fun Library.toUiModel() = UiLibrary(
    name = name,
    license = license?.toUiModel(),
    repositoryUrl = repositoryUrl,
    moreInfoUrl = moreInfoUrl,
    authors = authors,
)
