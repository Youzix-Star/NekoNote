/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/** A task item inside a list. */
data class MdTask(val text: String, val checked: Boolean)

/** One block of a note. Inline markup stays inside [text] until a renderer resolves it. */
sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullets(val items: List<String>, val ordered: Boolean) : MdBlock
    data class Tasks(val items: List<MdTask>) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
    data object Rule : MdBlock
}

/**
 * The small Markdown dialect this app edits and draws.
 *
 * It is deliberately a block parser plus an inline span builder rather than a full CommonMark
 * implementation: enough for headings, lists, tasks, quotes, fenced code, rules, emphasis, code
 * spans and links, and no HTML, tables or reference links. Anything it does not know stays visible
 * as the text the user typed, which is the right failure mode for an editor.
 */
object Markdown {
    private val HEADING = Regex("^(#{1,6})[ \\t]+(.*)$")
    private val RULE = Regex("^[ \\t]*([-*_])([ \\t]*\\1){2,}[ \\t]*$")
    private val BULLET = Regex("^[ \\t]*[-*+][ \\t]+(.*)$")
    private val TASK = Regex("^[ \\t]*[-*+][ \\t]+\\[([ xX])\\][ \\t]+(.*)$")
    private val ORDERED = Regex("^[ \\t]*\\d{1,3}[.)][ \\t]+(.*)$")
    private val TAG = Regex("(?<![\\p{L}\\p{N}_#])#([\\p{L}\\p{N}_-]{1,32})")
    private val LINK = Regex("\\[([^\\]]*)]\\(([^)]*)\\)")
    private val WHITESPACE = Regex("\\s+")

    // ---- blocks ----

