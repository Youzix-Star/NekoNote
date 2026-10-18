/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.miuix.fonts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.youzix.nekonote.ui.theme.NoteFonts
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * 字体与字号 — one page for the article typeface, its size and a live sample.
 */
@Composable
fun FontsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val family = NoteFonts.of(AppState.noteFont)
    val bodySize = AppState.editorTextSize.bodySp

    Scaffold(
        topBar = {
            BlurredBar(backdrop = null) {
                TopAppBar(
                    title = "字体与字号",
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(AppIcons.Back, "返回")
                        }
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = innerPadding.calculateTopPadding() + 6.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "preview") {
                Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(18.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = FontPreview.TITLE,
                            style = MiuixTheme.textStyles.title3.copy(
                                fontFamily = family,
                                fontSynthesis = FontSynthesis.All,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = FontPreview.BODY,
                            style = MiuixTheme.textStyles.body1.copy(
                                fontFamily = family,
                                fontSynthesis = FontSynthesis.All,
                                fontSize = bodySize.sp,
                                lineHeight = (bodySize * 1.7f).sp,
                            ),
                            color = colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = FontPreview.EXTRA,
                            style = MiuixTheme.textStyles.footnote1.copy(
                                fontFamily = family,
                                fontSynthesis = FontSynthesis.All,
                                fontSize = (bodySize - 2f).coerceAtLeast(11f).sp,
                                lineHeight = (bodySize * 1.5f).sp,
                            ),
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }

            item(key = "fonts") {
                Column {
                    SmallTitle(text = "字体")
                    Card(modifier = Modifier.fillMaxWidth()) {
                        NoteFont.entries.forEach { font ->
                            FontRow(
                                font = font,
                                selected = font == AppState.noteFont,
                                onClick = {
                                    AppState.noteFont = font
                                    NotePrefs.saveFont(context, font)
                                },
                            )
                        }
                    }
                }
            }

            item(key = "size") {
                Column {
                    SmallTitle(text = "字号")
                    Card(modifier = Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(
                            title = "正文字号",
                            summary = "编辑与预览共用",
                            items = EditorTextSize.entries.map { it.label },
                            selectedIndex = EditorTextSize.entries
                                .indexOf(AppState.editorTextSize)
                                .coerceAtLeast(0),
                            startAction = {
                                Icon(
                                    AppIcons.TextFormat,
                                    null,
                                    Modifier.size(22.dp),
                                    colorScheme.onBackground,
                                )
                            },
                            onSelectedIndexChange = { index ->
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
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FontRow(
    font: NoteFont,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = font.label,
                style = MiuixTheme.textStyles.body1.copy(
                    fontFamily = NoteFonts.of(font),
                    fontSynthesis = FontSynthesis.All,
                    fontSize = 17.sp,
                    color = colorScheme.onSurface,
                ),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = font.note,
                style = MiuixTheme.textStyles.footnote2,
                color = colorScheme.onSurfaceVariantSummary,
            )
        }
        if (selected) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = "已选择",
                modifier = Modifier.size(20.dp),
                tint = colorScheme.primary,
            )
        }
    }
}
