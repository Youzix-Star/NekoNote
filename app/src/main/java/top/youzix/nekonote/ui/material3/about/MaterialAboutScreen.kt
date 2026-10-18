/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * Rows are built from the segmented-column widgets ported from InstallerX-Revived (GPL-3.0).
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import top.youzix.nekonote.AppState
import top.youzix.nekonote.BuildConfig
import top.youzix.nekonote.crash.CrashHandler
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.AppMark
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.util.DeviceInfo
import top.youzix.nekonote.util.UpdateChecker
import top.youzix.nekonote.util.UpdateResult
import top.youzix.nekonote.ui.material3.material3AppBarColor
import top.youzix.nekonote.ui.material3.material3BlurEffect
import top.youzix.nekonote.ui.material3.rememberMaterial3BlurBackdrop
import top.youzix.nekonote.ui.material3.components.AppDialog
import top.youzix.nekonote.ui.material3.components.IconTile
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedListItem
import top.yukonga.miuix.kmp.blur.layerBackdrop

private const val REPOSITORY_URL = "https://github.com/Youzix-Star/NekoNote"

/**
 * 关于 — the identity page: what this build is, where it comes from, and what it wrote down when
 * it died. It is a second-level page, so it carries its own top bar and back arrow.
 */
@Composable
fun MaterialAboutScreen(
    onBack: () -> Unit,
    useBlur: Boolean,
    onOpenLicenses: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val backdrop = rememberMaterial3BlurBackdrop(useBlur)
    var crashLog by remember { mutableStateOf(CrashHandler.read(context)) }
    var showCrash by remember { mutableStateOf(false) }
    var deviceInfo by remember { mutableStateOf<String?>(null) }
    var checking by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<UpdateResult.Available?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                modifier = Modifier.material3BlurEffect(backdrop),
                title = { Text("关于", modifier = Modifier.padding(start = 12.dp)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backdrop.material3AppBarColor(),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    scrolledContainerColor = backdrop.material3AppBarColor(),
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(backdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier),
            contentPadding = paddingValues + PaddingValues(top = 8.dp, bottom = 24.dp),
        ) {
            item { AppHeader(onNotify = onNotify) }

            item {
                SegmentedColumn(
                    title = "关于",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    item {
                        SegmentedListItem(
                            onClick = { uriHandler.openUri(REPOSITORY_URL) },
                            headlineContent = { Text("获取源代码") },
                            supportingContent = { Text("GitHub 上的 NekoNote") },
                            leadingContent = { IconTile(AppIcons.SourceCode, "获取源代码") },
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
                    item {
                        SegmentedListItem(
                            onClick = {
                                crashLog = CrashHandler.read(context)
                                showCrash = true
                            },
                            headlineContent = { Text("崩溃日志") },
                            supportingContent = {
                                Text(
                                    if (crashLog.isNullOrBlank()) "没有记录"
                                    else "有一条，点开看看",
                                )
                            },
                            leadingContent = { IconTile(AppIcons.Debug, "崩溃日志") },
                            trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        )
                    }
                    item {
                        SegmentedListItem(
                            onClick = { deviceInfo = DeviceInfo.snapshot(context) },
                            headlineContent = { Text("设备信息") },
                            supportingContent = { Text("机型、系统与屏幕") },
                            leadingContent = { IconTile(AppIcons.Phones, "设备信息") },
                            trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        )
                    }
                    item {
                        SegmentedListItem(
                            onClick = {
                                if (!checking) {
                                    checking = true
                                    UpdateChecker.check { result ->
                                        checking = false
                                        when (result) {
                                            is UpdateResult.Available -> update = result
                                            UpdateResult.UpToDate -> onNotify("已经最新啦 ٩(ˊᗜˋ*)و")
                                            is UpdateResult.Failed -> onNotify("检查失败：" + result.message)
                                        }
                                    }
                                }
                            },
                            headlineContent = { Text("检查更新") },
                            supportingContent = {
                                Text(if (checking) "看看…" else "当前 v${BuildConfig.VERSION_NAME}")
                            },
                            leadingContent = { IconTile(AppIcons.Refresh, "检查更新") },
                            trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        )
                    }
                }
            }

            if (AppState.debugMode) {
                item {
                    SegmentedColumn(
                        title = "调试",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        item {
                            // Verifying the crash screen needs a crash, and waiting for a real bug
                            // to happen is not a test.
                            SegmentedListItem(
                                onClick = {
                                    throw IllegalStateException("模拟崩溃：这是调试里手动触发的")
                                },
                                headlineContent = { Text("模拟崩溃") },
                                supportingContent = { Text("崩一次，看看报告页") },
                                leadingContent = { IconTile(AppIcons.Debug, "模拟崩溃") },
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "AGPL-3.0 · 一个壳子 (･ω<)☆",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                )
            }
        }
    }

    val report = crashLog
    if (showCrash) {
        AppDialog(title = "崩溃日志", onDismissRequest = { showCrash = false }) {
            if (report.isNullOrBlank()) {
                Text("没有崩溃记录。")
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = report,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!report.isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(report))
                            onNotify("已复制崩溃日志")
                        },
                    ) { Text("复制") }
                    TextButton(
                        onClick = {
                            CrashHandler.clear(context)
                            crashLog = null
                            showCrash = false
                            onNotify("已清空")
                        },
                    ) { Text("清空") }
                }
                TextButton(onClick = { showCrash = false }) { Text("关闭") }
            }
        }
    }

    val info = deviceInfo
    if (info != null) {
        AppDialog(title = "设备信息", onDismissRequest = { deviceInfo = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = info,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(info))
                        onNotify("已复制设备信息")
                        deviceInfo = null
                    },
                ) { Text("复制") }
                TextButton(onClick = { deviceInfo = null }) { Text("关闭") }
            }
        }
    }

    update?.let { available ->
        AppDialog(title = "发现新版本 v${available.version}", onDismissRequest = { update = null }) {
            if (available.notes.isBlank()) {
                Text("这个版本没有写更新说明。")
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(text = available.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        val target = available.apkUrl
                        if (target != null) {
                            uriHandler.openUri(target)
                        } else {
                            UpdateChecker.openReleasePage(context)
                        }
                        update = null
                    },
                ) { Text(if (available.apkUrl != null) "下载" else "打开发布页") }
                TextButton(onClick = { update = null }) { Text("关闭") }
            }
        }
    }
}

@Composable
private fun AppHeader(onNotify: (String) -> Unit) {
    val context = LocalContext.current
    var taps by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "NekoNote", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Seven taps on the signature line put the diagnostic rows on show; nothing says so.
        Text(
            text = "Ciallo～(∠・ω c)⌒★",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable {
                taps += 1
                if (taps >= 7) {
                    taps = 0
                    val enabled = !AppState.debugMode
                    AppState.debugMode = enabled
                    UiEnginePrefs.saveDebugMode(context, enabled)
                    onNotify(if (enabled) "调试模式已开启" else "调试模式已关闭")
                }
            },
        )
    }
}
