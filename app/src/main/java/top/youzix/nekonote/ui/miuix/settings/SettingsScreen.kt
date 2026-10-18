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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.youzix.nekonote.AppState
import top.youzix.nekonote.data.EditorTextSize
import top.youzix.nekonote.data.NotePrefs
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.data.NoteSort
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.youzix.nekonote.ui.theme.ColorMode
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

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
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val context = LocalContext.current
    val engineLabels = remember { UiEngine.entries.map { it.label } }
    val modeLabels = remember { ColorMode.entries.map { it.label } }
    val sizeLabels = remember { EditorTextSize.entries.map { it.label } }
    val sortLabels = remember { NoteSort.entries.map { it.label } }
    val noteCount = NoteRepository.noteCount()
    val tagCount = NoteRepository.tags().size

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

        item(key = "editor") {
            Column {
                SmallTitle(text = "编辑器")
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "字号",
                        summary = "编辑和预览的正文字号",
                        items = sizeLabels,
                        selectedIndex = EditorTextSize.entries.indexOf(AppState.editorTextSize).coerceAtLeast(0),
                        startAction = { Icon(AppIcons.Edit, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onSelectedIndexChange = { index ->
                            EditorTextSize.entries.getOrNull(index)?.let {
                                AppState.editorTextSize = it
                                NotePrefs.saveTextSize(context, it)
                            }
                        },
                    )
                    SwitchPreference(
                        title = "打开时先看预览",
                        summary = "关掉就直接进 Markdown 源码",
                        startAction = { Icon(AppIcons.Preview, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        checked = AppState.previewFirst,
                        onCheckedChange = {
                            AppState.previewFirst = it
                            NotePrefs.savePreviewFirst(context, it)
                        },
                    )
                    OverlayDropdownPreference(
                        title = "默认排序",
                        summary = "置顶的笔记始终在最前面",
                        items = sortLabels,
                        selectedIndex = NoteSort.entries.indexOf(AppState.noteSort).coerceAtLeast(0),
                        startAction = { Icon(AppIcons.Sort, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onSelectedIndexChange = { index ->
                            NoteSort.entries.getOrNull(index)?.let {
                                AppState.noteSort = it
                                NotePrefs.saveSort(context, it)
                            }
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
                        summary = "换一套皮，笔记不变",
                        items = engineLabels,
                        selectedIndex = UiEngine.entries.indexOf(engine).coerceAtLeast(0),
                        startAction = { Icon(AppIcons.Notes, null, Modifier.size(22.dp), colorScheme.onBackground) },
                        onSelectedIndexChange = { index ->
                            UiEngine.entries.getOrNull(index)?.let(onEngineChange)
                        },
                    )
                }
            }
        }

        item(key = "data") {
            Column {
                SmallTitle(text = "数据")
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "笔记与标签",
                        summary = "$noteCount 条笔记 · $tagCount 个标签",
                        startAction = {
                            Icon(AppIcons.Notes, null, Modifier.size(22.dp), colorScheme.onBackground)
                        },
                    )
                    BasicComponent(
                        title = "存储位置",
                        summary = NoteRepository.storagePath(),
                        startAction = {
                            Icon(AppIcons.License, null, Modifier.size(22.dp), colorScheme.onBackground)
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
