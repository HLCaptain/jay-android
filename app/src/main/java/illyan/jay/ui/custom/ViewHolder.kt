package illyan.jay.ui.custom

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * ViewHolder using ViewBinding to bind its view and Data classes to restrict information given.
 */
abstract class ViewHolder<VB : ViewBinding, Item : Any>(
    private val binding: VB
) : RecyclerView.ViewHolder(binding.root) {
    var item: Item? = null
        set(value) {
            value?.let {
                field = value
                itemChanged(it)
            }
        }

    protected abstract fun itemChanged(newItem: Item)
}