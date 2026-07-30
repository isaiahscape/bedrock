package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                        label = { Text("PIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
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
