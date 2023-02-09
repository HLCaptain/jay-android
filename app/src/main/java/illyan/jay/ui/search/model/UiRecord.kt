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

package illyan.jay.ui.search.model

import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchSuggestion

data class UiRecord(
    val id: String,
    val title: String,
    val description: String?,
    val isFavorite: Boolean,
)

fun FavoriteRecord.toUiModel() = UiRecord(
    id = id,
    title = name,
    description = address?.region,
    isFavorite = true
)

fun SearchSuggestion.toUiModel(
    isFavorite: Boolean
) = UiRecord(
    id = id,
    title = name,
    description = address?.region,
    isFavorite = isFavorite,
)

fun IndexableRecord.toUiModel(
    isFavorite: Boolean
) = UiRecord(
    id = id,
    title = name,
    description = address?.region,
    isFavorite = isFavorite,
)
