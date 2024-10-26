package io.github.thegbguy.quietscreen.dnd

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import io.github.thegbguy.quietscreen.core.getNotification

class DndService : Service() {

    private val screenStateReceiver by lazy { ScreenStateReceiver() }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)

        val notification = getNotification(applicationContext)
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in foreground with a notification
        val notification = getNotification(applicationContext)
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
