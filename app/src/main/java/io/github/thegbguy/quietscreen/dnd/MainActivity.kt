package io.github.thegbguy.quietscreen.dnd

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.thegbguy.quietscreen.core.createNotificationChannel
import io.github.thegbguy.quietscreen.core.hasDndPermission
import io.github.thegbguy.quietscreen.core.isBatteryOptimizationDisabled
import io.github.thegbguy.quietscreen.core.isDndModeEnabled
import io.github.thegbguy.quietscreen.core.setDndMode
import io.github.thegbguy.quietscreen.core.showDndStatusNotification
import io.github.thegbguy.quietscreen.core.showIgnoreBatteryOptimizations

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = 1010

    private val isSilentModeActive by lazy { mutableStateOf(isDndModeEnabled(this)) }
    private val hasDndPermission by lazy { mutableStateOf(hasDndPermission(this)) }
    private val isBatteryOptimizationDisabled by lazy {
        mutableStateOf(isBatteryOptimizationDisabled(this))
    }
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
                ),
                requestNotificationPermission
            )
        }

        setContent {
            QuiteScreenApp(
                hasDndPermission = hasDndPermission.value,
                isSilentModeActive = isSilentModeActive.value,
                isBatteryOptimizationDisabled = isBatteryOptimizationDisabled.value,
                onToggle = { active ->
                    isSilentModeActive.value = active
                    setDndMode(this, active)
                    showDndStatusNotification(this, active)
                    if (active) startDndService() else stopDndService()
                },
                onBatteryOptimizationClick = {
                    showIgnoreBatteryOptimizations(this)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        isSilentModeActive.value = isDndModeEnabled(this)
        hasDndPermission.value = hasDndPermission(this)
        isBatteryOptimizationDisabled.value = isBatteryOptimizationDisabled(this)
    }

    private fun startDndService() {
        if (this::serviceIntent.isInitialized.not()) {
            serviceIntent = Intent(this, DndService::class.java)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopDndService() {
        if (this::serviceIntent.isInitialized.not()) return
        stopService(intent)
    }
}
