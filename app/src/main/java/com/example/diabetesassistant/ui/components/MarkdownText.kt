package com.example.diabetesassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diabetesassistant.ui.theme.HealthPalette

/**
 * 现代原生 Jetpack Compose Markdown 富文本渲染器。
 * 支持标题（# / ## / ###）、加粗（**text**）、斜体（*text*）、行内代码（`code`）、
 * 有序列表（1. ）、无序列表（- / *）、引用区块（> ）及分割线（---）。
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = HealthPalette.textPrimary(),
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 22.sp,
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val headingSize = when (block.level) {
                        1 -> (fontSize.value + 4f).sp
                        2 -> (fontSize.value + 2.5f).sp
                        else -> (fontSize.value + 1.2f).sp
                    }
                    Text(
                        text = buildInlineMarkdown(block.text, color),
                        fontSize = headingSize,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        lineHeight = (headingSize.value * 1.35f).sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildInlineMarkdown(block.text, color),
                        fontSize = fontSize,
                        color = color,
                        lineHeight = lineHeight,
                    )
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 8.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(HealthPalette.Secondary),
                        )
                        Text(
                            text = buildInlineMarkdown(block.text, color),
                            fontSize = fontSize,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${block.number}.",
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = HealthPalette.Secondary,
                            lineHeight = lineHeight,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(
                            text = buildInlineMarkdown(block.text, color),
                            fontSize = fontSize,
                            color = color,
                            lineHeight = lineHeight,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HealthPalette.chipSurface().copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(HealthPalette.Secondary),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildInlineMarkdown(block.text, color),
                            fontSize = (fontSize.value - 0.5f).sp,
                            fontStyle = FontStyle.Italic,
                            color = HealthPalette.textSecondary(),
                            lineHeight = lineHeight,
                        )
                    }
                }

                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        color = HealthPalette.chipBorder(),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

private fun parseMarkdownBlocks(rawMarkdown: String): List<MarkdownBlock> {
    if (rawMarkdown.isBlank()) return emptyList()

    val lines = rawMarkdown.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var paragraphBuffer = StringBuilder()

    fun flushParagraph() {
        if (paragraphBuffer.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(paragraphBuffer.toString().trim()))
            paragraphBuffer = StringBuilder()
        }
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            flushParagraph()
            continue
        }

        // 分隔线 --- / ***
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            flushParagraph()
            blocks.add(MarkdownBlock.Divider)
            continue
        }

        // 标题 # / ## / ###
        if (trimmed.startsWith("#")) {
            flushParagraph()
            val level = trimmed.takeWhile { it == '#' }.length
            val headingText = trimmed.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level.coerceIn(1, 4), headingText))
            continue
        }

        // 引用块 >
        if (trimmed.startsWith(">")) {
            flushParagraph()
            blocks.add(MarkdownBlock.Blockquote(trimmed.removePrefix(">").trim()))
            continue
        }

        // 无序列表 - / * / •
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
            flushParagraph()
            val itemText = trimmed.drop(2).trim()
            blocks.add(MarkdownBlock.BulletItem(itemText))
            continue
        }

        // 有序列表 1. / 2.
        val numberedMatch = Regex("""^(\d+)\.\s+(.*)$""").find(trimmed)
        if (numberedMatch != null) {
            flushParagraph()
            val num = numberedMatch.groupValues[1]
            val itemText = numberedMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, itemText))
            continue
        }

        // 常规段落内容累加
        if (paragraphBuffer.isNotEmpty()) {
            paragraphBuffer.append("\n")
        }
        paragraphBuffer.append(trimmed)
    }

    flushParagraph()
    return blocks
}

/**
 * 将行内的 **加粗**、*斜体* 与 `行内代码` 解析为 AnnotatedString
 */
private fun buildInlineMarkdown(text: String, primaryColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val len = text.length

        while (cursor < len) {
            // 加粗 **text** 或 __text__
            if (cursor + 1 < len && ((text[cursor] == '*' && text[cursor + 1] == '*') || (text[cursor] == '_' && text[cursor + 1] == '_'))) {
                val delim = text.substring(cursor, cursor + 2)
                val closingIdx = text.indexOf(delim, startIndex = cursor + 2)
                if (closingIdx != -1) {
                    val boldContent = text.substring(cursor + 2, closingIdx)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor))
                    append(boldContent)
                    pop()
                    cursor = closingIdx + 2
                    continue
                }
            }

            // 行内代码 `code`
            if (text[cursor] == '`') {
                val closingIdx = text.indexOf('`', startIndex = cursor + 1)
                if (closingIdx != -1) {
                    val codeContent = text.substring(cursor + 1, closingIdx)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor,
                            background = Color.Black.copy(alpha = 0.06f),
                        )
                    )
                    append(codeContent)
                    pop()
                    cursor = closingIdx + 1
                    continue
                }
            }

            // 斜体 *text*
            if (text[cursor] == '*' && (cursor == 0 || text[cursor - 1] != '*')) {
                val closingIdx = text.indexOf('*', startIndex = cursor + 1)
                if (closingIdx != -1 && (closingIdx + 1 >= len || text[closingIdx + 1] != '*')) {
                    val italicContent = text.substring(cursor + 1, closingIdx)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(italicContent)
                    pop()
                    cursor = closingIdx + 1
                    continue
                }
            }

            // 普通字符
            append(text[cursor])
            cursor++
        }
    }
}
