package com.example.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var isBatteryOptimized by remember { 
        mutableStateOf(!powerManager.isIgnoringBatteryOptimizations(context.packageName)) 
    }
    
    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders & Reliability") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Ensure your reminders arrive exactly on time by configuring these system settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            SettingsGroup {
                // System Notifications
                ExpressiveSettingsItem(
                    title = "System Notification Settings",
                    subtitle = "Manage channels and importance",
                    icon = Icons.Default.NotificationsActive,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsGroup {
                // Battery Optimization
                ExpressiveSettingsItem(
                    title = "Battery Restriction Bypass",
                    subtitle = if (isBatteryOptimized) "Optimized (May delay reminders)" else "Bypassed (Reliable reminders)",
                    icon = Icons.Default.BatteryAlert,
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
                
                if (isBatteryOptimized) {
                    Text(
                        text = "Recommend disabling optimization to prevent Android from delaying your reminders during Doze mode.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 76.dp, end = 20.dp, bottom = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exact Alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsGroup {
                    ExpressiveSettingsItem(
                        title = "Exact Alarm Permission",
                        subtitle = if (canScheduleExactAlarms) "Granted" else "Not Granted (Reminders may be late)",
                        icon = Icons.Default.Timer,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
