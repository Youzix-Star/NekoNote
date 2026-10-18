/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * The page is SukiSU-Ultra's SettingsMaterial.kt (GPL-3.0), same chrome and same row spacing:
 * an expressive scaffold, a column of segmented groups inset 16dp, 13dp between groups.
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.youzix.nekonote.AppState
import top.youzix.nekonote.data.EditorTextSize
import top.youzix.nekonote.data.NotePrefs
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.data.NoteSort
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold
import top.youzix.nekonote.ui.material3.components.IconTile
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedDropdownItem
import top.youzix.nekonote.ui.material3.components.SegmentedListItem
import top.youzix.nekonote.ui.material3.components.SegmentedSwitchItem
import top.youzix.nekonote.ui.material3.components.expressiveTopAppBarColors
import top.youzix.nekonote.ui.theme.ColorMode

@Composable
fun MaterialSettingsScreen(
    outerPadding: PaddingValues,
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val noteCount = NoteRepository.noteCount()
    val tagCount = NoteRepository.tags().size

    ExpressiveScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("设置") },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(outerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SegmentedColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
                title = "外观",
            ) {
                item {
                    SegmentedDropdownItem(
                        icon = AppIcons.Palette,
                        title = "主题模式",
                        summary = "浅色 / 深色 / 跟随系统",
                        items = ColorMode.entries.map { it.label },
                        selectedIndex = ColorMode.entries.indexOf(AppState.colorMode).coerceAtLeast(0),
                        onItemSelected = { index ->
                            ColorMode.entries.getOrNull(index)?.let {
                                AppState.colorMode = it
                                UiEnginePrefs.saveColorMode(context, it)
                            }
                        },
                    )
                }
                item {
                    SegmentedSwitchItem(
                        icon = AppIcons.BlurOn,
                        title = "模糊",
                        summary = "底栏与顶栏的毛玻璃",
                        checked = AppState.useBlur,
                        onCheckedChange = {
                            AppState.useBlur = it
                            UiEnginePrefs.saveUseBlur(context, it)
                        },
                    )
                }
            }

            SegmentedColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
                title = "编辑器",
            ) {
                item {
                    SegmentedDropdownItem(
                        icon = AppIcons.Edit,
                        title = "字号",
                        summary = "编辑和预览的正文字号",
                        items = EditorTextSize.entries.map { it.label },
                        selectedIndex = EditorTextSize.entries
                            .indexOf(AppState.editorTextSize)
                            .coerceAtLeast(0),
                        onItemSelected = { index ->
                            EditorTextSize.entries.getOrNull(index)?.let {
                                AppState.editorTextSize = it
                                NotePrefs.saveTextSize(context, it)
                            }
                        },
                    )
                }
                item {
                    SegmentedSwitchItem(
                        icon = AppIcons.Preview,
                        title = "打开时先看预览",
                        summary = "关掉就直接进 Markdown 源码",
                        checked = AppState.previewFirst,
                        onCheckedChange = {
                            AppState.previewFirst = it
                            NotePrefs.savePreviewFirst(context, it)
                        },
                    )
                }
                item {
                    SegmentedDropdownItem(
                        icon = AppIcons.Sort,
                        title = "默认排序",
                        summary = "置顶的笔记始终在最前面",
                        items = NoteSort.entries.map { it.label },
                        selectedIndex = NoteSort.entries
                            .indexOf(AppState.noteSort)
                            .coerceAtLeast(0),
                        onItemSelected = { index ->
                            NoteSort.entries.getOrNull(index)?.let {
                                AppState.noteSort = it
                                NotePrefs.saveSort(context, it)
                            }
                        },
                    )
                }
            }

            SegmentedColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
                title = "引擎",
            ) {
                item {
                    SegmentedDropdownItem(
                        icon = AppIcons.Notes,
                        title = "界面引擎",
                        summary = "换一套皮，笔记不变",
                        items = UiEngine.entries.map { it.label },
                        selectedIndex = UiEngine.entries.indexOf(engine).coerceAtLeast(0),
                        onItemSelected = { index ->
                            UiEngine.entries.getOrNull(index)?.let(onEngineChange)
                        },
                    )
                }
            }

            SegmentedColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
                title = "数据",
            ) {
                item {
                    SegmentedListItem(
                        headlineContent = { Text("笔记与标签") },
                        supportingContent = { Text("$noteCount 条笔记 · $tagCount 个标签") },
                        leadingContent = { IconTile(AppIcons.Notes, "笔记与标签") },
                    )
                }
                item {
                    SegmentedListItem(
                        headlineContent = { Text("存储位置") },
                        supportingContent = { Text(NoteRepository.storagePath()) },
                        leadingContent = { IconTile(AppIcons.License, "存储位置") },
                    )
                }
            }

            SegmentedColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp),
                title = "关于",
            ) {
                item {
                    SegmentedListItem(
                        onClick = onOpenAbout,
                        headlineContent = { Text("关于 NekoNote") },
                        supportingContent = { Text("版本、源码与设备") },
                        leadingContent = { IconTile(AppIcons.About, "关于 NekoNote") },
                        trailingContent = { Icon(AppIcons.ChevronRight, null) },
                    )
                }
                item {
                    SegmentedListItem(
                        onClick = onOpenLicenses,
                        headlineContent = { Text("开源许可") },
                        supportingContent = { Text("依赖的许可") },
                        leadingContent = { IconTile(AppIcons.License, "开源许可") },
                        trailingContent = { Icon(AppIcons.ChevronRight, null) },
                    )
                }
            }
        }
    }
}
