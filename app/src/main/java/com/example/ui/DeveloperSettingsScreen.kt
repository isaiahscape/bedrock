package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Options") },
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
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Advanced debugging tools for Bedrock development and troubleshooting.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Debug Group
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Generate logs.txt",
                    subtitle = "Saved to Download/Bedrock",
                    icon = Icons.Default.BugReport,
                    onClick = {
                        viewModel.generateLogsFile(context) { path ->
                            Toast.makeText(context, "Logs saved to: $path", Toast.LENGTH_LONG).show()
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ExpressiveSettingsItem(
                    title = "Clear Sync Logs",
                    subtitle = "Wipe all local sync history",
                    icon = Icons.Default.DeleteSweep,
                    onClick = {
                        viewModel.clearSyncLogs()
                        Toast.makeText(context, "Sync logs cleared", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility Group
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Disable Developer Mode",
                    subtitle = "Hide these options from settings",
                    icon = Icons.Default.Terminal,
                    onClick = {
                        viewModel.setDeveloperMode(false)
                        onBack()
                    },
                    showChevron = false
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
