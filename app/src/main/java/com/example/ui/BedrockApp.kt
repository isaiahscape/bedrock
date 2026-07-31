package com.example.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.viewmodel.NoteViewModel
import com.example.viewmodel.NoteViewModelFactory
import com.example.viewmodel.TabMode
import androidx.window.core.layout.WindowWidthSizeClass

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BedrockApp(viewModel: NoteViewModel) {
    val navController = rememberNavController()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isLargeScreen = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val openTabIds by viewModel.openTabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val tabModes by viewModel.tabModes.collectAsStateWithLifecycle()
    
    val openNotes = remember(notes, openTabIds) {
        openTabIds.mapNotNull { id ->
            if (id == 0L) {
                Note(id = 0, title = "New Note", content = "", type = "note")
            } else {
                notes.find { it.id == id }
            }
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val unlockedNoteIds by viewModel.unlockedNoteIds.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val masterPassword by viewModel.masterPassword.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userImageUri by viewModel.userImageUri.collectAsStateWithLifecycle()

    val tags by viewModel.allTags.collectAsStateWithLifecycle(initialValue = emptyList())
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle(initialValue = emptyList())

    var showSyncCenterDialog by remember { mutableStateOf(false) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var showCommandPalette by remember { mutableStateOf(false) }

    fun navigateToTabContent(noteId: Long) {
        val mode = tabModes[noteId] ?: TabMode.VIEW
        if (mode == TabMode.EDIT) {
            val note = notes.find { it.id == noteId }
            navController.navigate("note_edit/$noteId?type=${note?.type ?: "note"}")
        } else {
            navController.navigate("note_view/$noteId")
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.keyboardEvent.collect { action ->
            when (action) {
                "new_note" -> navController.navigate("note_edit/0?type=note")
                "command_palette" -> showCommandPalette = !showCommandPalette
                "focus_search" -> {
                    navController.popBackStack("note_list", inclusive = false)
                    // Focus search logic would go here
                }
                "close_tab" -> {
                    activeTabId?.let { viewModel.closeTab(it) }
                }
            }
        }
    }

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
            }
        )
    }

    SharedTransitionLayout {
        AdaptiveScaffold(
            showSidebar = isLargeScreen,
            sidebarContent = {
                NoteSidebar(
                    viewModel = viewModel,
                    notes = notes,
                    activeNoteId = activeTabId,
                    onNoteClick = { id ->
                        viewModel.openNoteInTab(id)
                        navigateToTabContent(id)
                    },
                    onCreateNote = { type ->
                        viewModel.openNoteInTab(0L, TabMode.EDIT)
                        navController.navigate("note_edit/0?type=$type")
                    },
                    onOpenSettings = { navController.navigate("settings") },
                    onOpenCommandPalette = { showCommandPalette = true }
                )
            },
            topBar = if (isLargeScreen && openNotes.isNotEmpty()) {
                {
                    WorkspaceTabs(
                        openNotes = openNotes,
                        activeNoteId = activeTabId,
                        onTabClick = { id ->
                            viewModel.switchTab(id)
                            navigateToTabContent(id)
                        },
                        onTabClose = { id -> viewModel.closeTab(id) }
                    )
                }
            } else null
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "note_list",
                modifier = Modifier.padding(innerPadding)
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
                        userName = userName,
                        userImageUri = userImageUri,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                    onNavigateToEditNote = { noteId ->
                        if (noteId == 0L) {
                            viewModel.openNoteInTab(0L, TabMode.EDIT)
                            navController.navigate("note_edit/0?type=note")
                        } else {
                            viewModel.openNoteInTab(noteId)
                            navigateToTabContent(noteId)
                        }
                    },
                        onCreateNote = { type ->
                            navController.navigate("note_edit/0?type=$type")
                        },
                        onOpenSyncCenter = { showSyncCenterDialog = true },
                        onOpenSettings = { navController.navigate("settings") },
                        onOpenRecycleBin = { showRecycleBinDialog = true }
                    )
                }

                composable(
                    route = "note_view/{noteId}",
                    arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                    LaunchedEffect(noteId) {
                        viewModel.switchTab(noteId)
                    }
                    NoteViewScreen(
                        noteId = noteId,
                        viewModel = viewModel,
                        allNotes = notes,
                        openNotes = openNotes,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBack = { navController.popBackStack() },
                        onOpenCommandPalette = { showCommandPalette = true },
                        onEditNote = { id ->
                            val note = notes.find { it.id == id }
                            viewModel.updateTabMode(id, TabMode.EDIT)
                            navController.navigate("note_edit/$id?type=${note?.type ?: "note"}")
                        },
                        onTabClick = { id ->
                            viewModel.switchTab(id)
                            navigateToTabContent(id)
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
                        openNotes = openNotes,
                        availableTags = tags,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBack = { navController.popBackStack() },
                        onOpenCommandPalette = { showCommandPalette = true },
                        onTabClick = { id ->
                            viewModel.switchTab(id)
                            navigateToTabContent(id)
                        }
                    )
                }

                composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onNavigateToAppearance = { navController.navigate("settings_appearance") },
                    onNavigateToSecurity = { navController.navigate("settings_security") },
                    onNavigateToNotifications = { navController.navigate("settings_notifications") },
                    onNavigateToSyncBackup = { navController.navigate("settings_sync_backup") },
                    onNavigateToDeveloper = { navController.navigate("settings_developer") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings_developer") {
                DeveloperSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings_sync_backup") {
                SyncBackupSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

                composable("settings_notifications") {
                    NotificationSettingsScreen(
                        viewModel = viewModel,
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
                    masterPassword = masterPassword ?: "1234",
                    onBack = { navController.popBackStack() }
                )
            }
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
