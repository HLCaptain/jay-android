/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import co.zsmb.rainbowcake.base.RainbowCakeFragment
import co.zsmb.rainbowcake.base.RainbowCakeViewModel

/**
 * Same as RainbowCakeFragment<VS, VM>, but uses ViewBinding for view inflation.
 *
 * @param VS ViewState class.
 * @param VM ViewModel class, which takes VS as a template parameter.
 * @param VB ViewBinding inflater class.
 * @constructor Create empty Rainbow cake fragment
 */
abstract class RainbowCakeFragment<
        VS : Any,
        VM : RainbowCakeViewModel<VS>,
        VB : ViewBinding
        > : RainbowCakeFragment<VS, VM>() {

    protected lateinit var binding: VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = provideViewBindingInflater().invoke(inflater, container, false)
        return binding.root
    }

    /**
     * Provide view binding inflater.
     *
     * @return inflater function with proper parameters.
     */
    protected abstract fun provideViewBindingInflater(): (LayoutInflater, ViewGroup?, Boolean) -> VB
}