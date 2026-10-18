/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.theme.ColorMode

/**
 * Process-wide UI state for the shell.
 *
 * This build is the interface only: the four tabs draw themselves and remember how the user wants
 * them drawn, and nothing behind them does anything.
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
}
