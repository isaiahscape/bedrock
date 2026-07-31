package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val timeState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE)
    )
    var showTimePicker by remember { mutableStateOf(false) }

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = dateState.selectedDateMillis ?: System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        onTimeSelected(calendar.timeInMillis)
                    }
                ) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Back") }
            },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timeState)
            }
        )
    }
}

enum class EditorViewMode {
    EDIT, PREVIEW, SPLIT
}

@Composable
fun SecurityDialog(
    isEncrypted: Boolean,
    onEncryptionChange: (Boolean) -> Unit,
    passcode: String,
    onPasscodeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Security") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Encrypt Note")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = isEncrypted, onCheckedChange = onEncryptionChange)
                }
                if (isEncrypted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = onPasscodeChange,
                        label = { Text("Note Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
fun ActionPopUpMenu(
    onInsertTemplate: () -> Unit,
    onDeletePermanently: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onDismiss: () -> Unit
) {
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
                onClick = {
                    onInsertTemplate()
                    onDismiss()
                }
            )
            ActionMenuItem(
                icon = Icons.Default.Terminal,
                label = "Open command palette",
                onClick = {
                    onOpenCommandPalette()
                    onDismiss()
                }
            )
            ActionMenuItem(
                icon = Icons.Default.DeleteOutline,
                label = "Move to trash",
                onClick = onDismiss
            )
            ActionMenuItem(
                icon = Icons.Default.DeleteForever,
                label = "Delete permanently",
                onClick = {
                    onDeletePermanently()
                    onDismiss()
                }
            )
            ActionMenuItem(
                icon = Icons.Default.SwapHoriz,
                label = "Open quick switcher",
                onClick = onDismiss
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

@Composable
fun BreadcrumbPillToolbar(
    onLogoClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1E1E1E),
        modifier = modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Breadcrumb Logo Trigger
            IconButton(onClick = onLogoClick) {
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

            content()
        }
    }
}

@Composable
fun FloatingFormattingToolbar(
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF1E1E1E),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormattingIconButton(label = "H1", onClick = { onAction("# ") })
            FormattingIconButton(label = "H2", onClick = { onAction("## ") })
            FormattingIconButton(icon = Icons.Default.TextFields, onClick = { /* Aa placeholder */ })
            FormattingIconButton(icon = Icons.Default.FormatBold, onClick = { onAction("**") })
            FormattingIconButton(icon = Icons.Default.FormatItalic, onClick = { onAction("*") })
            FormattingIconButton(icon = Icons.Default.FormatUnderlined, onClick = { onAction("<u>") })
            FormattingIconButton(icon = Icons.Default.FormatStrikethrough, onClick = { onAction("~~") })
            FormattingIconButton(icon = Icons.Default.Image, onClick = { onAction("image_picker") })
        }
    }
}

@Composable
private fun FormattingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String? = null,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFB0B0B0),
                modifier = Modifier.size(22.dp)
            )
        } else if (label != null) {
            Text(
                text = label,
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ToolbarButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

fun insertFormatting(content: String, syntax: String): String {
    // Basic insertion at end or start of line for now
    // In a real editor we'd handle cursor position, but for simplicity:
    return if (content.isEmpty() || content.endsWith("\n")) {
        content + syntax
    } else {
        if (syntax.startsWith("#")) {
            "$content\n$syntax"
        } else {
            // Surround or append
            if (syntax == "**" || syntax == "*" || syntax == "~~" || syntax == "<u>") {
                val closeSyntax = when(syntax) {
                    "<u>" -> "</u>"
                    else -> syntax
                }
                "$content$syntax$closeSyntax"
            } else {
                content + syntax
            }
        }
    }
}

fun insertMarkdownSymbol(currentContent: String, symbol: String): String {
    return if (currentContent.endsWith("\n") || currentContent.isEmpty()) {
        currentContent + symbol
    } else {
        "$currentContent\n$symbol"
    }
}
