package io.github.thegbguy.quietscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                setDndMode(context, true)
                showDndStatusNotification(context, true)
            }

            Intent.ACTION_SCREEN_OFF -> {
                setDndMode(context, false)
                showDndStatusNotification(context, false)
            }
        }
    }
}
