/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.demo

/** What a row's tag is saying. */
enum class TagTone { OK, WARN, ACCENT, NEUTRAL }

/** The superuser-style filter above the list. */
enum class ListFilter { All, Granted, Denied }

/** One row: an app and whether it has been granted access. */
data class AppEntry(
    val id: String,
    val name: String,
    val packageName: String,
    val granted: Boolean,
    val note: String,
    val tone: TagTone,
)

/**
 * The list page's content: a superuser-shaped list of apps.
 *
 * SukiSU's SuperUser page is the reference here — a granted/total summary, a row of filter chips,
 * then one segmented list. The data is written down rather than fetched, because the page exists
 * to show the shape.
 */
object ListDemo {

    val apps: List<AppEntry> = listOf(
        AppEntry("camera", "相机", "com.example.camera", true, "ROOT", TagTone.OK),
        AppEntry("files", "文件管理", "com.example.files", true, "ROOT", TagTone.OK),
        AppEntry("term", "终端", "com.example.term", true, "自定义", TagTone.ACCENT),
        AppEntry("backup", "备份", "com.example.backup", true, "ROOT", TagTone.OK),
        AppEntry("shell", "Shell", "com.example.shell", false, "未授权", TagTone.NEUTRAL),
        AppEntry("tweak", "调参工具", "com.example.tweak", false, "未授权", TagTone.NEUTRAL),
        AppEntry("adb", "无线调试", "com.example.adb", true, "自定义", TagTone.ACCENT),
        AppEntry("log", "日志查看器", "com.example.logviewer", false, "未授权", TagTone.NEUTRAL),
    )

    val total: Int get() = apps.size

    val granted: Int get() = apps.count { it.granted }

    /** Apps matching [filter] and [query], in declaration order. */
    fun filter(query: String, filter: ListFilter): List<AppEntry> {
        val needle = query.trim()
        return apps.filter { app ->
            val matchesFilter = when (filter) {
                ListFilter.All -> true
                ListFilter.Granted -> app.granted
                ListFilter.Denied -> !app.granted
            }
            val matchesQuery = needle.isEmpty() ||
                app.name.contains(needle, ignoreCase = true) ||
                app.packageName.contains(needle, ignoreCase = true) ||
                app.note.contains(needle, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }
}
