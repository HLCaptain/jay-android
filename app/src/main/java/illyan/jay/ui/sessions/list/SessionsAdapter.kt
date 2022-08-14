/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.ui.sessions.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import illyan.jay.databinding.ItemSessionBinding
import illyan.jay.ui.sessions.list.model.UiSession
import javax.inject.Inject

class SessionsAdapter @Inject constructor(
    var itemEventListener: SessionItemEventListener
) : ListAdapter<UiSession, SessionViewHolder>(SessionComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SessionViewHolder(
        ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ),
        itemEventListener
    )

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.item = getItem(position)
    }
}
