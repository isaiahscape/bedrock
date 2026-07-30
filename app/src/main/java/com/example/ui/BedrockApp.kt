package com.example.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.viewmodel.NoteViewModel
import com.example.viewmodel.NoteViewModelFactory

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BedrockApp(viewModel: NoteViewModel) {
    val navController = rememberNavController()

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val unlockedNoteIds by viewModel.unlockedNoteIds.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val masterPin by viewModel.masterPin.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val tags by viewModel.allTags.collectAsStateWithLifecycle(initialValue = emptyList())
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle(initialValue = emptyList())

    var showSyncCenterDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var showCommandPalette by remember { mutableStateOf(false) }

    val commands = remember(viewModel, navController) {
        listOf(
            Command("new_text", "New Text Note", Icons.AutoMirrored.Filled.Note) {
                navController.navigate("note_edit/0?type=note")
            },
            Command("new_markdown", "New Markdown Note", Icons.Default.Code) {
                navController.navigate("note_edit/0?type=markdown")
            },
            Command("new_todo", "New To-do List", Icons.Default.Checklist) {
                navController.navigate("note_edit/0?type=todo")
            },
            Command("sync", "Sync Notes", Icons.Default.Sync) {
                viewModel.triggerSync()
            },
            Command("settings", "Open Settings", Icons.Default.Settings) {
                navController.navigate("settings")
            },
            Command("theme", "Switch Theme", Icons.Default.Brightness4) {
                val nextMode = when(themeMode) {
                    "light" -> "dark"
                    "dark" -> "auto"
                    else -> "light"
                }
                viewModel.setThemeMode(nextMode)
            },
            Command("search", "Search Notes", Icons.Default.Search) {
                navController.popBackStack("note_list", inclusive = false)
                // We could add a side effect to focus search, but for now just navigating back
            }
        )
    }

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "note_list"
        ) {
            composable("note_list") {
                NoteListScreen(
                    viewModel = viewModel,
                    notes = notes,
                    tags = tags,
                    searchQuery = searchQuery,
                    selectedTag = selectedTag,
                    unlockedNoteIds = unlockedNoteIds,
                    isOfflineMode = isOfflineMode,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onNavigateToEditNote = { noteId ->
                        if (noteId == 0L) {
                            navController.navigate("note_edit/0?type=note")
                        } else {
                            navController.navigate("note_view/$noteId")
                        }
                    },
                    onCreateNote = { type ->
                        navController.navigate("note_edit/0?type=$type")
                    },
                    onOpenSyncCenter = { showSyncCenterDialog = true },
                    onOpenSecuritySettings = { navController.navigate("settings") },
                    onOpenProfile = { showProfileDialog = true },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenRecycleBin = { showRecycleBinDialog = true }
                )
            }

            composable(
                route = "note_view/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                NoteViewScreen(
                    noteId = noteId,
                    viewModel = viewModel,
                    allNotes = notes,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onBack = { navController.popBackStack() },
                    onOpenCommandPalette = { showCommandPalette = true },
                    onEditNote = { id ->
                        val note = notes.find { it.id == id }
                        navController.navigate("note_edit/$id?type=${note?.type ?: "note"}")
                    }
                )
            }

            composable(
                route = "note_edit/{noteId}?type={type}",
                arguments = listOf(
                    navArgument("noteId") { type = NavType.LongType },
                    navArgument("type") { type = NavType.StringType; defaultValue = "note" }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                val noteType = backStackEntry.arguments?.getString("type") ?: "note"
                NoteEditScreen(
                    noteId = noteId,
                    noteType = noteType,
                    viewModel = viewModel,
                    allNotes = notes,
                    availableTags = tags,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onBack = { navController.popBackStack() },
                    onOpenCommandPalette = { showCommandPalette = true }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onNavigateToAppearance = { navController.navigate("settings_appearance") },
                    onNavigateToSecurity = { navController.navigate("settings_security") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings_appearance") {
                AppearanceSettingsScreen(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings_security") {
                SecuritySettingsScreen(
                    viewModel = viewModel,
                    masterPin = masterPin ?: "1234",
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showSyncCenterDialog) {
        SyncCenterDialog(
            viewModel = viewModel,
            isOfflineMode = isOfflineMode,
            isSyncing = isSyncing,
            syncMessage = syncMessage,
            syncLogs = syncLogs,
            onDismiss = {
                showSyncCenterDialog = false
                viewModel.clearSyncMessage()
            }
        )
    }

    if (showProfileDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { androidx.compose.material3.Text("Profile") },
            text = { androidx.compose.material3.Text("Profile page coming soon.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showProfileDialog = false }) {
                    androidx.compose.material3.Text("OK")
                }
            }
        )
    }

    if (showRecycleBinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRecycleBinDialog = false },
            title = { androidx.compose.material3.Text("Recycle Bin") },
            text = { androidx.compose.material3.Text("Deleted notes will appear here.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showRecycleBinDialog = false }) {
                    androidx.compose.material3.Text("OK")
                }
            }
        )
    }

    if (showCommandPalette) {
        CommandPalette(
            onDismiss = { showCommandPalette = false },
            commands = commands
        )
    }
}
