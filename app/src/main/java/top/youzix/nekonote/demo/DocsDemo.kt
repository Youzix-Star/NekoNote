/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.demo

/**
 * The text the gallery's document block renders.
 *
 * A handful of Markdown constructs, shown as styling rather than parsed: the gallery is a
 * catalogue of looks, not a Markdown viewer.
 */
object DocsDemo {

    const val TITLE = "组件规范"

    val paragraphs: List<String> = listOf(
        "界面只有两件事要交代清楚：现在是什么状态，接下来能做什么。其余都是装饰。",
    )

    val bulletPoints: List<String> = listOf(
        "一屏一个主操作。",
        "状态用颜色和标签说。",
        "空态要说下一步。",
    )

    const val QUOTE = "能开二级页就别用对话框。"

    const val CODE = "val home = demoHome()\nHomeScreen(contentPadding, scrollBehavior)"

    val codeNote = "等宽 + 主题色，别超过半屏。"
}
