package io.github.thegbguy.quietscreen

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = 1010
    private val receiver = ScreenStateReceiver()

    private val isSilentModeActive by lazy { mutableStateOf(isDndModeEnabled(this)) }
    private val hasDndPermission by lazy { mutableStateOf(hasDndPermission(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)
        registerScreenStateReceiver()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestNotificationPermission
            )
        }

        setContent {
            SilentModeApp(
                hasDndPermission = hasDndPermission.value,
                isSilentModeActive = isSilentModeActive.value,
                onToggle = { active ->
                    isSilentModeActive.value = active
                    setDndMode(this, active)
                    showDndStatusNotification(this, active)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        isSilentModeActive.value = isDndModeEnabled(this)
        hasDndPermission.value = hasDndPermission(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterScreenStateReceiver()
    }

    private fun registerScreenStateReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(receiver, intentFilter)
    }

    private fun unregisterScreenStateReceiver() {
        unregisterReceiver(receiver)
    }
}

@Composable
fun SilentModeApp(
    hasDndPermission: Boolean,
    isSilentModeActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Scaffold(
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                HomeScreen(
                    isActive = isSilentModeActive,
                    hasDndPermission = hasDndPermission,
                    onToggle = onToggle
                )
            }
        }
    )
}

@Composable
fun HomeScreen(
    isActive: Boolean,
    hasDndPermission: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppStatusCard(isActive, onToggle)

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasDndPermission) {
            DndPermissionCard()
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DndPermissionCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
        onClick = {
            requestDndPermission(context)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                    contentDescription = "Warning",
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "DND permission is required! Please click here to grant it.",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Image(
                painter = painterResource(R.drawable.guide),
                contentDescription = "Guide",
            )
        }
    }
}

@Composable
fun AppStatusCard(isActive: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isActive) "Silent Mode Active" else "Silent Mode Inactive",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BellSwitch(
                isChecked = isActive,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun BatteryOptimizationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Battery Optimization",
                tint = Color(0xFF0097A7),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Disable battery optimization for the best performance.",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun BellSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(
                id = if (isChecked) R.drawable.bell_off
                else R.drawable.bell_ring
            ),
            contentDescription = if (isChecked) "Bell Off" else "Bell Ring",
            tint = if (isChecked) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color(0xFFF44336),
                checkedTrackColor = Color(0xFF81C784),
                uncheckedTrackColor = Color(0xFFE57373)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSilentModeApp() {
    SilentModeApp(hasDndPermission = false, isSilentModeActive = true) {}
}
