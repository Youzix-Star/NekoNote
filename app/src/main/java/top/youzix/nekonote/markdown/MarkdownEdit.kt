/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.markdown

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * The editor's formatting actions, as pure functions over a [TextFieldValue].
 *
 * Keeping them out of the screen means the toolbar cannot quietly disagree with the cursor: every
 * action returns both the new text and the new selection, so a wrapped selection stays selected and
 * an inserted snippet puts the cursor where the next character belongs.
 */
object MarkdownEdit {

    /**
     * The code-fence marker, spelled with escapes rather than literal backticks: the repo's lexer
     * reads a backtick as the start of a Kotlin name and loses track of the string around it.
     */
    /** One backtick, for inline code. */
    private val TICK = "\u0060"

    private val FENCE = TICK + TICK + TICK

    fun bold(value: TextFieldValue) = wrap(value, "**", "**")

    fun italic(value: TextFieldValue) = wrap(value, "*", "*")

    fun strike(value: TextFieldValue) = wrap(value, "~~", "~~")

    fun code(value: TextFieldValue) = wrap(value, TICK, TICK)

    fun heading(value: TextFieldValue) = linePrefix(value, "# ")

    fun bullet(value: TextFieldValue) = linePrefix(value, "- ")

    fun task(value: TextFieldValue) = linePrefix(value, "- [ ] ")

    fun quote(value: TextFieldValue) = linePrefix(value, "> ")

    fun codeBlock(value: TextFieldValue) = wrap(value, FENCE + "\n", "\n" + FENCE)

    fun link(value: TextFieldValue): TextFieldValue {
        val start = minOf(value.selection.start, value.selection.end)
        val end = maxOf(value.selection.start, value.selection.end)
        if (start == end) {
            val snippet = "[](https://)"
            val text = value.text.replaceRange(start, start, snippet)
            return TextFieldValue(text = text, selection = TextRange(start + 1))
        }
        return wrap(value, "[", "](https://)")
    }

    /** Surrounds the selection with [before] and [after], keeping the selection on the text. */
    fun wrap(value: TextFieldValue, before: String, after: String): TextFieldValue {
        val start = minOf(value.selection.start, value.selection.end)
        val end = maxOf(value.selection.start, value.selection.end)
        val selected = value.text.substring(start, end)
        val replacement = before + selected + after
        val text = value.text.replaceRange(start, end, replacement)
        val selection = if (selected.isEmpty()) {
            TextRange(start + before.length)
        } else {
            TextRange(start + before.length, start + before.length + selected.length)
        }
        return TextFieldValue(text = text, selection = selection)
    }

    /** Puts [prefix] at the start of the line the selection begins on. */
    fun linePrefix(value: TextFieldValue, prefix: String): TextFieldValue {
        val start = minOf(value.selection.start, value.selection.end)
        val end = maxOf(value.selection.start, value.selection.end)
        val lineStart = value.text.substring(0, start).lastIndexOf('\n') + 1
        val text = value.text.replaceRange(lineStart, lineStart, prefix)
        val shift = prefix.length
        return value.copy(
            text = text,
            selection = TextRange(start + shift, end + shift),
        )
    }

    /** Inserts [snippet] at the cursor (or over the selection) and puts the cursor after it. */
    fun insert(value: TextFieldValue, snippet: String): TextFieldValue {
        val start = minOf(value.selection.start, value.selection.end)
        val end = maxOf(value.selection.start, value.selection.end)
        val text = value.text.replaceRange(start, end, snippet)
        return TextFieldValue(text = text, selection = TextRange(start + snippet.length))
    }
}
