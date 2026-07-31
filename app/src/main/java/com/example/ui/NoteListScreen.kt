package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Note
import com.example.data.Tag
import com.example.util.MarkdownContent
import com.example.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    notes: List<Note>,
    tags: List<Tag>,
    searchQuery: String,
    selectedTag: String?,
    unlockedNoteIds: Set<Long>,
    isOfflineMode: Boolean,
    userName: String,
    userImageUri: String?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateToEditNote: (Long) -> Unit,
    onCreateNote: (String) -> Unit,
    onOpenSyncCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecycleBin: () -> Unit
) {
    var showUnlockDialogForNote by remember { mutableStateOf<Note?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    
    var showProfileHub by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    var fabExpanded by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(targetValue = if (fabExpanded) 45f else 0f)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Notes",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onOpenSyncCenter
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = "Sync",
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { showProfileHub = true }
                        ) {
                            if (userImageUri != null) {
                                AsyncImage(
                                    model = userImageUri,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.padding(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
                ) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Text Note", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 14.sp)
                            }
                            SmallFloatingActionButton(
                                onClick = { 
                                    fabExpanded = false
                                    onCreateNote("note")
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Note, contentDescription = "Text Note")
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Markdown", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 14.sp)
                            }
                            SmallFloatingActionButton(
                                onClick = { 
                                    fabExpanded = false
                                    onCreateNote("markdown")
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Code, contentDescription = "Markdown")
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("To-do List", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 14.sp)
                            }
                            SmallFloatingActionButton(
                                onClick = { 
                                    fabExpanded = false
                                    onCreateNote("todo")
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Checklist, contentDescription = "To-do List")
                            }
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("create_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Note",
                        modifier = Modifier.rotate(fabRotation)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search your notes") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("search_notes_input")
            )

            // Tags Filter Row
            if (tags.isNotEmpty() || selectedTag != null) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.setSelectedTag(null) },
                            label = { Text("All Notes") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag.name,
                            onClick = {
                                viewModel.setSelectedTag(if (selectedTag == tag.name) null else tag.name)
                            },
                            label = { Text("#${tag.name}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    item {
                        AssistChip(
                            onClick = { showAddTagDialog = true },
                            label = { Text("+ Tag") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tag", modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            // Notes List
            if (notes.isEmpty()) {
                EmptyNotesState(
                    hasFilter = searchQuery.isNotEmpty() || selectedTag != null,
                    onCreateNote = { onNavigateToEditNote(0L) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCardItem(
                            note = note,
                            isUnlocked = !note.isEncrypted || unlockedNoteIds.contains(note.id),
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onNoteClick = {
                                if (note.isEncrypted && !unlockedNoteIds.contains(note.id)) {
                                    showUnlockDialogForNote = note
                                    pinInput = ""
                                    pinError = false
                                } else {
                                    // Navigate to view/edit with type
                                    onNavigateToEditNote(note.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Unlock Encrypted Note Dialog
    showUnlockDialogForNote?.let { note ->
        AlertDialog(
            onDismissRequest = { showUnlockDialogForNote = null },
            icon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock") },
            title = { Text("Encrypted Note Lock") },
            text = {
                Column {
                    Text("This note is protected with local encryption. Enter your Master Password to view:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        label = { Text("Master Password") },
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text(
                            text = "Incorrect password. Please try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val success = viewModel.unlockNote(note.id, pinInput, note)
                        if (success) {
                            showUnlockDialogForNote = null
                            onNavigateToEditNote(note.id)
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialogForNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Tag Dialog
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("Create New Tag") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("Tag Name (e.g. Work, Ideas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            viewModel.addTag(newTagName.trim())
                            newTagName = ""
                            showAddTagDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showProfileHub) {
        ModalBottomSheet(
            onDismissRequest = { showProfileHub = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            ProfileHubContent(
                viewModel = viewModel,
                userName = userName,
                userImageUri = userImageUri,
                onNavigateToSettings = {
                    showProfileHub = false
                    onOpenSettings()
                },
                onOpenRecycleBin = {
                    showProfileHub = false
                    onOpenRecycleBin()
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NoteCardItem(
    note: Note,
    isUnlocked: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNoteClick: () -> Unit
) {
    val (completedTodos, totalTodos) = remember(note.content) { note.getTodoStats() }
    val isEncryptedLocked = note.isEncrypted && !isUnlocked
    val isChecklist = totalTodos > 0 && !isEncryptedLocked

    with(sharedTransitionScope) {
        if (isEncryptedLocked) {
            // Style 3: Encrypted Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        rememberSharedContentState(key = "note_card_${note.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onNoteClick
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = note.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (note.reminderTime != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Reminder set",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFC09945) // Goldish lock color based on screenshot
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Encrypted locally • Synced 2m ago",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isChecklist) {
            // Style 2: Checklist Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        rememberSharedContentState(key = "note_card_${note.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onNoteClick
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (note.reminderTime != null) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder set",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Extract top 2 checklist items
                    val lines = note.content.lines().filter { it.trim().startsWith("- [") || it.trim().startsWith("* [") }.take(2)
                    lines.forEach { line ->
                        val isChecked = line.contains("[x]", ignoreCase = true)
                        val text = line.substringAfter("]").trim()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Style 1: Dark Markdown Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        rememberSharedContentState(key = "note_card_${note.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1E1E), // Dark background
                contentColor = Color.White,
                onClick = onNoteClick
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (note.reminderTime != null) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder set",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF333333),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "MARKDOWN",
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = note.content.take(100).replace("\n", " "),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    val tagList = note.getTagList()
                    if (tagList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tagList.take(3).forEach { tag ->
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF333333),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotesState(
    hasFilter: Boolean,
    onCreateNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (hasFilter) Icons.Default.SearchOff else Icons.Default.NoteAdd,
                contentDescription = "Empty Notes",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasFilter) "No notes match your filter" else "No Notes Yet",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasFilter)
                    "Try clearing your search query or tag filter."
                else
                    "Create your first monochrome markdown note with tags and to-dos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (!hasFilter) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onCreateNote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create First Note")
                }
            }
        }
    }
}
