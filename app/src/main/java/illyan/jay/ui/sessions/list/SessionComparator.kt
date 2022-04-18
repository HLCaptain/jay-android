package illyan.jay.ui.sessions.list

import androidx.recyclerview.widget.DiffUtil
import illyan.jay.ui.sessions.list.model.UiSession

object SessionComparator : DiffUtil.ItemCallback<UiSession>() {
    override fun areItemsTheSame(oldItem: UiSession, newItem: UiSession): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UiSession, newItem: UiSession): Boolean {
        return oldItem.distance == newItem.distance &&
                oldItem.startTime == newItem.startTime &&
                oldItem.endTime == newItem.endTime
    }
}