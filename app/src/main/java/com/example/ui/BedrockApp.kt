package com.example.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
                onBack = { navController.popBackStack() },
                onEditNote = { id ->
                    navController.navigate("note_edit/$id?type=note")
                }
            )
        }

        composable(
            route = "note_edit/{noteId}?type={noteType}",
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
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                themeMode = themeMode,
                masterPin = masterPin ?: "1234",
                onBack = { navController.popBackStack() }
            )
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
}
