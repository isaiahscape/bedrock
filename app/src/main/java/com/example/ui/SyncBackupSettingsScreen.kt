package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncBackupSettingsScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val masterPassword by viewModel.masterPassword.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupString by remember { mutableStateOf("") }
    var restoreString by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.exportBackupToFile(context, masterPassword ?: "1234") { path ->
                Toast.makeText(context, "Backup saved to: $path", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Storage permission required for legacy devices", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleExportToDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // MediaStore handles it without WRITE permission
            viewModel.exportBackupToFile(context, masterPassword ?: "1234") { path ->
                Toast.makeText(context, "Backup saved to: $path", Toast.LENGTH_LONG).show()
            }
        } else {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                viewModel.exportBackupToFile(context, masterPassword ?: "1234") { path ->
                    Toast.makeText(context, "Backup saved to: $path", Toast.LENGTH_LONG).show()
                }
            } else {
                permissionLauncher.launch(permission)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync & Backup") },
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
                text = "Manage your data synchronization and secure local backups.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Group 1: Backup & Restore
            Text(
                text = "Local Backup",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Create Encrypted Backup",
                    subtitle = "Export notes using Master Password",
                    icon = Icons.Default.CloudUpload,
                    onClick = {
                        coroutineScope.launch {
                            val backup = viewModel.getEncryptedBackup(masterPassword ?: "1234")
                            backupString = backup
                            showBackupDialog = true
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ExpressiveSettingsItem(
                    title = "Export to Download/Bedrock",
                    subtitle = "Save encrypted JSON file locally",
                    icon = Icons.Default.SaveAlt,
                    onClick = { handleExportToDownload() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ExpressiveSettingsItem(
                    title = "Restore from Backup",
                    subtitle = "Import notes from encrypted data",
                    icon = Icons.Default.CloudDownload,
                    onClick = { showRestoreDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 2: Synchronization
            Text(
                text = "Synchronization",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Sync Status",
                    subtitle = "All notes are up to date",
                    icon = Icons.Default.Sync,
                    onClick = { viewModel.triggerSync() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ExpressiveSettingsItem(
                    title = "Cloud integrity check",
                    subtitle = "Verify device replica checksums",
                    icon = Icons.Default.VerifiedUser,
                    onClick = { /* Simulation */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup Created") },
            text = {
                Column {
                    Text("Your encrypted backup is ready. Keep this string safe:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = backupString.take(100) + "...",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    clipboardManager.setText(AnnotatedString(backupString))
                    showBackupDialog = false
                }) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("Close") }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Notes") },
            text = {
                Column {
                    OutlinedTextField(
                        value = restoreString,
                        onValueChange = { restoreString = it },
                        label = { Text("Backup Data (Base64)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it },
                        label = { Text("Backup Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (restoreMessage != null) {
                        Text(
                            text = restoreMessage!!,
                            color = if (restoreMessage!!.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importEncryptedBackup(restoreString, restorePassword) { count ->
                        if (count == -1) {
                            restoreMessage = "Error: Invalid key or corrupted data"
                        } else {
                            restoreMessage = "Successfully imported $count notes!"
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(2000)
                                showRestoreDialog = false
                            }
                        }
                    }
                }) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
            }
        )
    }
}
