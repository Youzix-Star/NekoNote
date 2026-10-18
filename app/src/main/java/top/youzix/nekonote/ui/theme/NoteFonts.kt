/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import top.youzix.nekonote.R
import top.youzix.nekonote.data.NoteFont

/**
 * The article font family.
 *
 * Each bundled family carries a regular and a bold face from the packages the user supplied;
 * callers still set `fontSynthesis = FontSynthesis.All` so any missing face is synthesized.
 */
object NoteFonts {
    private val miSans = FontFamily(
        Font(R.font.misans_regular, FontWeight.Normal),
        Font(R.font.misans_bold, FontWeight.Bold),
    )
    private val notoSans = FontFamily(
        Font(R.font.noto_sans_sc_regular, FontWeight.Normal),
        Font(R.font.noto_sans_sc_bold, FontWeight.Bold),
    )
    private val harmony = FontFamily(
        Font(R.font.harmonyos_sans_sc_regular, FontWeight.Normal),
        Font(R.font.harmonyos_sans_sc_bold, FontWeight.Bold),
    )

    fun of(font: NoteFont): FontFamily = when (font) {
        NoteFont.System -> FontFamily.SansSerif
        NoteFont.MiSans -> miSans
        NoteFont.NotoSans -> notoSans
        NoteFont.HarmonyOS -> harmony
    }
}
