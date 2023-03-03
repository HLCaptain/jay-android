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
import kotlinx.parcelize.Parcelize

@Parcelize
data class License(
    val name: String? = null,
    val type: LicenseType? = null,
    val description: String? = null,
) : Parcelable {
    enum class LicenseType(
        val licenseName: String,
        val description: String,
    ) {
        ApacheV2(
            licenseName = "Apache-2.0",
            description = "\n" +
                    "Licensed under the Apache License, Version 2.0 (the \"License\");" +
                    " you may not use this file except in compliance with the License." +
                    " You may obtain a copy of the License at\n" +
                    "\n" +
                    "    https://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "Unless required by applicable law or agreed to in writing, software" +
                    " distributed under the License is distributed on an \"AS IS\" BASIS," +
                    " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                    " See the License for the specific language governing permissions and" +
                    " limitations under the License."
        ),
    }

    class Builder {
        var interval: IntRange? = null
        var year: Int? = null
        var author: String? = null
        var type: LicenseType? = null

        val copyrightYear: String?
            get() = if (interval != null) {
                "${interval?.first}-${interval?.last}"
            } else if (year != null) {
                year.toString()
            } else {
                null
            }

        val description: String?
            get() = when (type) {
                LicenseType.ApacheV2 -> {
                    // "\\s+".toRegex() removes additional whitespaces between words
                    "Copyright ${if (copyrightYear != null) copyrightYear else ""} $author\n"
                        .replace("\\s+".toRegex(), " ") + LicenseType.ApacheV2.description
                }

                else -> null
            }

        fun setAuthor(author: String): Builder {
            this.author = author
            return this
        }

        fun setYearInterval(interval: IntRange): Builder {
            this.interval = interval
            return this
        }

        fun setYear(year: Int): Builder {
            this.year = year
            return this
        }

        fun setType(type: LicenseType): Builder {
            this.type = type
            return this
        }

        fun build(): License {
            return License(
                name = type?.licenseName,
                type = type,
                description = description
            )
        }
    }
}
