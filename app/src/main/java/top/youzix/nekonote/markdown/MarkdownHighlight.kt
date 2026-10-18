/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration

/**
 * The palette the source editor paints Markdown syntax with.
 *
 * Each engine builds its own instance from its own theme, exactly like the preview renderers: the
 * highlighting rules are shared, the colours are not.
 */
data class MarkdownHighlightColors(
    val syntax: Color,
    val heading: Color,
    val codeBackground: Color,
    val codeText: Color,
    val tagBackground: Color,
    val tagText: Color,
    val link: Color,
    val muted: Color,
)

/**
 * Styles the source without changing it.
 *
 * Only span styles are added, so the cursor and selection map one to one onto the original text
 * ([OffsetMapping.Identity]). Markdown syntax stays visible and editable — this is live colouring,
 * not a live preview — but the markers, headings, tags, links, code and tasks read the way a
 * Markdown editor is expected to read.
 */
class MarkdownVisualTransformation(
    private val colors: MarkdownHighlightColors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(highlightMarkdown(text.text, colors), OffsetMapping.Identity)
}

private val HEADING = Regex("^(#{1,6})[ \\t]+(.*)$")
private val RULE = Regex("^[ \\t]*([-*_])([ \\t]*\\1){2,}[ \\t]*$")
private val TASK = Regex("^([-*+])[ \\t]+\\[([ xX])\\](?:[ \\t]+(.*))?$")
private val BULLET = Regex("^([-*+])[ \\t]+")
private val ORDERED = Regex("^(\\d{1,3}[.)])[ \\t]+")

/** Paints [source] line by line; fences get monospace, headings get bold, everything else inline. */
fun highlightMarkdown(source: String, colors: MarkdownHighlightColors): AnnotatedString {
    val builder = AnnotatedString.Builder(source)
    if (source.isEmpty()) return builder.toAnnotatedString()

    val syntax = SpanStyle(color = colors.syntax, fontWeight = FontWeight.Medium)
    val codeLine = SpanStyle(
        fontFamily = FontFamily.Monospace,
        color = colors.codeText,
        background = colors.codeBackground,
    )

    var inCode = false
    var lineStart = 0
    while (lineStart < source.length) {
        val newline = source.indexOf('\n', lineStart)
        val lineEnd = if (newline < 0) source.length else newline
        val line = source.substring(lineStart, lineEnd)
        val trimmed = line.trimStart()
        val base = lineStart + (line.length - trimmed.length)

        if (trimmed.isNotEmpty()) {
            when {
                trimmed.startsWith("```") -> {
                    builder.mark(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = colors.syntax,
                            background = colors.codeBackground,
                        ),
                        base,
                        lineEnd,
                    )
                    inCode = !inCode
                }

                inCode -> builder.mark(codeLine, lineStart, lineEnd)

                RULE.matches(trimmed) -> builder.mark(syntax, base, lineEnd)

                else -> highlightLine(builder, source, base, lineEnd, trimmed, colors, syntax)
            }
        }
        lineStart = lineEnd + 1
    }
    return builder.toAnnotatedString()
}

private fun highlightLine(
    builder: AnnotatedString.Builder,
    source: String,
    start: Int,
    end: Int,
    line: String,
    colors: MarkdownHighlightColors,
    syntax: SpanStyle,
) {
    val heading = HEADING.find(line)
    if (heading != null) {
        val hashes = heading.groups[1]!!
        builder.mark(
            SpanStyle(color = colors.syntax, fontWeight = FontWeight.Bold),
            start + hashes.range.first,
            start + hashes.range.last + 1,
        )
        val content = heading.groups[2]
        if (content != null && content.value.isNotEmpty()) {
            val contentStart = start + content.range.first
            builder.mark(SpanStyle(color = colors.heading, fontWeight = FontWeight.Bold), contentStart, end)
            styleInline(builder, source, contentStart, end, colors)
        }
        return
    }

    val task = TASK.find(line)
    if (task != null) {
        val bullet = task.groups[1]!!
        builder.mark(syntax, start + bullet.range.first, start + bullet.range.last + 1)
        val check = task.groups[2]!!
        val checked = check.value.equals("x", ignoreCase = true)
        builder.mark(
            SpanStyle(
                color = if (checked) colors.syntax else colors.muted,
                fontWeight = FontWeight.Bold,
            ),
            start + check.range.first - 1,
            start + check.range.last + 2,
        )
        val body = task.groups[3]
        if (body != null && body.value.isNotEmpty()) {
            val bodyStart = start + body.range.first
            if (checked) {
                builder.mark(
                    SpanStyle(color = colors.muted, textDecoration = TextDecoration.LineThrough),
                    bodyStart,
                    end,
                )
            }
            styleInline(builder, source, bodyStart, end, colors)
        }
        return
    }

    val bullet = BULLET.find(line)
    if (bullet != null) {
        val marker = bullet.groups[1]!!
        builder.mark(syntax, start + marker.range.first, start + marker.range.last + 1)
        styleInline(builder, source, start + bullet.value.length, end, colors)
        return
    }

    val ordered = ORDERED.find(line)
    if (ordered != null) {
        val marker = ordered.groups[1]!!
        builder.mark(syntax, start + marker.range.first, start + marker.range.last + 1)
        styleInline(builder, source, start + ordered.value.length, end, colors)
        return
    }

    if (line.startsWith(">")) {
        var markerEnd = 0
        while (markerEnd < line.length && (line[markerEnd] == '>' || line[markerEnd] == ' ' || line[markerEnd] == '\t')) {
            markerEnd++
        }
        builder.mark(syntax, start, start + markerEnd)
        styleInline(builder, source, start + markerEnd, end, colors)
        return
    }

    styleInline(builder, source, start, end, colors)
}

