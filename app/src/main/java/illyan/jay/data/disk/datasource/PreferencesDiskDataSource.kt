package illyan.jay.data.disk.datasource

import illyan.jay.data.disk.dao.PreferencesDao
import illyan.jay.data.disk.toDomainModel
import illyan.jay.data.disk.toRoomModel
import illyan.jay.domain.model.DomainPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class PreferencesDiskDataSource @Inject constructor(
    private val preferencesDao: PreferencesDao
) {
    fun getPreferences(userUUID: String): Flow<DomainPreferences?> {
        Timber.v("Getting preferences flow for user ${userUUID.take(4)}")
        return preferencesDao.getPreferences(userUUID).map {
            it?.toDomainModel()
        }
    }

    fun upsertPreferences(domainPreferences: DomainPreferences) {
        if (domainPreferences.userUUID != null) {
            Timber.v("Upserting $domainPreferences")
            preferencesDao.upsertPreferences(
                domainPreferences.toRoomModel(userUUID = domainPreferences.userUUID)
            )
        } else {
            Timber.e(
                IllegalArgumentException(
                    "Cannot save preferences with no user associated with it via Room!\n" +
                            "Offline user's preferences is saved per-app in [AppSettings]."
                )
            )
        }
    }

    fun deletePreferences(userUUID: String) = preferencesDao.deletePreferences(userUUID)

    fun deletePreferences(domainPreferences: DomainPreferences) {
        if (domainPreferences.userUUID != null) {
            Timber.v("Deleting $domainPreferences")
            preferencesDao.deletePreferences(
                domainPreferences.toRoomModel(domainPreferences.userUUID)
            )
        } else {
            Timber.e(
                IllegalArgumentException(
                    "User UUID was null, which is the primary key.\n" +
                            "So it cannot be null and isn't in the SQLite database."
                )
            )
        }
    }

    fun setAnalyticsEnabled(userUUID: String, analyticsEnabled: Boolean) {
        Timber.v("Setting analyticsEnabled to $analyticsEnabled for user ${userUUID.take(4)}")
        preferencesDao.setAnalyticsEnabled(userUUID, analyticsEnabled)
    }

    fun setFreeDriveAutoStart(userUUID: String, freeDriveAutoStart: Boolean) {
        Timber.v("Setting freeDriveAutoStart to $freeDriveAutoStart for user ${userUUID.take(4)}")
        preferencesDao.setFreeDriveAutoStart(userUUID, freeDriveAutoStart)
    }
}