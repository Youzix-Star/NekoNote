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
 * The bundled faces carry one regular weight; callers set `fontSynthesis = FontSynthesis.All` so
 * titles and Markdown bold are synthesized instead of silently falling back.
 */
object NoteFonts {
    private val miSans = FontFamily(Font(R.font.misans_regular, FontWeight.Normal))
    private val notoSans = FontFamily(Font(R.font.noto_sans_sc_regular, FontWeight.Normal))
    private val harmony = FontFamily(Font(R.font.harmonyos_sans_sc_regular, FontWeight.Normal))

    fun of(font: NoteFont): FontFamily = when (font) {
        NoteFont.System -> FontFamily.SansSerif
        NoteFont.MiSans -> miSans
        NoteFont.NotoSans -> notoSans
        NoteFont.HarmonyOS -> harmony
    }
}
