/*
 * Copyright (c) 2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.custom

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import co.zsmb.rainbowcake.base.RainbowCakeActivity
import co.zsmb.rainbowcake.base.RainbowCakeViewModel

/**
 * Same as RainbowCakeActivity<VS, VM>, but uses ViewBinding for view inflation.
 *
 * @param VS ViewState class.
 * @param VM ViewModel class, which takes VS as a template parameter.
 * @param VB ViewBinding inflater class.
 * @constructor Create empty Rainbow cake activity
 */
abstract class RainbowCakeActivity<
		VS : Any,
		VM : RainbowCakeViewModel<VS>,
		VB : ViewBinding
		> : RainbowCakeActivity<VS, VM>() {

	protected lateinit var binding: VB

	/**
	 * Inflating binding and setting up the content view.
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = provideViewBinding()
		setContentView(binding.root)
	}

	/**
	 * Provide ViewBinding
	 *
	 * @return inflated ViewBinding, which has a root property.
	 */
	protected abstract fun provideViewBinding(): VB
}