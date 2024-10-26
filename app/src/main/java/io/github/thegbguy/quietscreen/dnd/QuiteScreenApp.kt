package io.github.thegbguy.quietscreen.dnd

import android.Manifest
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.github.thegbguy.quietscreen.R
import io.github.thegbguy.quietscreen.core.requestDndPermission

@Composable
fun QuiteScreenApp(
    hasDndPermission: Boolean,
    isSilentModeActive: Boolean,
    isBatteryOptimizationDisabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onBatteryOptimizationClick: () -> Unit
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
                    isBatteryOptimizationDisabled = isBatteryOptimizationDisabled,
                    onToggle = onToggle,
                    onBatteryOptimizationClick = onBatteryOptimizationClick
                )
            }
        }
    )
}

@Composable
fun HomeScreen(
    isActive: Boolean,
    hasDndPermission: Boolean,
    isBatteryOptimizationDisabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onBatteryOptimizationClick: () -> Unit
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

        Spacer(modifier = Modifier.height(12.dp))

        if (isBatteryOptimizationDisabled.not()) {
            BatteryOptimizationCard(onClick = onBatteryOptimizationClick)
        }

        Spacer(modifier = Modifier.height(12.dp))

        PermissionRequestCard()

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
fun BatteryOptimizationCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)),
        onClick = onClick,
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

internal val permissions = listOf(
    Manifest.permission.POST_NOTIFICATIONS,
    Manifest.permission.FOREGROUND_SERVICE,
    Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestCard() {
    val permissionState = rememberMultiplePermissionsState(permissions)

    if (permissionState.allPermissionsGranted.not()) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = {
                permissionState.launchMultiplePermissionRequest()
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                permissions.forEachIndexed { index, permission ->
                    Text(
                        text = permissionWithDescription(permission),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (index != permission.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

fun permissionWithDescription(permission: String): String {
    return when (permission) {
        Manifest.permission.POST_NOTIFICATIONS -> permission.plus(": Allows the app to post notifications.")
        Manifest.permission.FOREGROUND_SERVICE -> permission.plus(": Required for running the app as a foreground service.")
        Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE -> permission.plus(": Needed for specialized background services.")
        else -> "Permission: $permission"
    }
}


@Preview(showBackground = true)
@Composable
fun QuiteScreenAppPreview() {
    QuiteScreenApp(
        hasDndPermission = false,
        isSilentModeActive = true,
        isBatteryOptimizationDisabled = false,
        onToggle = {},
        onBatteryOptimizationClick = {}
    )
}