    fun parse(source: String): List<MdBlock> {
        if (source.isBlank()) return emptyList()
        val lines = source.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        val blocks = ArrayList<MdBlock>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trimEnd()
            when {
                line.isBlank() -> i++

                line.trimStart().startsWith("```") -> {
                    val language = line.trim().removePrefix("```").trim()
                    val code = StringBuilder()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        if (code.isNotEmpty()) code.append('\n')
                        code.append(lines[i])
                        i++
                    }
                    if (i < lines.size) i++
                    blocks += MdBlock.Code(language, code.toString())
                }

                HEADING.matches(line) -> {
                    val match = HEADING.find(line) ?: continue
                    blocks += MdBlock.Heading(
                        level = match.groupValues[1].length,
                        text = match.groupValues[2].trim(),
                    )
                    i++
                }

                RULE.matches(line) -> {
                    blocks += MdBlock.Rule
                    i++
                }

                isQuote(line) -> {
                    val quoted = ArrayList<String>()
                    while (i < lines.size && isQuote(lines[i])) {
                        quoted += lines[i].trimStart().removePrefix(">").trimStart()
                        i++
                    }
                    blocks += MdBlock.Quote(quoted.joinToString("\n"))
                }

                TASK.matches(line) || BULLET.matches(line) -> {
                    val tasks = ArrayList<MdTask>()
                    val items = ArrayList<String>()
                    var allTasks = true
                    while (i < lines.size) {
                        val current = lines[i].trimEnd()
                        val task = TASK.find(current)
                        if (task != null) {
                            tasks += MdTask(task.groupValues[2].trim(), task.groupValues[1].equals("x", true))
                            items += task.groupValues[2].trim()
                            i++
                        } else {
                            val bullet = BULLET.find(current) ?: break
                            allTasks = false
                            items += bullet.groupValues[1].trim()
                            i++
                        }
                    }
                    blocks += if (allTasks && tasks.isNotEmpty()) {
                        MdBlock.Tasks(tasks)
                    } else {
                        MdBlock.Bullets(items, ordered = false)
                    }
                }

                ORDERED.matches(line) -> {
                    val items = ArrayList<String>()
                    while (i < lines.size) {
                        val match = ORDERED.find(lines[i].trimEnd()) ?: break
                        items += match.groupValues[1].trim()
                        i++
                    }
                    blocks += MdBlock.Bullets(items, ordered = true)
                }

                else -> {
                    val paragraph = ArrayList<String>()
                    while (i < lines.size) {
                        val current = lines[i].trimEnd()
                        if (current.isBlank() || startsBlock(current)) break
                        paragraph += current.trim()
                        i++
                    }
                    if (paragraph.isEmpty()) {
                        i++
                    } else {
                        blocks += MdBlock.Paragraph(paragraph.joinToString(" "))
                    }
                }
            }
        }
        return blocks
    }

    private fun isQuote(line: String): Boolean = line.trimStart().startsWith(">")

    private fun startsBlock(line: String): Boolean =
        line.trimStart().startsWith("```") ||
            HEADING.matches(line) ||
            RULE.matches(line) ||
            isQuote(line) ||
            TASK.matches(line) ||
            BULLET.matches(line) ||
            ORDERED.matches(line)

    // ---- inline spans ----

    /**
     * Turns the inline markup in [text] into spans. The colours come from whichever engine is
     * drawing the page, so the same parse works in both; `#标签` gets the emphasised tag treatment
     * (a container background) when [tagBackground] is supplied.
     */
    fun inline(
        text: String,
        codeBackground: Color,
        linkColor: Color,
        tagBackground: Color = Color.Unspecified,
        tagText: Color = Color.Unspecified,
    ): AnnotatedString =
        buildAnnotatedString {
            appendInline(text, codeBackground, linkColor, tagBackground, tagText)
        }

    private fun AnnotatedString.Builder.appendInline(
        text: String,
        codeBackground: Color,
        linkColor: Color,
        tagBackground: Color,
        tagText: Color,
    ) {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end > i + 3) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                            ),
                        ) {
                            appendInline(text.substring(i + 3, end), codeBackground, linkColor, tagBackground, tagText)
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text.startsWith("**", i) || text.startsWith("__", i) -> {
                    val marker = text.substring(i, i + 2)
                    val end = text.indexOf(marker, i + 2)
                    if (end > i + 2) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            appendInline(text.substring(i + 2, end), codeBackground, linkColor, tagBackground, tagText)
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end > i + 2) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            appendInline(text.substring(i + 2, end), codeBackground, linkColor, tagBackground, tagText)
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end > i + 1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = codeBackground,
                            ),
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text[i] == '*' || text[i] == '_' -> {
                    val end = text.indexOf(text[i], i + 1)
                    if (end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            appendInline(text.substring(i + 1, end), codeBackground, linkColor, tagBackground, tagText)
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text[i] == '[' -> {
                    val closeBracket = text.indexOf(']', i + 1)
                    val hasTarget = closeBracket > i + 1 &&
                        closeBracket + 1 < text.length &&
                        text[closeBracket + 1] == '('
                    val closeParen = if (hasTarget) text.indexOf(')', closeBracket + 2) else -1
                    if (hasTarget && closeParen > closeBracket + 2) {
                        withStyle(
                            SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ) {
                            appendInline(text.substring(i + 1, closeBracket), codeBackground, linkColor, tagBackground, tagText)
                        }
                        i = closeParen + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text[i] == '#' && (i == 0 || (text[i - 1] != '#' && !isTagChar(text[i - 1]))) -> {
                    var tagEnd = i + 1
                    while (tagEnd < text.length && isTagChar(text[tagEnd])) tagEnd++
                    if (tagEnd > i + 1 && tagBackground != Color.Unspecified) {
                        withStyle(
                            SpanStyle(
                                background = tagBackground,
                                color = tagText,
                                fontWeight = FontWeight.Medium,
                            ),
                        ) {
                            append(text.substring(i, tagEnd))
                        }
                        i = tagEnd
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }

    /** The characters a `#标签` token may contain; CJK characters count as letters. */
    private fun isTagChar(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '-'

    // ---- plain text views ----

    /** Markup characters removed, for titles and excerpts. */
    fun cleanInline(text: String): String = text
        .replace(LINK) { it.groupValues[1] }
        .replace("**", "")
        .replace("__", "")
        .replace("~~", "")
        .replace("`", "")
        .replace("*", "")
        .replace("_", "")
        .trim()

    /** The first useful line of a note, used when the title field is empty. */
    fun titleOf(markdown: String): String {
        for (block in parse(markdown)) {
            val text = when (block) {
                is MdBlock.Heading -> block.text
                is MdBlock.Paragraph -> block.text
                is MdBlock.Quote -> block.text
                is MdBlock.Bullets -> block.items.firstOrNull().orEmpty()
                is MdBlock.Tasks -> block.items.firstOrNull()?.text.orEmpty()
                is MdBlock.Code -> ""
                MdBlock.Rule -> ""
            }
            val cleaned = cleanInline(text)
            if (cleaned.isNotBlank()) return cleaned.take(60)
        }
        return ""
    }

    /** The body as one readable sentence, for the list and for search. */
    fun plainText(markdown: String): String {
        val out = StringBuilder()
        fun add(text: String) {
            if (text.isBlank()) return
            if (out.isNotEmpty()) out.append(' ')
            out.append(text)
        }
        for (block in parse(markdown)) {
            when (block) {
                is MdBlock.Heading -> add(cleanInline(block.text))
                is MdBlock.Paragraph -> add(cleanInline(block.text))
                is MdBlock.Bullets -> block.items.forEach { add(cleanInline(it)) }
                is MdBlock.Tasks -> block.items.forEach { add(cleanInline(it.text)) }
                is MdBlock.Quote -> add(cleanInline(block.text))
                is MdBlock.Code -> Unit
                MdBlock.Rule -> Unit
            }
        }
        return out.toString().replace(WHITESPACE, " ").trim()
    }

    fun excerptOf(markdown: String, limit: Int = 96): String {
        val plain = plainText(markdown)
        return if (plain.length <= limit) plain else plain.take(limit).trimEnd() + "…"
    }

    /** Tags are `#标签` in the body, ignoring fenced code and headings. */
    fun tagsOf(markdown: String): List<String> {
        val tags = LinkedHashSet<String>()
        var inFence = false
        for (line in markdown.lineSequence()) {
            if (line.trimStart().startsWith("```")) {
                inFence = !inFence
                continue
            }
            if (inFence) continue
            TAG.findAll(line).forEach { match -> tags += match.groupValues[1] }
        }
        return tags.toList()
    }

    fun charCount(markdown: String): Int = markdown.count { !it.isWhitespace() }

    /** The heading outline, for the editor's contents card. */
    fun headings(markdown: String): List<Pair<Int, String>> = parse(markdown)
        .filterIsInstance<MdBlock.Heading>()
        .map { it.level to cleanInline(it.text) }
        .filter { it.second.isNotBlank() }
}
