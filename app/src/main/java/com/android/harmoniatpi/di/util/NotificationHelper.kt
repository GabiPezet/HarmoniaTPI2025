package com.android.harmoniatpi.di.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.android.harmoniatpi.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showSimpleNotification(title: String, content: String) {
        createChannelIfNeeded()

//        val intent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
        val dummyIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            dummyIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "test_channel_id")
            .setSmallIcon(R.drawable.ic_harmonyicon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent) // ← esto hace que al tocarla se dispare, pero no pase nada
            .setAutoCancel(true)             // ← esto hace que se borre automáticamente al tocarla
            .build()

        notificationManager.notify(2, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test_channel_id",
                "Canal de Prueba",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Descripción del canal"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}