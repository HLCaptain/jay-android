package illyan.jay.ui.settings.model

import illyan.jay.domain.model.DomainPreferences
import java.time.ZonedDateTime

data class UiPreferences(
    val userUUID: String?,
    val analyticsEnabled: Boolean,
    val freeDriveAutoStart: Boolean,
    val lastUpdate: ZonedDateTime
)

fun DomainPreferences.toUiModel() = UiPreferences(
    userUUID = userUUID,
    analyticsEnabled = analyticsEnabled,
    freeDriveAutoStart = freeDriveAutoStart,
    lastUpdate = lastUpdate
)
