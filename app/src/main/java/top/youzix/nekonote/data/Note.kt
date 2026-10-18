/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.data

import top.youzix.nekonote.markdown.Markdown

/**
 * One note.
 *
 * [body] is the Markdown source, the title is a separate field, and [tags] is an explicit list
 * edited on its own line under the title. Tags used to be read out of the body; old files still
 * migrate that way (see the repository's decoder), but new edits write the list directly.
 */
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val pinned: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    /** What the list shows: the title, or the first line of the body, or a placeholder. */
    val displayTitle: String
        get() = title.ifBlank { Markdown.titleOf(body) }.ifBlank { "未命名笔记" }

    /** A few lines of the body with the markup taken out, for the list. */
    val excerpt: String get() = Markdown.excerptOf(body)

    /** Chinese text is measured in characters; whitespace does not count. */
    val charCount: Int get() = Markdown.charCount(body)

    /** A draft that has never been typed into. */
    val isBlank: Boolean get() = title.isBlank() && body.isBlank() && tags.isEmpty()
}

/** How the note list is ordered below the pinned notes. */
enum class NoteSort(val label: String) {
    Updated("最近更新"),
    Created("创建时间"),
    Title("标题"),
    ;

    companion object {
        fun from(name: String?): NoteSort = entries.firstOrNull { it.name == name } ?: Updated
    }
}

/** Editor type size, offered as four steps instead of a slider. */
enum class EditorTextSize(val label: String, val bodySp: Float) {
    Small("小", 14f),
    Normal("标准", 16f),
    Large("大", 18f),
    Huge("特大", 21f),
    ;

    companion object {
        fun from(name: String?): EditorTextSize = entries.firstOrNull { it.name == name } ?: Normal
    }
}
