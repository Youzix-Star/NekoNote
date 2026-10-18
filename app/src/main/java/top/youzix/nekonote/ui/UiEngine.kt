/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui

import android.content.Context
import top.youzix.nekonote.ui.theme.ColorMode

/**
 * Which UI engine draws the app.
 *
 * The two engines are complete, independent implementations of the same four tabs — one built on
 * miuix, one on Material Design — and the user picks between them on the Settings page.
 */
enum class UiEngine(val id: String, val label: String) {
    Miuix("miuix", "Miuix"),
    Material3("material3", "Material Design"),
    ;

    companion object {
        fun from(id: String?): UiEngine = entries.firstOrNull { it.id == id } ?: Miuix
    }
}

/** Persists the engine, the glass switch, the theme and the diagnostic switch. */
object UiEnginePrefs {
    private const val PREFS = "ui_prefs"
    private const val KEY_ENGINE = "ui_engine"
    private const val KEY_USE_BLUR = "use_blur"
    private const val KEY_COLOR_MODE = "color_mode"
    private const val KEY_DEBUG_MODE = "debug_mode"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): UiEngine =
        UiEngine.from(prefs(context).getString(KEY_ENGINE, null))

    fun save(context: Context, engine: UiEngine) {
        prefs(context).edit().putString(KEY_ENGINE, engine.id).apply()
    }

    /**
     * Whether translucency is on: the miuix bottom bar's liquid glass and the Material 3 top
     * bar's blur.
     */
    fun loadUseBlur(context: Context): Boolean =
        prefs(context).getBoolean(KEY_USE_BLUR, true)

    fun saveUseBlur(context: Context, useBlur: Boolean) {
        prefs(context).edit().putBoolean(KEY_USE_BLUR, useBlur).apply()
    }

    /** Light/dark/monet choice, by enum name. */
    fun loadColorMode(context: Context): ColorMode =
        ColorMode.fromName(prefs(context).getString(KEY_COLOR_MODE, null))

    fun saveColorMode(context: Context, mode: ColorMode) {
        prefs(context).edit().putString(KEY_COLOR_MODE, mode.name).apply()
    }




    /** Whether the diagnostic rows are on show. */
    fun loadDebugMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DEBUG_MODE, false)

    fun saveDebugMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
    }
}
