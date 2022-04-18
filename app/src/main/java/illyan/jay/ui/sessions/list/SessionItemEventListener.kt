package illyan.jay.ui.sessions.list

import illyan.jay.ui.sessions.list.model.UiSession
import illyan.jay.util.ItemEventListener

class SessionItemEventListener : ItemEventListener<UiSession>() {
	private var mapClickListener: ((UiSession) -> Unit) = {}

	fun setOnMapClickListener(listener: (UiSession) -> Unit) {
		mapClickListener = listener
	}

	fun onMapClick(session: UiSession) = mapClickListener.invoke(session)
}