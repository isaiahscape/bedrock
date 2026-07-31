package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.platform.LocalContext
import com.example.data.Note
import com.example.data.Tag
import com.example.util.MarkdownContent
import com.example.util.MarkdownHelper
import com.example.viewmodel.NoteViewModel
import com.example.viewmodel.TabMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    noteType: String,
    viewModel: NoteViewModel,
    allNotes: List<Note>,
    openNotes: List<Note>,
    availableTags: List<Tag>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onTabClick: (Long) -> Unit
) {
    val existingNote = remember(noteId, allNotes) {
        if (noteId != 0L) allNotes.find { it.id == noteId } else null
    }

    if (noteType == "markdown") {
        MarkdownEditor(
            noteId = noteId,
            viewModel = viewModel,
            existingNote = existingNote,
            availableTags = availableTags,
            openNotes = openNotes,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onBack = onBack,
            onOpenCommandPalette = onOpenCommandPalette,
            onTabClick = onTabClick
        )
        return
    }

    if (noteType == "todo") {
        TodoEditor(
            noteId = noteId,
            viewModel = viewModel,
            existingNote = existingNote,
            availableTags = availableTags,
            openNotes = openNotes,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onBack = onBack,
            onOpenCommandPalette = onOpenCommandPalette,
            onTabClick = onTabClick
        )
        return
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var tagsString by remember { mutableStateOf(existingNote?.tags ?: "") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var isEncrypted by remember { mutableStateOf(existingNote?.isEncrypted ?: false) }
    var passcode by remember { mutableStateOf(existingNote?.passcodeHash ?: "1234") }
    var reminderTime by remember { mutableStateOf(existingNote?.reminderTime) }

    var viewMode by remember { mutableStateOf(EditorViewMode.EDIT) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showTabList by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }

    val androidContext = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val imageMarkdown = "\n![Image]($it){w=300}\n"
            content += imageMarkdown
        }
    }

    val currentTags = remember(tagsString) {
        if (tagsString.isBlank()) emptyList()
        else tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun toggleTag(tagName: String) {
        val tags = if (currentTags.contains(tagName)) {
            currentTags.filter { it != tagName }
        } else {
            currentTags + tagName
        }
        tagsString = tags.joinToString(",")
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
            type = "note",
            reminderTime = reminderTime,
            context = androidContext
        )
        if (noteId != 0L) {
            viewModel.updateTabMode(noteId, TabMode.VIEW)
        }
        onBack()
    }

    LaunchedEffect(viewModel) {
        viewModel.keyboardEvent.collect { action ->
            val syntax = when (action) {
                "bold" -> "**"
                "italic" -> "*"
                "underline" -> "<u>"
                "save_note" -> {
                    saveAndBack()
                    null
                }
                else -> null
            }
            syntax?.let { 
                content = insertFormatting(content, it)
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            isSearchActive = false
                            localSearchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Search")
                        }
                        
                        TextField(
                            value = localSearchQuery,
                            onValueChange = { localSearchQuery = it },
                            placeholder = { Text("Search in note...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        
                        if (localSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { localSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                }
            } else {
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
                                    else -> EditorViewMode.EDIT
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (viewMode == EditorViewMode.EDIT) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = "Toggle Preview"
                            )
                        }
                        IconButton(onClick = { showReminderPicker = true }) {
                            Icon(
                                imageVector = if (reminderTime != null) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = "Set Reminder",
                                tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
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
                    SelectionContainer {
                        Column {
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
                                    .padding(horizontal = 16.dp),
                                visualTransformation = remember(localSearchQuery) { SearchHighlightTransformation(localSearchQuery) }
                            )

                            if (currentTags.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                ) {
                                    items(currentTags) { tag ->
                                        Surface(
                                            onClick = { toggleTag(tag) },
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (viewMode == EditorViewMode.EDIT) {
                                TextField(
                                    value = content,
                                    onValueChange = { content = it },
                                    placeholder = { Text("Note") },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    visualTransformation = remember(localSearchQuery) { SearchHighlightTransformation(localSearchQuery) }
                                )
                            } else {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    MarkdownContent(
                                        content = content,
                                        isEditable = true,
                                        searchQuery = localSearchQuery,
                                        onContentChange = { content = it }
                                    ) { lineIndex ->
                                        content = MarkdownHelper.toggleTodoAtLine(content, lineIndex)
                                    }
                                }
                            }
                        }
                    }

                    reminderTime?.let { time ->
                        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        InputChip(
                            selected = true,
                            onClick = { reminderTime = null },
                            label = { Text("Reminder: ${sdf.format(Date(time))}") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                    
                    TextButton(
                        onClick = { showTagDialog = true },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Tags", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.height(120.dp))
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
                            onMoveToTrash = {
                                if (noteId != 0L) {
                                    viewModel.moveToTrash(noteId)
                                    onBack()
                                } else {
                                    onBack()
                                }
                            },
                            onDeletePermanently = {
                                if (noteId != 0L) {
                                    viewModel.deleteNote(noteId)
                                    onBack()
                                } else {
                                    onBack()
                                }
                            },
                            onOpenCommandPalette = onOpenCommandPalette,
                            onDismiss = { showActionMenu = false }
                        )
                    }

                    FloatingFormattingToolbar(
                        onAction = { syntax ->
                            if (syntax == "image_picker") {
                                imageLauncher.launch("image/*")
                            } else {
                                content = insertFormatting(content, syntax)
                            }
                        }
                    )

                    BreadcrumbPillToolbar(
                        onLogoClick = { showActionMenu = !showActionMenu }
                    ) {
                        IconButton(onClick = { /* Undo */ }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { /* Redo */ }) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { showTabList = true }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = openNotes.size.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { saveAndBack() }) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showTagDialog) {
        TagSelectionDialog(
            availableTags = availableTags,
            selectedTags = currentTags,
            onTagToggled = { toggleTag(it) },
            onAddGlobalTag = { viewModel.addTag(it) },
            onDismiss = { showTagDialog = false }
        )
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

    if (showReminderPicker) {
        ReminderPickerDialog(
            onDismiss = { showReminderPicker = false },
            onTimeSelected = { time ->
                reminderTime = time
                showReminderPicker = false
            }
        )
    }

    if (showTabList) {
        TabListBottomSheet(
            openNotes = openNotes,
            activeNoteId = noteId,
            onTabClick = onTabClick,
            onTabClose = { id -> viewModel.closeTab(id) },
            onDismiss = { showTabList = false }
        )
    }
}
