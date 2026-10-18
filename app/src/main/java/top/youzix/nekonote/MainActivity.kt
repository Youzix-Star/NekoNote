/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import top.youzix.nekonote.data.NotePrefs
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.ui.material3.MaterialApp
import top.youzix.nekonote.ui.miuix.MiuixApp

/**
 * Picks the UI engine and hands the whole window to it.
 *
 * Two complete implementations of the same three tabs live side by side — [MiuixApp] and
 * [MaterialApp] — and the choice is persisted, so switching in Settings takes effect immediately.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppState.engine = UiEnginePrefs.load(this)
        AppState.useBlur = UiEnginePrefs.loadUseBlur(this)
        AppState.colorMode = UiEnginePrefs.loadColorMode(this)
        AppState.debugMode = UiEnginePrefs.loadDebugMode(this)
        AppState.noteSort = NotePrefs.loadSort(this)
        AppState.editorTextSize = NotePrefs.loadTextSize(this)
        AppState.previewFirst = NotePrefs.loadPreviewFirst(this)
        setContent {
            when (AppState.engine) {
                UiEngine.Miuix -> MiuixApp()
                UiEngine.Material3 -> MaterialApp()
            }
        }
    }
}