/** Inline spans inside one line, or inside a heading/task/link label. */
private fun styleInline(
    builder: AnnotatedString.Builder,
    text: String,
    from: Int,
    to: Int,
    colors: MarkdownHighlightColors,
) {
    if (from >= to) return
    val syntax = SpanStyle(color = colors.syntax, fontWeight = FontWeight.Medium)
    var i = from
    while (i < to) {
        when {
            text.startsWith("**", i) || text.startsWith("__", i) -> {
                val marker = text.substring(i, i + 2)
                val close = text.indexOf(marker, i + 2)
                if (close > i + 2 && close + 2 <= to) {
                    builder.mark(syntax, i, i + 2)
                    builder.mark(SpanStyle(fontWeight = FontWeight.Bold), i + 2, close)
                    styleInline(builder, text, i + 2, close, colors)
                    builder.mark(syntax, close, close + 2)
                    i = close + 2
                } else {
                    i++
                }
            }

            text.startsWith("~~", i) -> {
                val close = text.indexOf("~~", i + 2)
                if (close > i + 2 && close + 2 <= to) {
                    builder.mark(syntax, i, i + 2)
                    builder.mark(SpanStyle(textDecoration = TextDecoration.LineThrough), i + 2, close)
                    styleInline(builder, text, i + 2, close, colors)
                    builder.mark(syntax, close, close + 2)
                    i = close + 2
                } else {
                    i++
                }
            }

            text[i] == '`' -> {
                val close = text.indexOf('`', i + 1)
                if (close > i + 1 && close + 1 <= to) {
                    builder.mark(syntax, i, i + 1)
                    builder.mark(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = colors.codeBackground,
                            color = colors.codeText,
                        ),
                        i + 1,
                        close,
                    )
                    builder.mark(syntax, close, close + 1)
                    i = close + 1
                } else {
                    i++
                }
            }

            text[i] == '*' || text[i] == '_' -> {
                val marker = text[i]
                val close = text.indexOf(marker, i + 1)
                if (close > i + 1 && close + 1 <= to) {
                    builder.mark(syntax, i, i + 1)
                    builder.mark(SpanStyle(fontStyle = FontStyle.Italic), i + 1, close)
                    styleInline(builder, text, i + 1, close, colors)
                    builder.mark(syntax, close, close + 1)
                    i = close + 1
                } else {
                    i++
                }
            }

            text[i] == '[' -> {
                val closeBracket = text.indexOf(']', i + 1)
                val hasTarget = closeBracket > i + 1 &&
                    closeBracket + 1 < to &&
                    text[closeBracket + 1] == '('
                val closeParen = if (hasTarget) text.indexOf(')', closeBracket + 2) else -1
                if (hasTarget && closeParen > closeBracket + 2 && closeParen + 1 <= to) {
                    builder.mark(syntax, i, i + 1)
                    builder.mark(
                        SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
                        i + 1,
                        closeBracket,
                    )
                    styleInline(builder, text, i + 1, closeBracket, colors)
                    builder.mark(syntax, closeBracket, closeBracket + 2)
                    builder.mark(SpanStyle(color = colors.muted), closeBracket + 2, closeParen)
                    builder.mark(syntax, closeParen, closeParen + 1)
                    i = closeParen + 1
                } else {
                    i++
                }
            }

            text[i] == '#' -> {
                val previous = if (i > from) text[i - 1] else '\n'
                if (previous != '#' && !isTagChar(previous)) {
                    var tagEnd = i + 1
                    while (tagEnd < to && isTagChar(text[tagEnd])) tagEnd++
                    if (tagEnd > i + 1) {
                        builder.mark(
                            SpanStyle(
                                color = colors.tagText,
                                background = colors.tagBackground,
                                fontWeight = FontWeight.Medium,
                            ),
                            i,
                            tagEnd,
                        )
                        i = tagEnd
                    } else {
                        i++
                    }
                } else {
                    i++
                }
            }

            else -> i++
        }
    }
}

private fun isTagChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '-'

private fun AnnotatedString.Builder.mark(style: SpanStyle, start: Int, end: Int) {
    if (start < end) addStyle(style, start, end)
}
