/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.miuix.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.youzix.nekonote.demo.DocsDemo
import top.youzix.nekonote.demo.LogDemo
import top.youzix.nekonote.ui.miuix.components.StatusTag
import top.youzix.nekonote.ui.miuix.components.WarningCard
import top.youzix.nekonote.ui.miuix.components.WarningLevel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.ColorPicker
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
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

/**
 * 组件 — every control the shell offers, on one page.
 *
 * This is the page an AI (or a person) should scroll through before writing a new screen: if what
 * it needs is here, it should be reused rather than rebuilt.
 */
@Composable
fun GalleryScreen(bottomPadding: Dp) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            BlurredBar(backdrop = null) {
                TopAppBar(title = "组件", scrollBehavior = scrollBehavior)
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        GalleryContent(
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + bottomPadding,
            ),
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
private fun GalleryContent(
    contentPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
) {
    var switchOn by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableStateOf(0.4f) }
    val initialColor = MiuixTheme.colorScheme.primary
    var picked by remember { mutableStateOf(initialColor) }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .overScrollVertical(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "text") {
            Section(title = "文本") {
                Text("标题", style = MiuixTheme.textStyles.title3)
                Spacer(Modifier.height(6.dp))
                Text("正文：一屏说清现在怎样、接下来能做什么。", style = MiuixTheme.textStyles.body2)
                Spacer(Modifier.height(6.dp))
                Text("脚注：其余都是装饰。", style = MiuixTheme.textStyles.footnote1)
            }
        }

        item(key = "buttons") {
            Section(title = "按钮") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {}) {
                        Text("主要操作", style = MiuixTheme.textStyles.button)
                    }
                    Button(onClick = {}, enabled = false) {
                        Text("不可用", style = MiuixTheme.textStyles.button)
                    }
                }
            }
        }

        item(key = "tags") {
            Section(title = "标签") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusTag(
                        label = "稳定",
                        backgroundColor = MiuixTheme.colorScheme.primaryContainer,
                        contentColor = MiuixTheme.colorScheme.onPrimaryContainer,
                    )
                    StatusTag(
                        label = "实验",
                        backgroundColor = MiuixTheme.colorScheme.errorContainer,
                        contentColor = MiuixTheme.colorScheme.onErrorContainer,
                    )
                    StatusTag(
                        label = "强调",
                        backgroundColor = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSecondaryContainer,
                    )
                    StatusTag(
                        label = "默认",
                        backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                    )
                }
            }
        }

        item(key = "controls") {
            Section(title = "开关 / 滑杆") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("开关", style = MiuixTheme.textStyles.body2)
                    Switch(checked = switchOn, onCheckedChange = { switchOn = it })
                }
                Spacer(Modifier.height(8.dp))
                Slider(value = sliderValue, onValueChange = { sliderValue = it })
                Text(
                    text = "滑杆 ${(sliderValue * 100).toInt()}%",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        item(key = "warning") {
            Column {
                SmallTitle(text = "提示")
                WarningCard(message = "已保存 ٩(ˊᗜˋ*)و", level = WarningLevel.Notice)
                Spacer(Modifier.height(8.dp))
                WarningCard(message = "失败了，可以重试", level = WarningLevel.Error)
            }
        }

        item(key = "picker") {
            Section(title = "取色器") {
                ColorPicker(color = picked, onColorChanged = { picked = it })
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(picked, RoundedCornerShape(12.dp)),
                )
            }
        }

        item(key = "palette") {
            Section(title = "调色板") {
                PaletteRow("primary", MiuixTheme.colorScheme.primary)
                PaletteRow("secondary", MiuixTheme.colorScheme.secondary)
                PaletteRow("tertiary", MiuixTheme.colorScheme.tertiaryContainer)
                PaletteRow("error", MiuixTheme.colorScheme.error)
                PaletteRow("surface", MiuixTheme.colorScheme.surface)
                PaletteRow("surfaceContainer", MiuixTheme.colorScheme.surfaceContainerHigh)
            }
        }

        item(key = "code") {
            Section(title = "代码") {
                Text(
                    text = DocsDemo.CODE,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }

        item(key = "log") {
            Section(title = "日志") {
                LogDemo.lines.takeLast(4).forEach { line ->
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        item(key = "doc") {
            Section(title = "文档") {
                Text(DocsDemo.TITLE, style = MiuixTheme.textStyles.title4)
                Spacer(Modifier.height(6.dp))
                DocsDemo.paragraphs.forEach { paragraph ->
                    Text(paragraph, style = MiuixTheme.textStyles.body2)
                }
                Spacer(Modifier.height(6.dp))
                DocsDemo.bulletPoints.forEach { point ->
                    Text("· $point", style = MiuixTheme.textStyles.footnote1)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "「${DocsDemo.QUOTE}」",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.primary,
                )
            }
        }

        item(key = "dialog") {
            Section(title = "对话框") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { showDialog = true }) {
                        Text("打开对话框", style = MiuixTheme.textStyles.button)
                    }
                    Button(onClick = {}) {
                        Text("显示提示", style = MiuixTheme.textStyles.button)
                    }
                }
            }
        }
    }

    if (showDialog) {
        OverlayDialog(
            show = true,
            title = "对话框",
            summary = "能开二级页就别用对话框。",
            onDismissRequest = { showDialog = false },
        ) {
            Button(onClick = { showDialog = false }, modifier = Modifier.fillMaxWidth()) {
                Text("知道了", style = MiuixTheme.textStyles.button)
            }
        }
    }
}

/** A titled card: the gallery's unit of structure. */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        SmallTitle(text = title)
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        ) {
            content()
        }
    }
}

/** One labelled colour swatch. */
@Composable
private fun PaletteRow(label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(6.dp)),
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
