package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
    onEditNote: (Long) -> Unit
) {
    val note = remember(noteId, allNotes) {
        allNotes.find { it.id == noteId }
    }

    var showActionMenu by remember { mutableStateOf(false) }

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
                            onTodoToggle = { lineIndex ->
                                viewModel.toggleTodoItem(note, lineIndex)
                            }
                        )
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
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Action Menu (Slide-up)
                    AnimatedVisibility(
                        visible = showActionMenu,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        ActionPopUpMenu(
                            onAction = { showActionMenu = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pill Toolbar
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Breadcrumb Logo Trigger
                            IconButton(onClick = { showActionMenu = !showActionMenu }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "Actions",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            VerticalDivider(
                                modifier = Modifier.height(24.dp).width(1.dp),
                                color = Color(0xFF333333)
                            )

                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back",
                                    tint = Color(0xFFB0B0B0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { /* Forward - placeholder */ }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Forward",
                                    tint = Color(0xFFB0B0B0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { /* Search - placeholder */ }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFFB0B0B0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { onEditNote(noteId) }) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Edit Note",
                                    tint = Color(0xFFB0B0B0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { /* Tabs - placeholder */ }) {
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
    }
}

@Composable
fun ActionPopUpMenu(onAction: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E1E1E),
        modifier = Modifier.wrapContentWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ActionMenuItem(
                icon = Icons.Default.PostAdd,
                label = "Insert template",
                onClick = onAction
            )
            ActionMenuItem(
                icon = Icons.Default.Terminal,
                label = "Open command palette",
                onClick = onAction
            )
            ActionMenuItem(
                icon = Icons.Default.DeleteOutline,
                label = "Move to trash",
                onClick = onAction
            )
            ActionMenuItem(
                icon = Icons.Default.DeleteForever,
                label = "Delete permanently",
                onClick = onAction
            )
            ActionMenuItem(
                icon = Icons.Default.SwapHoriz,
                label = "Open quick switcher",
                onClick = onAction
            )
        }
    }
}

@Composable
fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFB0B0B0),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = Color(0xFFB0B0B0),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
