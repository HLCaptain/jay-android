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

package illyan.jay.ui.libraries

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import illyan.jay.ui.libraries.model.Library
import illyan.jay.ui.libraries.model.License
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LibrariesViewModel @Inject constructor(

) : ViewModel() {
    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries = _libraries.asStateFlow()

    init {
        _libraries.value = listOf(
            Library(
                name = "Plumber",
                license = License(
                    type = "Apache v2",
                    description = "Cool license",
                ),
                moreInfoUrl = "https://github.com/HLCaptain/plumber",
                repositoryUrl = "https://github.com/HLCaptain/plumber"
            )
        )
    }
}