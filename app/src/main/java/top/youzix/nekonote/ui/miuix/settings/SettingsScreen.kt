/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * Row layout follows SukiSU-Ultra's SettingsMiuix.kt (GPL-3.0): appearance is one row into the
 * theme page, then the engine, then the identity rows.
 */

package top.youzix.nekonote.ui.miuix.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import top.youzix.nekonote.AppState
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.ui.theme.ColorMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.unit.Dp
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar

@Composable
fun SettingsScreen(
    bottomPadding: Dp,
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            BlurredBar(backdrop = null) {
                TopAppBar(title = "设置", scrollBehavior = scrollBehavior)
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        SettingsContent(
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + bottomPadding,
            ),
            scrollBehavior = scrollBehavior,
            engine = engine,
            onEngineChange = onEngineChange,
            onOpenAbout = onOpenAbout,
            onOpenLicenses = onOpenLicenses,
        )
    }
}

@Composable
private fun SettingsContent(
    contentPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val context = LocalContext.current
    val engineLabels = remember { UiEngine.entries.map { it.label } }
    val modeLabels = remember { ColorMode.entries.map { it.label } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .overScrollVertical(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "appearance") {
            Column {
                SmallTitle(text = "外观")
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "主题模式",
                        summary = "浅色 / 深色 / 跟随系统",
                        items = modeLabels,
                        selectedIndex = ColorMode.entries.indexOf(AppState.colorMode).coerceAtLeast(0),
                        startAction = { Icon(AppIcons.Palette, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onSelectedIndexChange = { index ->
                            ColorMode.entries.getOrNull(index)?.let {
                                AppState.colorMode = it
                                UiEnginePrefs.saveColorMode(context, it)
                            }
                        },
                    )
                    SwitchPreference(
                        title = "模糊",
                        summary = "底栏与顶栏的毛玻璃",
                        startAction = { Icon(AppIcons.BlurOn, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        checked = AppState.useBlur,
                        onCheckedChange = {
                            AppState.useBlur = it
                            UiEnginePrefs.saveUseBlur(context, it)
                        },
                    )
                }
            }
        }

        item(key = "engine") {
            Column {
                SmallTitle(text = "引擎")
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "界面引擎",
                        summary = "换一套皮",
                        items = engineLabels,
                        selectedIndex = UiEngine.entries.indexOf(engine).coerceAtLeast(0),
                        startAction = { Icon(AppIcons.Gallery, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onSelectedIndexChange = { index ->
                            UiEngine.entries.getOrNull(index)?.let(onEngineChange)
                        },
                    )
                }
            }
        }

        item(key = "about") {
            Column {
                SmallTitle(text = "关于")
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "关于 NekoNote",
                        summary = "版本、源码与设备",
                        startAction = { Icon(AppIcons.About, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onClick = onOpenAbout,
                    )
                    ArrowPreference(
                        title = "开源许可",
                        summary = "依赖的许可",
                        startAction = { Icon(AppIcons.License, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onClick = onOpenLicenses,
                    )
                }
            }
        }
    }
}
