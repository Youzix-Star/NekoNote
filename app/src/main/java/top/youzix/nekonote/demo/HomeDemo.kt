/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.demo

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import top.youzix.nekonote.AppState
import top.youzix.nekonote.BuildConfig
import top.youzix.nekonote.ui.AppIcons

/** How a tag on the hero card is coloured. */
enum class Tone { OK, WARN, BAD, BUSY, IDLE, ACCENT }

/** A small label shown on the hero card. */
data class Tag(val label: String, val tone: Tone)

/**
 * The hero card: the one block on the page that carries the state.
 *
 * The shape is SukiSU's: a big tinted surface with a watermark, tags along the top, and the title
 * set large enough to be the first thing a thumb lands on.
 */
data class Hero(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tags: List<Tag>,
)

/** One of the three big numbers under the hero. */
data class Stat(val count: String, val label: String)

/** One line of a spec sheet: a label on the left, a monospace value on the right. */
data class SpecRow(val label: String, val value: String)

/** Where a home link goes. The shell decides what that means. */
enum class HomeTarget { Gallery, Licenses }

/** A row in the home page's segmented list. */
data class HomeLink(
    val target: HomeTarget,
    val title: String,
    val summary: String,
    val icon: ImageVector,
)

/**
 * Everything the home page draws.
 *
 * The page has no state of its own and nothing behind it to ask — this value is the whole input,
 * which is what keeps the two engines showing the same thing.
 */
data class Home(
    val hero: Hero,
    val stats: List<Stat>,
    val links: List<HomeLink>,
    val system: List<SpecRow>,
    val build: List<SpecRow>,
)

/** The home page's content. Reads [AppState], so switching the engine redraws it. */
@Composable
fun demoHome(): Home = Home(
    hero = Hero(
        icon = AppIcons.Home,
        title = "界面外壳",
        subtitle = "v${BuildConfig.VERSION_NAME} · ${AppState.engine.label} · 已签名",
        tags = listOf(
            Tag("双引擎", Tone.ACCENT),
            Tag("已签名", Tone.OK),
            Tag("演示", Tone.IDLE),
        ),
    ),
    stats = listOf(
        Stat("4", "页签"),
        Stat("24", "控件"),
        Stat("2", "引擎"),
    ),
    links = listOf(
        HomeLink(
            target = HomeTarget.Gallery,
            title = "组件画廊",
            summary = "控件都在这儿",
            icon = AppIcons.Gallery,
        ),
        HomeLink(
            target = HomeTarget.Licenses,
            title = "开源许可",
            summary = "AGPL-3.0",
            icon = AppIcons.License,
        ),
    ),
    system = listOf(
        SpecRow("应用版本", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"),
        SpecRow("系统版本", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"),
        SpecRow("界面引擎", AppState.engine.label),
        SpecRow("编译 / 目标", "SDK 37 / 35"),
    ),
    build = listOf(
        SpecRow("签名", "v2 + v3"),
        SpecRow("压缩", "R8 + 资源收缩"),
        SpecRow("构建", "GitHub Actions"),
        SpecRow("崩溃处理", ":crash 独立进程"),
    ),
)
