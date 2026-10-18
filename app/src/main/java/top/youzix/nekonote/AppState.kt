/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import top.youzix.nekonote.data.EditorTextSize
import top.youzix.nekonote.data.NoteSort
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.theme.ColorMode

/**
 * Process-wide state for the shell and the editor.
 *
 * The note data itself lives in [top.youzix.nekonote.data.NoteRepository]; this object only holds
 * the choices about how it is shown, all of which are persisted.
 */
object AppState {
    /** Which engine draws the app; persisted on change via [top.youzix.nekonote.ui.UiEnginePrefs]. */
    var engine by mutableStateOf(UiEngine.Miuix)

    /**
     * Translucent surfaces: the miuix bottom bar's liquid glass, and the Material 3 top bar's
     * blur. Persisted alongside the engine.
     */
    var useBlur by mutableStateOf(true)

    /** Light/dark/monet choice, shared by both engines. */
    var colorMode by mutableStateOf(ColorMode.System)

    /**
     * Whether the diagnostic rows are on show.
     *
     * Kept in memory and on disk: the About page unlocks it by tapping the tagline seven times.
     */
    var debugMode by mutableStateOf(false)

    /** The order of the note list below the pinned notes; persisted via NotePrefs. */
    var noteSort by mutableStateOf(NoteSort.Updated)

    /** The editor's body size; persisted via NotePrefs. */
    var editorTextSize by mutableStateOf(EditorTextSize.Normal)

    /** Whether the editor opens on the rendered preview instead of the Markdown source. */
    var previewFirst by mutableStateOf(false)

    /**
     * The tag the note list is filtered by, or null for every note.
     *
     * The tags tab writes it and then switches to the notes tab; it is deliberately not persisted,
     * because a filter the user cannot see is a filter they will think is a bug.
     */
    var noteTagFilter by mutableStateOf<String?>(null)
}
