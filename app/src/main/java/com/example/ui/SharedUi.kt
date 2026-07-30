package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

fun insertMarkdownSymbol(currentContent: String, symbol: String): String {
    return if (currentContent.endsWith("\n") || currentContent.isEmpty()) {
        currentContent + symbol
    } else {
        "$currentContent\n$symbol"
    }
}
