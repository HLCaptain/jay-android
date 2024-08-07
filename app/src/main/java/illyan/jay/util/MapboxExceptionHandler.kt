/*
 * Copyright (c) 2024 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.util

import timber.log.Timber

class MapboxExceptionHandler : Thread.UncaughtExceptionHandler {

    var openGlNotSupportedCallback: () -> Unit = {}

    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.e(e)
        if (e is IllegalStateException &&
            e.message?.contains("OpenGL ES 3.0 context could not be created") == true) {
            // Older emulated Android devices may not support OpenGL ES 3.0 properly
            openGlNotSupportedCallback()
        }
    }
}