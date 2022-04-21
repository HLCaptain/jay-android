/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.util

/**
 * Simple item event listener propagates events onto set functions.
 *
 * @param Item class item to propagate to the events listeners.
 * @constructor Create empty Item event listener
 */
open class ItemEventListener<Item : Any> {
    private var itemClickListener: ((Item) -> Unit) = {}
    private var itemLongClickListener: ((Item) -> Boolean) = { true }

    /**
     * Set on item click listener.
     *
     * @param listener listener is set to be called on item click event.
     * @receiver
     */
    fun setOnItemClickListener(listener: (Item) -> Unit) {
        itemClickListener = listener
    }

    /**
     * Set on item long click listener.
     *
     * @param listener listener is set to be called on item long click event.
     * @receiver
     */
    fun setOnItemLongClickListener(listener: (Item) -> Boolean) {
        itemLongClickListener = listener
    }

    /**
     * On item click.
     *
     * @param item item clicked on.
     */
    fun onItemClick(item: Item) = itemClickListener.invoke(item)

    /**
     * On item long click.
     *
     * @param item item long clicked on.
     */
    fun onItemLongClick(item: Item) = itemLongClickListener.invoke(item)
}