/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.data

/**
 * Typefaces offered for the article. The three bundled families are free for commercial use;
 * the system option follows whatever the device uses.
 */
enum class NoteFont(val label: String, val note: String) {
    System("系统字体", "设备默认字体"),
    MiSans("MiSans", "小米 MiSans · 免费商用"),
    NotoSans("Noto Sans", "SIL OFL 1.1"),
    HarmonyOS("HarmonyOS Sans", "华为鸿蒙字体 · 免费商用"),
    ;

    companion object {
        fun from(name: String?): NoteFont = entries.firstOrNull { it.name == name } ?: System
    }
}

/** The sample used by the font picker; kept here so both engines show the same text. */
object FontPreview {
    const val TITLE = "世界人权宣言"
    const val BODY =
        "鉴于对人类家庭所有成员的固有尊严及其平等的和不移的权利的承认，乃是世界自由、正义与和平的基础。"
    const val EXTRA =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz 0123456789 · #标签 · ★ ☆ ♪ ✓ →"
}
