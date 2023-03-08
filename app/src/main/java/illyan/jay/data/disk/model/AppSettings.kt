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
 */

package illyan.jay.data.disk.model

import illyan.jay.domain.model.DomainPreferences
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val clientUUID: String? = null,
    val preferences: DomainPreferences = DomainPreferences.Default,
) {
    companion object {
        val default = AppSettings()
    }
    // TODO: "Press this <button or screen> to support the project by giving me ad revenue"
    //  in user preferences screen. This is an easy way to support the project besides
    //  supporting it directly with donations.
}
