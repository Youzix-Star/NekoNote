/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.fonts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.youzix.nekonote.AppState
import top.youzix.nekonote.data.EditorTextSize
import top.youzix.nekonote.data.FontPreview
import top.youzix.nekonote.data.NoteFont
import top.youzix.nekonote.data.NotePrefs
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold
import top.youzix.nekonote.ui.material3.components.IconTile
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedDropdownItem
import top.youzix.nekonote.ui.material3.components.SegmentedListItem
import top.youzix.nekonote.ui.material3.components.TonalCard
import top.youzix.nekonote.ui.material3.components.expressiveTopAppBarColors
import top.youzix.nekonote.ui.theme.NoteFonts

/** 字体与字号 — the Material flavour of the font picker. */
@Composable
fun MaterialFontsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val family = NoteFonts.of(AppState.noteFont)
    val bodySize = AppState.editorTextSize.bodySp

    ExpressiveScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("字体与字号") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.Back, "返回")
                    }
                },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 4.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "preview") {
                TonalCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = FontPreview.TITLE,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = family,
                                fontSynthesis = FontSynthesis.All,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = FontPreview.BODY,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = family,
                                fontSynthesis = FontSynthesis.All,
                                fontSize = bodySize.sp,
                                lineHeight = (bodySize * 1.7f).sp,
                            ),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = FontPreview.EXTRA,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = family,
                                fontSynthesis = FontSynthesis.All,
                                fontSize = (bodySize - 2f).coerceAtLeast(11f).sp,
                                lineHeight = (bodySize * 1.5f).sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item(key = "fonts") {
                SegmentedColumn(title = "字体") {
                    NoteFont.entries.forEach { font ->
                        item(key = font.name) {
                            SegmentedListItem(
                                onClick = {
                                    AppState.noteFont = font
                                    NotePrefs.saveFont(context, font)
                                },
                                headlineContent = {
                                    Text(
                                        text = font.label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = NoteFonts.of(font),
                                            fontSynthesis = FontSynthesis.All,
                                        ),
                                    )
                                },
                                supportingContent = { Text(font.note) },
                                leadingContent = { IconTile(AppIcons.TextFormat, font.label) },
                                trailingContent = {
                                    if (font == AppState.noteFont) {
                                        Icon(AppIcons.Check, "已选择")
                                    }
                                },
                            )
                        }
                    }
                }
            }

            item(key = "size") {
                SegmentedColumn(title = "字号") {
                    item {
                        SegmentedDropdownItem(
                            icon = AppIcons.TextFormat,
                            title = "正文字号",
                            summary = "编辑与预览共用",
                            items = EditorTextSize.entries.map { it.label },
                            selectedIndex = EditorTextSize.entries
                                .indexOf(AppState.editorTextSize)
                                .coerceAtLeast(0),
                            onItemSelected = { index ->
                                EditorTextSize.entries.getOrNull(index)?.let { size ->
                                    AppState.editorTextSize = size
                                    NotePrefs.saveTextSize(context, size)
                                }
                            },
                        )
                    }
                }
            }

            item(key = "license") {
                Text(
                    text = "MiSans（小米）、HarmonyOS Sans（华为）与 Noto Sans（SIL OFL 1.1）均可免费商用；完整许可见「开源许可」。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}
