package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note

@Composable
fun WorkspaceTabs(
    openNotes: List<Note>,
    activeNoteId: Long?,
    onTabClick: (Long) -> Unit,
    onTabClose: (Long) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = openNotes.indexOfFirst { it.id == activeNoteId }.coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.background,
        divider = {},
        indicator = { tabPositions ->
            if (openNotes.isNotEmpty()) {
                val index = openNotes.indexOfFirst { it.id == activeNoteId }.coerceAtLeast(0)
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        openNotes.forEach { note ->
            val isSelected = note.id == activeNoteId
            Tab(
                selected = isSelected,
                onClick = { onTabClick(note.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title.ifEmpty { "Untitled" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onTabClose(note.id) },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
