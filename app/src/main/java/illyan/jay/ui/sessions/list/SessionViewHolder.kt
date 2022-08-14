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

import illyan.jay.databinding.ItemSessionBinding
import illyan.jay.ui.custom.ViewHolder
import illyan.jay.ui.sessions.list.model.UiSession
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.roundToLong

class SessionViewHolder(
    private val binding: ItemSessionBinding,
    private val itemEventListener: SessionItemEventListener
) : ViewHolder<ItemSessionBinding, UiSession>(binding) {

    init {
        binding.sessionCard.setOnClickListener { item?.let { itemEventListener.onItemClick(it) } }
        binding.sessionCard.setOnLongClickListener {
            item?.let { return@setOnLongClickListener itemEventListener.onItemLongClick(it) }
            false
        }
        binding.viewInMapButton.setOnClickListener {
            item?.let { itemEventListener.onMapClick(it) }
        }
    }

    override fun itemChanged(newItem: UiSession) {
        binding.apply {
            sessionTitle.text = newItem.id.toString()
            sessionDistance.text = String.format(
                Locale.US,
                (newItem.distance / 1000.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP)
                    .toString()
            )
            sessionStart.text =
                SimpleDateFormat("HH:mm MMM dd", Locale.US).format(newItem.startTime)
            sessionEnd.text = newItem.endTime?.let {
                SimpleDateFormat("HH:mm MMM dd", Locale.US).format(it)
            }

            // Duration of session
            val durationInMillis = newItem.endTime?.let {
                (it.time - newItem.startTime.time)
            } ?: (Instant.now().toEpochMilli() - newItem.startTime.time)
            val durationInMinutes = (durationInMillis.toFloat() / 1000 / 60).roundToLong()
            sessionDuration.text = durationInMinutes.toString()
        }
    }
}
