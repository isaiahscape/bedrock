package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabListBottomSheet(
    openNotes: List<Note>,
    activeNoteId: Long?,
    onTabClick: (Long) -> Unit,
    onTabClose: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Open Tabs",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(24.dp)
            )

            if (openNotes.isEmpty()) {
                Text(
                    "No open tabs",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(openNotes) { note ->
                        val isSelected = note.id == activeNoteId
                        ListItem(
                            headlineContent = { Text(note.title.ifEmpty { "Untitled" }, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingContent = { Icon(Icons.Default.Description, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { onTabClose(note.id) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.clickable {
                                onTabClick(note.id)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
