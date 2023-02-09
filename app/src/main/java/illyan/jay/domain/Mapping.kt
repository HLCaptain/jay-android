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

package illyan.jay.domain

import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType

fun IndexableRecord.toFavoriteRecord() = FavoriteRecord(
    address = address,
    categories = categories,
    coordinate = coordinate,
    descriptionText = descriptionText,
    id = id,
    makiIcon = makiIcon,
    metadata = metadata,
    name = name,
    routablePoints = routablePoints,
    type = type
)

fun SearchResult.toFavoriteRecord(
    type: SearchResultType = types.firstOrNull() ?: SearchResultType.PLACE
) = FavoriteRecord(
    address = address,
    categories = categories,
    coordinate = coordinate,
    descriptionText = descriptionText,
    id = id,
    makiIcon = makiIcon,
    metadata = metadata,
    name = name,
    routablePoints = routablePoints,
    type = type
)
