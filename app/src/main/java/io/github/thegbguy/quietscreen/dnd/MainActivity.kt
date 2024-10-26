package io.github.thegbguy.quietscreen.dnd

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentSanitizer
import androidx.core.util.Predicate
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
        if (!this::serviceIntent.isInitialized) {
            serviceIntent = Intent(this, DndService::class.java).apply {
                component = ComponentName(this@MainActivity, DndService::class.java)
                action = "io.github.thegbguy.ACTION_START_DND"
                type = "text/plain"
                data = Uri.parse("io.github.thegbguy://dnd_service")
            }
        }

        try {
            val sanitizedIntent = IntentSanitizer.Builder()
                .allowComponent(serviceIntent.component!!)      // Allow the service's component
                .allowAction("io.github.thegbguy.ACTION_START_DND")  // Allow only specific actions
                .allowData(Predicate.not { it.scheme != "io.github.thegbguy" })                    // Restrict data URI usage
                .allowType("text/plain")                     // Allow only this MIME type
                .build()
                .sanitizeByThrowing(serviceIntent)  // Will throw if the intent is malicious

            // Start the foreground service using the sanitized intent
            ContextCompat.startForegroundService(this, sanitizedIntent)

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Toast.makeText(this, "Invalid intent detected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopDndService() {
        if (this::serviceIntent.isInitialized.not()) return
        stopService(intent)
    }
}
