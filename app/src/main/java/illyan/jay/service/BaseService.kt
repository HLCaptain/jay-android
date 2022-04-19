/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import illyan.jay.MainActivity
import timber.log.Timber

abstract class BaseService : Service() {
    companion object {
        const val KEY_SERVICE_STATE_CHANGE = "KEY_SERVICE_STATE_CHANGE"
        const val KEY_SERVICE_NAME = "KEY_SERVICE_NAME"
        const val SERVICE_RUNNING = "SERVICE_RUNNING"
        const val SERVICE_STOPPED = "SERVICE_STOPPED"
    }

    private fun broadcastStateChange(
        name: String,
        state: String
    ) {
        val intent = Intent()
        intent.action = KEY_SERVICE_STATE_CHANGE
        intent.putExtra(KEY_SERVICE_NAME, name)
        intent.putExtra(KEY_SERVICE_STATE_CHANGE, state)
        Timber.i("$name state = $state")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        broadcastStateChange(this::class.simpleName.toString(), SERVICE_RUNNING)
    }

    override fun onDestroy() {
        broadcastStateChange(this::class.simpleName.toString(), SERVICE_STOPPED)
        super.onDestroy()
    }

    fun createNotification(
        title: String,
        text: String,
        channelId: String,
        notificationId: Int,
        icon: IconCompat
    ): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

        createNotificationChannel(channelId)

        val contentIntent = PendingIntent.getActivity(
            this,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(icon)
            .setVibrate(longArrayOf(1000, 2000, 1000))
            .setContentIntent(contentIntent)
            .setSilent(true)
            .build()
    }

    fun updateNotification(
        title: String,
        text: String,
        channelId: String,
        notificationId: Int,
        icon: IconCompat
    ) {
        val notification = createNotification(title, text, channelId, notificationId, icon)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(channelId: String) {
        val serviceChannel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }
}