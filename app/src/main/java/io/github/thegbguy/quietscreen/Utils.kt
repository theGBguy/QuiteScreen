package io.github.thegbguy.quietscreen

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun hasDndPermission(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

fun requestDndPermission(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    context.startActivity(intent)
}

fun isDndModeEnabled(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return if (notificationManager.isNotificationPolicyAccessGranted) {
        notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
    } else {
        false
    }
}

fun setDndMode(context: Context, isEnabled: Boolean) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (notificationManager.isNotificationPolicyAccessGranted) {
        notificationManager.setInterruptionFilter(
            if (isEnabled) NotificationManager.INTERRUPTION_FILTER_NONE
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "dnd_channel",
            "DND Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the current DND status"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@SuppressLint("MissingPermission")
fun showDndStatusNotification(context: Context, isSilent: Boolean) {
    // Create an intent to launch the app when the notification is clicked
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Create the notification
    val notification = NotificationCompat.Builder(context, "dnd_channel")
        .setSmallIcon(if (isSilent) R.drawable.bell_off else R.drawable.bell_ring)
        .setContentTitle("DND Status")
        .setContentText(if (isSilent) "Silent Mode is ON" else "Silent Mode is OFF")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .build()

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(1, notification)
    }
}