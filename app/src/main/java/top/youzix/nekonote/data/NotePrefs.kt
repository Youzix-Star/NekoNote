/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.data

import android.content.Context

/** The editor choices that must survive a restart. */
object NotePrefs {
    private const val PREFS = "note_prefs"
    private const val KEY_SORT = "sort"
    private const val KEY_TEXT_SIZE = "text_size"
    private const val KEY_PREVIEW_FIRST = "preview_first"
    private const val KEY_FONT = "font"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadSort(context: Context): NoteSort =
        NoteSort.from(prefs(context).getString(KEY_SORT, null))

    fun saveSort(context: Context, sort: NoteSort) {
        prefs(context).edit().putString(KEY_SORT, sort.name).apply()
    }

    fun loadTextSize(context: Context): EditorTextSize =
        EditorTextSize.from(prefs(context).getString(KEY_TEXT_SIZE, null))

    fun saveTextSize(context: Context, size: EditorTextSize) {
        prefs(context).edit().putString(KEY_TEXT_SIZE, size.name).apply()
    }

    fun loadFont(context: Context): NoteFont =
        NoteFont.from(prefs(context).getString(KEY_FONT, null))

    fun saveFont(context: Context, font: NoteFont) {
        prefs(context).edit().putString(KEY_FONT, font.name).apply()
    }

    /** Whether opening a note lands on the rendered preview instead of the source. */
    fun loadPreviewFirst(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PREVIEW_FIRST, false)

    fun savePreviewFirst(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PREVIEW_FIRST, enabled).apply()
    }
}
