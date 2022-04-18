package illyan.jay.ui.sessions.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import illyan.jay.databinding.ItemSessionBinding
import illyan.jay.ui.sessions.list.model.UiSession
import illyan.jay.util.ItemEventListener
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