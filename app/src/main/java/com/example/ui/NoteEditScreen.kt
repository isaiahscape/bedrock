package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import com.example.data.Tag
import com.example.util.MarkdownContent
import com.example.util.MarkdownHelper
import com.example.viewmodel.NoteViewModel

enum class EditorViewMode {
    EDIT, PREVIEW, SPLIT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    noteType: String,
    viewModel: NoteViewModel,
    allNotes: List<Note>,
    availableTags: List<Tag>,
    onBack: () -> Unit
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
            onBack = onBack
        )
        return
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember {
        mutableStateOf(
            existingNote?.content ?: if (noteType == "todo") "- [ ] " else ""
        )
    }
    var tagsString by remember { mutableStateOf(existingNote?.tags ?: "") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var isEncrypted by remember { mutableStateOf(existingNote?.isEncrypted ?: false) }
    var passcode by remember { mutableStateOf(existingNote?.passcodeHash ?: "1234") }

    var showSecurityDialog by remember { mutableStateOf(false) }

    val currentTags = remember(tagsString) {
        if (tagsString.isBlank()) emptyList()
        else tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveNote(
                                id = noteId,
                                title = title,
                                content = content,
                                tags = tagsString,
                                isPinned = isPinned,
                                isEncrypted = isEncrypted,
                                passcode = passcode
                            )
                            onBack()
                        }
                    ) {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Field
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
                        .padding(horizontal = 16.dp) // Adjusted for alignment
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

                // Content Field
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
                        .padding(horizontal = 16.dp) // Adjusted for alignment
                )
                
                Spacer(modifier = Modifier.height(80.dp)) // Space for floating toolbar
                
                TextButton(
                    onClick = { /* Tag picker */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Tags", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Floating Toolbar
            FloatingFormattingToolbar(
                onAction = { syntax -> content = insertFormatting(content, syntax) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }
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
