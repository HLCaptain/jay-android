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

package illyan.jay.domain.model.libraries

enum class LicenseType(
    val licenseName: String,
    val url: String,
    val description: String,
) {
    ApacheV2(
        licenseName = "Apache-2.0",
        description = /* "Copyright $year $author" */ "\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");" +
                " you may not use this file except in compliance with the License." +
                " You may obtain a copy of the License at" +
                "\n" +
                "\thttps://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software" +
                " distributed under the License is distributed on an \"AS IS\" BASIS," +
                " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                " See the License for the specific language governing permissions and" +
                " limitations under the License.",
        url = "https://www.apache.org/licenses/LICENSE-2.0",
    ),
}