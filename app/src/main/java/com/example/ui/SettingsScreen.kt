package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NoteViewModel,
    themeMode: String,
    onNavigateToAppearance: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Search Bar
            SettingsSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Group 1: Appearance
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Appearance",
                    subtitle = when (themeMode) {
                        "light" -> "Light mode"
                        "dark" -> "Dark mode"
                        else -> "System default"
                    },
                    icon = Icons.Default.Palette,
                    onClick = onNavigateToAppearance
                )
            }

            // Group 2: Security
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Security",
                    subtitle = "Master PIN & Encryption",
                    icon = Icons.Default.Security,
                    onClick = onNavigateToSecurity
                )
            }

            // Group 3: Reminders
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "Reminders & Reliability",
                    subtitle = "Battery optimization & Notifications",
                    icon = Icons.Default.NotificationsActive,
                    onClick = onNavigateToNotifications
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section (Group 4)
            SettingsGroup {
                ExpressiveSettingsItem(
                    title = "View on GitHub",
                    subtitle = "github.com/isaiahscape/bedrock",
                    icon = Icons.Default.Code,
                    onClick = { uriHandler.openUri("https://github.com/isaiahscape/bedrock") },
                    showChevron = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Version Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bedrock",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A minimalist monochrome notes editor built for focused writing and local-first security.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
