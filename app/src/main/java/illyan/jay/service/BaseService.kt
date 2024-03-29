/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.jay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import illyan.jay.MainActivity
import timber.log.Timber

/**
 * Base (foreground) service class which provide some basic
 * state broadcasting behaviours.
 * Also provides a way for Foreground Services to
 * update notifications as needed.
 * OnBind returns null as a default.
 *
 * @constructor Create empty Base service
 */
abstract class BaseService : Service() {
    /**
     * Broadcast service state change.
     *
     * @param name name of the service.
     * @param state state of the service.
     */
    private fun broadcastStateChange(
        name: String,
        state: String,
    ) {
        val intent = Intent()
        intent.action = KEY_SERVICE_STATE_CHANGE
        intent.putExtra(KEY_SERVICE_NAME, name)
        intent.putExtra(KEY_SERVICE_STATE_CHANGE, state)
        Timber.i("$name state = $state")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    /**
     * On bind always returns null, because the service is a foreground service.
     *
     * @param intent
     * @return null
     */
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

    /**
     * Create notification
     *
     * @param title
     * @param text
     * @param channelId
     * @param notificationId
     * @param icon
     * @return
     */
    fun createNotification(
        title: String,
        text: String,
        channelId: String,
        notificationId: Int,
        icon: IconCompat,
        channelDescription: String,
    ): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

        createNotificationChannel(channelId, text, channelDescription)

        val contentIntent = PendingIntent.getActivity(
            this,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setVibrate(longArrayOf(1000, 2000, 1000))
            .setContentIntent(contentIntent)
            .setSilent(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification.setSmallIcon(icon)
        }

        return notification.build()
    }

    /**
     * Update notification
     *
     * @param title
     * @param text
     * @param channelId
     * @param notificationId
     * @param icon
     */
    fun updateNotification(
        title: String,
        text: String,
        channelId: String,
        notificationId: Int,
        icon: IconCompat,
        channelDescription: String,
    ) {
        val notification = createNotification(
            title,
            text,
            channelId,
            notificationId,
            icon,
            channelDescription
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun removeNotification(
        notificationId: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.deleteNotificationChannel(notificationId.toString())
        }
    }

    private fun createNotificationChannel(
        channelId: String,
        name: String,
        description: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val KEY_SERVICE_STATE_CHANGE = "KEY_SERVICE_STATE_CHANGE"
        const val KEY_SERVICE_NAME = "KEY_SERVICE_NAME"
        const val SERVICE_RUNNING = "SERVICE_RUNNING"
        const val SERVICE_STOPPED = "SERVICE_STOPPED"
    }
}
