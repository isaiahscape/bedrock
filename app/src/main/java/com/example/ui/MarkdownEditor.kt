package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
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
import com.example.data.Note
import com.example.data.Tag
import com.example.util.MarkdownContent
import com.example.util.MarkdownHelper
import com.example.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MarkdownEditor(
    noteId: Long,
    viewModel: NoteViewModel,
    existingNote: Note?,
    availableTags: List<Tag>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "# ") }
    var tagsString by remember { mutableStateOf(existingNote?.tags ?: "") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var isEncrypted by remember { mutableStateOf(existingNote?.isEncrypted ?: false) }
    var passcode by remember { mutableStateOf(existingNote?.passcodeHash ?: "1234") }

    var viewMode by remember { mutableStateOf(EditorViewMode.SPLIT) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    val currentTags = remember(tagsString) {
        if (tagsString.isBlank()) emptyList()
        else tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun saveAndBack() {
        viewModel.saveNote(
            id = noteId,
            title = title,
            content = content,
            tags = tagsString,
            isPinned = isPinned,
            isEncrypted = isEncrypted,
            passcode = passcode,
            type = "markdown"
        )
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { saveAndBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin"
                        )
                    }
                    IconButton(onClick = { showSecurityDialog = true }) {
                        Icon(
                            imageVector = if (isEncrypted) Icons.Default.Lock else Icons.Outlined.Lock,
                            contentDescription = "Security",
                            tint = if (isEncrypted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            viewMode = when (viewMode) {
                                EditorViewMode.EDIT -> EditorViewMode.PREVIEW
                                EditorViewMode.PREVIEW -> EditorViewMode.SPLIT
                                EditorViewMode.SPLIT -> EditorViewMode.EDIT
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when (viewMode) {
                                EditorViewMode.EDIT -> Icons.Default.Visibility
                                EditorViewMode.PREVIEW -> Icons.Default.VerticalSplit
                                EditorViewMode.SPLIT -> Icons.Default.Edit
                            },
                            contentDescription = "Toggle Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = "note_card_$noteId"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Title", fontSize = 22.sp) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    if (currentTags.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        ) {
                            items(currentTags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (viewMode) {
                            EditorViewMode.EDIT -> {
                                TextField(
                                    value = content,
                                    onValueChange = { content = it },
                                    placeholder = { Text("Note") },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            EditorViewMode.PREVIEW -> {
                                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    MarkdownContent(content = content) { lineIndex ->
                                        content = MarkdownHelper.toggleTodoAtLine(content, lineIndex)
                                    }
                                }
                            }
                            EditorViewMode.SPLIT -> {
                                Column {
                                    TextField(
                                        value = content,
                                        onValueChange = { content = it },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                                        MarkdownContent(content = content) { lineIndex ->
                                            content = MarkdownHelper.toggleTodoAtLine(content, lineIndex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Floating Toolbars
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(
                        visible = showActionMenu,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        ActionPopUpMenu(
                            onInsertTemplate = { showTemplateDialog = true },
                            onDeletePermanently = {
                                if (noteId != 0L) {
                                    viewModel.deleteNote(noteId)
                                    onBack()
                                } else {
                                    onBack()
                                }
                            },
                            onOpenCommandPalette = { /* Palette */ },
                            onDismiss = { showActionMenu = false }
                        )
                    }

                    if (viewMode != EditorViewMode.PREVIEW) {
                        FloatingFormattingToolbar(
                            onAction = { syntax -> content = insertFormatting(content, syntax) }
                        )
                    }

                    BreadcrumbPillToolbar(
                        onLogoClick = { showActionMenu = !showActionMenu }
                    ) {
                        IconButton(onClick = { /* Undo */ }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = Color(0xFFB0B0B0))
                        }
                        IconButton(onClick = { /* Redo */ }) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = Color(0xFFB0B0B0))
                        }
                        IconButton(onClick = { saveAndBack() }) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showTemplateDialog) {
        TemplateSelectionDialog(
            onDismiss = { showTemplateDialog = false },
            onTemplateSelected = { template ->
                content = if (content.isBlank()) template.content else "$content\n\n${template.content}"
            }
        )
    }

    if (showSecurityDialog) {
        SecurityDialog(
            isEncrypted = isEncrypted,
            onEncryptionChange = { isEncrypted = it },
            passcode = passcode,
            onPasscodeChange = { passcode = it },
            onDismiss = { showSecurityDialog = false }
        )
    }
}
