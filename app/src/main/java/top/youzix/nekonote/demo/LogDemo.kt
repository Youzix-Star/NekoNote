/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The gallery's log block, and the crash page's breadcrumb trail.
 *
 * A shell like the rest of the demo layer: [append] only adds a line to the list.
 */
object LogDemo {
    private const val MAX_LINES = 200

    val lines = mutableStateListOf<String>().apply { addAll(initial()) }

    var visible by mutableStateOf(true)
        private set

    fun append(line: String) {
        lines += line
        while (lines.size > MAX_LINES) lines.removeAt(0)
    }

    fun clear() {
        lines.clear()
    }

    fun toggleVisible() {
        visible = !visible
    }

    private fun initial(): List<String> = listOf(
        "12:01:04 I/NekoNote 引擎 = Miuix（已持久化）",
        "12:01:04 I/NekoNote 主题 = 动态取色 · 跟随系统",
        "12:01:05 I/NekoNote 首页：3 个概览分组",
        "12:01:09 I/NekoNote 列表：搜索 \"标签\" → 1 组 / 1 行",
        "12:01:12 W/NekoNote 空态：没有匹配的条目",
        "12:01:15 I/NekoNote 画廊：24 个控件已渲染",
        "12:01:20 I/NekoNote 设置：切换引擎 → Material Design",
        "12:01:23 I/NekoNote 二级页：开源许可（预见式返回）",
    )
}
