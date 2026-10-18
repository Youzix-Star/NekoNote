/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.miuix.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private const val REPOSITORY_URL = "https://github.com/Youzix-Star/NekoNote"

/**
 * 关于 — the identity page: what this build is, where it comes from, and what it wrote down when
 * it died.
 *
 * The crash report is the reason this page exists at all: a report nobody can reach is the same as
 * no report, so the file is listed, viewable, copyable and clearable from here.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "关于",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = AppIcons.Back, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection) + 12.dp,
            top = innerPadding.calculateTopPadding() + 12.dp,
            end = innerPadding.calculateEndPadding(layoutDirection) + 12.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp,
        )
        AboutContent(contentPadding, scrollBehavior, onOpenLicenses, onNotify)
    }
}

@Composable
private fun AboutContent(
    contentPadding: PaddingValues,
    scrollBehavior: ScrollBehavior,
    onOpenLicenses: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var crashLog by remember { mutableStateOf(CrashHandler.read(context)) }
    var showCrash by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<UpdateResult.Available?>(null) }
    var deviceInfo by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .overScrollVertical(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            AppHeader(onNotify = onNotify)
        }

        item(key = "about") {
            Column {
                SmallTitle(text = "关于")
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "获取源代码",
                        summary = "GitHub 上的 NekoNote",
                        startAction = {
                            Icon(AppIcons.SourceCode, null, Modifier.size(22.dp))
                        },
                        onClick = { uriHandler.openUri(REPOSITORY_URL) },
                    )
                    ArrowPreference(
                        title = "开源许可",
                        summary = "依赖的许可",
                        startAction = {
                            Icon(AppIcons.License, null, Modifier.size(22.dp))
                        },
                        onClick = onOpenLicenses,
                    )
                    ArrowPreference(
                        title = "崩溃日志",
                        summary = if (crashLog.isNullOrBlank()) "没有记录" else "有一条，点开看看",
                        startAction = {
                            Icon(AppIcons.Debug, null, Modifier.size(22.dp))
                        },
                        onClick = {
                            crashLog = CrashHandler.read(context)
                            showCrash = true
                        },
                    )
                    ArrowPreference(
                        title = "设备信息",
                        summary = "机型、系统与屏幕",
                        startAction = {
                            Icon(AppIcons.Phones, null, Modifier.size(22.dp))
                        },
                        onClick = { deviceInfo = DeviceInfo.snapshot(context) },
                    )
                    ArrowPreference(
                        title = "检查更新",
                        summary = if (checking) "看看…" else "当前 v${BuildConfig.VERSION_NAME}",
                        startAction = {
                            Icon(AppIcons.Refresh, null, Modifier.size(22.dp))
                        },
                        onClick = {
                            if (checking) return@ArrowPreference
                            checking = true
                            UpdateChecker.check { result ->
                                checking = false
                                when (result) {
                                    is UpdateResult.Available -> update = result
                                    UpdateResult.UpToDate -> onNotify("已经最新啦 ٩(ˊᗜˋ*)و")
                                    is UpdateResult.Failed -> onNotify("检查失败：" + result.message)
                                }
                            }
                        },
                    )
                }
            }
        }

        item(key = "debug") {
            if (AppState.debugMode) {
                Column {
                    SmallTitle(text = "调试")
                    Card(modifier = Modifier.fillMaxWidth()) {
                        // Verifying the crash screen needs a crash, and waiting for a real bug to
                        // happen is not a test. Thrown on the main thread so the uncaught handler
                        // (and the report screen) see it exactly like a real one.
                        ArrowPreference(
                            title = "模拟崩溃",
                            summary = "崩一次，看看报告页",
                            startAction = {
                                Icon(AppIcons.Debug, null, Modifier.size(22.dp))
                            },
                            onClick = { throw IllegalStateException("模拟崩溃：这是调试里手动触发的") },
                        )
                    }
                }
            }
        }

        item(key = "footer") {
            Text(
                text = "AGPL-3.0 · 一个壳子 (･ω<)☆",
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }

    if (showCrash) {
        val report = crashLog
        OverlayDialog(
            show = true,
            title = "崩溃日志",
            summary = runCatching { CrashHandler.file(context).absolutePath }.getOrDefault("?"),
            onDismissRequest = { showCrash = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (report.isNullOrBlank()) {
                    Text("没有崩溃记录。", style = MiuixTheme.textStyles.body2)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = report,
                            style = MiuixTheme.textStyles.footnote2,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    Button(
                        onClick = {
                            clipboard.setText(AnnotatedString(report))
                            onNotify("已复制崩溃日志")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("复制")
                    }
                    Button(
                        onClick = {
                            CrashHandler.clear(context)
                            crashLog = null
                            showCrash = false
                            onNotify("已清空")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("清空")
                    }
                }
                Button(onClick = { showCrash = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("关闭")
                }
            }
        }
    }

    deviceInfo?.let { info ->
        OverlayDialog(
            show = true,
            title = "设备信息",
            onDismissRequest = { deviceInfo = null },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = info,
                        style = MiuixTheme.textStyles.footnote2,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(info))
                        onNotify("已复制设备信息")
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("复制")
                }
                Button(onClick = { deviceInfo = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("关闭")
                }
            }
        }
    }

    update?.let { available ->
        OverlayDialog(
            show = true,
            title = "发现新版本 v${available.version}",
            onDismissRequest = { update = null },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (available.notes.isBlank()) {
                    Text("这个版本没有写更新说明。", style = MiuixTheme.textStyles.body2)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(available.notes, style = MiuixTheme.textStyles.footnote1)
                    }
                }
                Button(
                    onClick = {
                        val target = available.apkUrl
                        if (target != null) uriHandler.openUri(target) else UpdateChecker.openReleasePage(context)
                        update = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (available.apkUrl != null) "下载" else "打开发布页")
                }
                Button(onClick = { update = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("关闭")
                }
            }
        }
    }
}

/** The brand block: the mark, the name, the version. */
@Composable
private fun AppHeader(onNotify: (String) -> Unit) {
    val context = LocalContext.current
    var taps by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "NekoNote",
            style = MiuixTheme.textStyles.title1,
            color = MiuixTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Seven taps on the signature line put the diagnostic rows on show; nothing says so.
        Text(
            text = "Ciallo～(∠・ω c)⌒★",
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
