package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.Note
import com.example.util.MarkdownContent
import com.example.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NoteViewScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    allNotes: List<Note>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onEditNote: (Long) -> Unit
) {
    val context = LocalContext.current
    val note = remember(noteId, allNotes) {
        allNotes.find { it.id == noteId }
    }

    var showActionMenu by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (note != null) {
                        IconButton(onClick = { viewModel.togglePin(note) }) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .sharedElement(
                        rememberSharedContentState(key = "note_card_$noteId"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            ) {
                if (note != null) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .padding(bottom = 100.dp) // space for bottom bar
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (note.tags.isNotBlank()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    note.getTagList().forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = null
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            MarkdownContent(
                                content = note.content,
                                isEditable = true, // Allow resizing in view mode
                                onContentChange = { newContent ->
                                    viewModel.saveNote(
                                        id = note.id,
                                        title = note.title,
                                        content = newContent,
                                        tags = note.tags,
                                        isPinned = note.isPinned,
                                        isEncrypted = note.isEncrypted,
                                        passcode = note.passcodeHash,
                                        type = note.type,
                                        reminderTime = note.reminderTime,
                                        context = context
                                    )
                                },
                                onTodoToggle = { lineIndex ->
                                    viewModel.toggleTodoItem(note, lineIndex)
                                }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Note not found")
                    }
                }

                // Action Menu and Toolbar Container
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action Menu (Slide-up)
                    AnimatedVisibility(
                        visible = showActionMenu,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        ActionPopUpMenu(
                            onInsertTemplate = { showTemplateDialog = true },
                            onDeletePermanently = {
                                viewModel.deleteNote(noteId)
                                onBack()
                            },
                            onOpenCommandPalette = onOpenCommandPalette,
                            onDismiss = { showActionMenu = false }
                        )
                    }

                    // Pill Toolbar
                    BreadcrumbPillToolbar(
                        onLogoClick = { showActionMenu = !showActionMenu }
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* Forward */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Forward",
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* Search */ }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { onEditNote(noteId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Note",
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { /* Tabs */ }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(1.5.dp, Color(0xFFB0B0B0), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB0B0B0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTemplateDialog && note != null) {
        TemplateSelectionDialog(
            onDismiss = { showTemplateDialog = false },
            onTemplateSelected = { template ->
                val newContent = if (note.content.isBlank()) {
                    template.content
                } else {
                    "${note.content}\n\n${template.content}"
                }
                viewModel.saveNote(
                    id = note.id,
                    title = note.title,
                    content = newContent,
                    tags = note.tags,
                    isPinned = note.isPinned,
                    isEncrypted = note.isEncrypted,
                    passcode = note.passcodeHash,
                    type = note.type,
                    reminderTime = note.reminderTime,
                    context = context
                )
            }
        )
    }
}
