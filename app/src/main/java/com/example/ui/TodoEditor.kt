package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.Tag
import com.example.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TodoEditor(
    noteId: Long,
    viewModel: NoteViewModel,
    existingNote: Note?,
    availableTags: List<Tag>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onBack: () -> Unit,
    onOpenCommandPalette: () -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    
    // Initial parsing
    val initialItems = remember(existingNote) {
        val lines = existingNote?.content?.lines() ?: listOf("")
        lines.mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]")) {
                TodoItemState(text = line.substringAfter("]").trim(), isChecked = false)
            } else if (trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") ||
                       trimmed.startsWith("* [x]") || trimmed.startsWith("* [X]")) {
                TodoItemState(text = line.substringAfter("]").trim(), isChecked = true)
            } else if (trimmed.isNotEmpty()) {
                TodoItemState(text = trimmed, isChecked = false)
            } else {
                null
            }
        }.ifEmpty { listOf(TodoItemState(text = "", isChecked = false)) }
    }

    val items = remember { mutableStateListOf<TodoItemState>().apply { addAll(initialItems) } }
    
    var tagsString by remember { mutableStateOf(existingNote?.tags ?: "") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var isEncrypted by remember { mutableStateOf(existingNote?.isEncrypted ?: false) }
    var passcode by remember { mutableStateOf(existingNote?.passcodeHash ?: "1234") }
    var reminderTime by remember { mutableStateOf(existingNote?.reminderTime) }

    var showActionMenu by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }

    val currentTags = remember(tagsString) {
        if (tagsString.isBlank()) emptyList()
        else tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val context = LocalContext.current

    fun toggleTag(tagName: String) {
        val tags = if (currentTags.contains(tagName)) {
            currentTags.filter { it != tagName }
        } else {
            currentTags + tagName
        }
        tagsString = tags.joinToString(",")
    }

    fun serializeItems(): String {
        return items.joinToString("\n") { item ->
            val prefix = if (item.isChecked) "- [x] " else "- [ ] "
            prefix + item.text
        }
    }

    fun saveAndBack() {
        viewModel.saveNote(
            id = noteId,
            title = title,
            content = serializeItems(),
            tags = tagsString,
            isPinned = isPinned,
            isEncrypted = isEncrypted,
            passcode = passcode,
            type = "todo",
            reminderTime = reminderTime,
            context = context
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
                ) {
                    SelectionContainer {
                        Column {
                            // Title
                            TextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("Title", fontSize = 22.sp) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // List
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                            TodoItemRow(
                                item = item,
                                onTextChanged = { newText ->
                                    items[index] = item.copy(text = newText)
                                },
                                onCheckedChange = { checked ->
                                    items[index] = item.copy(isChecked = checked)
                                },
                                onDelete = {
                                    if (items.size > 1) items.removeAt(index)
                                },
                                onEnterPressed = {
                                    items.add(index + 1, TodoItemState(text = "", isChecked = false))
                                }
                            )
                        }

                        item {
                            Column {
                                TextButton(
                                    onClick = { items.add(TodoItemState(text = "", isChecked = false)) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add item")
                                }
                                
                                TextButton(
                                    onClick = { showTagDialog = true },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Tags", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
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
                            onOpenCommandPalette = onOpenCommandPalette,
                            onDismiss = { showActionMenu = false }
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
                val templateItems = template.content.lines().map { TodoItemState(text = it, isChecked = false) }
                items.addAll(templateItems)
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
}

data class TodoItemState(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean
)

@Composable
fun TodoItemRow(
    item: TodoItemState,
    onTextChanged: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEnterPressed: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(item.id) {
        if (item.text.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange
        )
        
        TextField(
            value = item.text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            placeholder = { Text("List item", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onEnterPressed() }
            ),
            singleLine = true
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
