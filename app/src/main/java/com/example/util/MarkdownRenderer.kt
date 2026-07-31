package com.example.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.ImageBlock

object MarkdownHelper {

    /**
     * Toggles a checkbox in the given markdown text at line targetLineIndex.
     */
    fun toggleTodoAtLine(content: String, targetLineIndex: Int): String {
        val lines = content.lines().toMutableList()
        if (targetLineIndex < 0 || targetLineIndex >= lines.size) return content

        val line = lines[targetLineIndex]
        val trimmed = line.trimStart()
        val indent = line.substring(0, line.length - trimmed.length)

        val updatedLine = when {
            trimmed.startsWith("- [ ]") -> indent + "- [x]" + trimmed.substring(5)
            trimmed.startsWith("* [ ]") -> indent + "* [x]" + trimmed.substring(5)
            trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") -> indent + "- [ ]" + trimmed.substring(5)
            trimmed.startsWith("* [x]") || trimmed.startsWith("* [X]") -> indent + "* [ ]" + trimmed.substring(5)
            else -> line
        }

        lines[targetLineIndex] = updatedLine
        return lines.joinToString("\n")
    }

    /**
     * Parse inline formatting like **bold**, *italic*, and `code` into AnnotatedString.
     */
    fun parseInlineMarkdown(text: String, primaryColor: Color, onSurfaceColor: Color): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            val len = text.length

            while (i < len) {
                // Inline code `code`
                if (text[i] == '`') {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = onSurfaceColor.copy(alpha = 0.1f),
                                fontSize = 13.sp
                            )
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                        continue
                    }
                }

                // Bold **text**
                if (i + 1 < len && text[i] == '*' && text[i + 1] == '*') {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                        continue
                    }
                }

                // Italic *text*
                if (text[i] == '*') {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                        continue
                    }
                }

                append(text[i])
                i++
            }
        }
    }
}

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    isEditable: Boolean = false,
    onContentChange: ((String) -> Unit)? = null,
    onTodoToggle: ((lineIndex: Int) -> Unit)? = null
) {
    val lines = content.lines()
    var inCodeBlock = false
    val codeBlockLines = mutableListOf<String>()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()

            // Image Parsing: ![alt](uri){w=300}
            val imageRegex = """!\[(.*?)]\((.*?)\)(\{w=(\d+)})?""".toRegex()
            val match = imageRegex.matchEntire(trimmed)
            if (match != null) {
                val alt = match.groupValues[1]
                val uri = match.groupValues[2]
                val widthStr = match.groupValues.getOrNull(4)
                val width = widthStr?.toFloatOrNull() ?: 300f
                
                ImageBlock(
                    uri = uri,
                    initialWidth = width,
                    isEditable = isEditable,
                    onResize = { newWidth ->
                        if (onContentChange != null) {
                            val newWidthInt = newWidth.toInt()
                            val updatedLine = "![$alt]($uri){w=$newWidthInt}"
                            val updatedLines = lines.toMutableList()
                            updatedLines[index] = updatedLine
                            onContentChange(updatedLines.joinToString("\n"))
                        }
                    },
                    onDelete = {
                        if (onContentChange != null) {
                            val updatedLines = lines.toMutableList()
                            updatedLines.removeAt(index)
                            onContentChange(updatedLines.joinToString("\n"))
                        }
                    },
                    onMoveUp = {
                        if (onContentChange != null && index > 0) {
                            val updatedLines = lines.toMutableList()
                            val item = updatedLines.removeAt(index)
                            updatedLines.add(index - 1, item)
                            onContentChange(updatedLines.joinToString("\n"))
                        }
                    },
                    onMoveDown = {
                        if (onContentChange != null && index < lines.size - 1) {
                            val updatedLines = lines.toMutableList()
                            val item = updatedLines.removeAt(index)
                            updatedLines.add(index + 1, item)
                            onContentChange(updatedLines.joinToString("\n"))
                        }
                    }
                )
                return@forEachIndexed
            }

            // Code block delimiter ```
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    CodeBlock(code = codeBlockLines.joinToString("\n"))
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                return@forEachIndexed
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                return@forEachIndexed
            }

            // Horizontal Rule ---
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
                return@forEachIndexed
            }

            // Headers
            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = MarkdownHelper.parseInlineMarkdown(
                            trimmed.substring(2),
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = MarkdownHelper.parseInlineMarkdown(
                            trimmed.substring(3),
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = MarkdownHelper.parseInlineMarkdown(
                            trimmed.substring(4),
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // Checkboxes
                trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]") -> {
                    val taskText = trimmed.substring(5).trim()
                    TodoItemRow(
                        isChecked = false,
                        text = taskText,
                        onToggle = { onTodoToggle?.invoke(index) }
                    )
                }
                trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") ||
                trimmed.startsWith("* [x]") || trimmed.startsWith("* [X]") -> {
                    val taskText = trimmed.substring(5).trim()
                    TodoItemRow(
                        isChecked = true,
                        text = taskText,
                        onToggle = { onTodoToggle?.invoke(index) }
                    )
                }
                // Bullet points
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val itemText = trimmed.substring(2).trim()
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    ) {
                        Text(
                            text = "•  ",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = MarkdownHelper.parseInlineMarkdown(
                                itemText,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.onSurface
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // Blockquotes
                trimmed.startsWith("> ") -> {
                    val quoteText = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = MarkdownHelper.parseInlineMarkdown(
                                quoteText,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.onSurface
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Standard Body Paragraph
                line.isNotEmpty() -> {
                    Text(
                        text = MarkdownHelper.parseInlineMarkdown(
                            line,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Catch unclosed code block at end
        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            CodeBlock(code = codeBlockLines.joinToString("\n"))
        }
    }
}

@Composable
fun TodoItemRow(
    isChecked: Boolean,
    text: String,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onToggle() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = MarkdownHelper.parseInlineMarkdown(
                text,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onSurface
            ),
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}
