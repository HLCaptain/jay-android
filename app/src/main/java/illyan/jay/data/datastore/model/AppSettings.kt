package illyan.jay.data.datastore.model

import illyan.jay.domain.model.DomainPreferences
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val clientUUID: String? = null,
    val preferences: DomainPreferences = DomainPreferences.Default,
) {
    companion object {
        val default = AppSettings()
    }
    // TODO: "Press this <button or screen> to support the project by giving me ad revenue"
    //  in user preferences screen. This is an easy way to support the project besides
    //  supporting it directly with donations.
}