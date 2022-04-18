package illyan.jay.util

open class ItemEventListener<Item : Any> {
    private var itemClickListener: ((Item) -> Unit) = {}
    private var itemLongClickListener: ((Item) -> Boolean) = { true }

    fun setOnItemClickListener(listener: (Item) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (Item) -> Boolean) {
        itemLongClickListener = listener
    }

    fun onItemClick(item: Item) = itemClickListener.invoke(item)
    fun onItemLongClick(item: Item) = itemLongClickListener.invoke(item)
}