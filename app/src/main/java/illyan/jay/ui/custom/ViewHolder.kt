/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.ui.custom

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * ViewHolder using ViewBinding to bind its view and
 * Data classes to restrict information given.
 *
 * @param VB ViewBinding class.
 * @param Item data class, which carries information for the UI.
 * @property binding binding of the view.
 * @constructor Create empty View holder
 */
abstract class ViewHolder<VB : ViewBinding, Item : Any>(
    private val binding: VB
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Item must change from null to a bound (not null) state.
     * It calls itemChanged when that happens.
     */
    var item: Item? = null
        set(value) {
            value?.let {
                field = value
                itemChanged(it)
            }
        }

    /**
     * Gets called when the item is changed.
     * Great opportunity to bind your view to the new item.
     *
     * @param newItem item's new value.
     */
    protected abstract fun itemChanged(newItem: Item)
}