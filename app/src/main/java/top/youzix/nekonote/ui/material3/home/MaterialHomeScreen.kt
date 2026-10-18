package top.youzix.nekonote.ui.material3.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import top.youzix.nekonote.AppState
import top.youzix.nekonote.BuildConfig
import top.youzix.nekonote.demo.HomeActions
import top.youzix.nekonote.demo.HomeTarget
import top.youzix.nekonote.demo.HomeUiState
import top.youzix.nekonote.demo.SystemInfo
import top.youzix.nekonote.demo.demoHomeState
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold
import top.youzix.nekonote.ui.material3.components.IconTile
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedListItem
import top.youzix.nekonote.ui.material3.components.StatusTag
import top.youzix.nekonote.ui.material3.components.TonalCard
import top.youzix.nekonote.ui.material3.components.expressiveTopAppBarColors
import top.youzix.nekonote.ui.miuix.components.WarningLevel

private val HeroShape = RoundedCornerShape(28.dp)
private val TileShape = RoundedCornerShape(22.dp)

@Composable
fun HomePagerMaterial(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ExpressiveScaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                state = state,
                actions = actions,
            )
            if (state.showManagerPrBuildWarning && state.showFullStatus) {
                WarningCard(stringResource(id = R.string.home_pr_build_warning), level = WarningLevel.Notice)
            } else if (state.showKernelPrBuildWarning && state.showFullStatus) {
                WarningCard(stringResource(id = R.string.home_pr_kernel_warning), level = WarningLevel.Notice)
            }
            if (state.requiresNewKernel && state.showFullStatus) {
                WarningCard(
                    stringResource(
                        id = if (state.canInstallKernelUpdate) R.string.require_kernel_version else R.string.require_kernel_version_gki
                    ),
                    onClick = if (state.canInstallKernelUpdate) actions.onInstallClick else null
                )
            }
            if (state.requiresNewManager) {
                WarningCard(
                    stringResource(
                        id = R.string.require_manager_version
                    )
                )
            }
            if (state.showLkmUpdate && state.showFullStatus) {
                WarningCard(
                    message = stringResource(R.string.home_lkm_update_available),
                    level = WarningLevel.Notice,
                    onClick = actions.onInstallClick,
                )
            }
            if (state.showRootWarning) {
                WarningCard(stringResource(id = R.string.grant_root_failed))
            }
            StatsRow(state = state, actions = actions)
            InfoCard(systemInfo = state.systemInfo, showFullStatus = state.showFullStatus)
            SupportLinks(onOpenUrl = actions.onOpenUrl)
            Spacer(
                Modifier.height(
                    bottomInnerPadding + if (!true)
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
                )
            )
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeFlexibleTopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        colors = expressiveTopAppBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun StatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val ksuActive = state.ksuVersion != null
    val onClick = {
        if (!state.isLateLoadMode) {
            actions.onInstallClick()
        }
    }
    if (ksuActive) {
        ActiveStatusCard(state = state, onClick = onClick)
    } else {
        InactiveStatusCard(
            state = state,
            actions = actions,
            notInstalled = state.kernelVersion.isGKI(),
            onClick = onClick,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveStatusCard(
    state: HomeUiState,
    onClick: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val workingMode = when (state.lkmMode) {
        null -> ""
        true -> "LKM"
        else -> "Built-in"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shape = HeroShape,
        onClick = onClick,
    ) {
        Box(modifier = Modifier.drawBehind { drawStatusRings(contentColor.copy(alpha = 0.18f)) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 188.dp)
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (workingMode.isNotEmpty()) {
                        StatusTag(
                            label = workingMode,
                            contentColor = containerColor,
                            backgroundColor = contentColor,
                        )
                    }
                    if (state.showCustomLkmBadge) {
                        StatusTag(
                            label = stringResource(R.string.home_lkm_custom),
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    }
                    if (state.isSafeMode) {
                        StatusTag(
                            label = stringResource(id = R.string.safe_mode),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    }
                    if (state.isLateLoadMode) {
                        StatusTag(
                            label = stringResource(id = R.string.jailbreak_mode),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.home_working),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} · ${AppState.engine.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun InactiveStatusCard(
    state: HomeUiState,
    actions: HomeActions,
    notInstalled: Boolean,
    onClick: () -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val accentColor = MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceBright,
        shape = HeroShape,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind { drawDashedBorder(outlineColor) }
                .heightIn(min = 188.dp)
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (notInstalled) Icons.Rounded.Warning else Icons.Rounded.Block,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(
                        if (notInstalled) R.string.home_click_to_install else R.string.home_unsupported_reason
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                )
            }
            Text(
                text = stringResource(if (notInstalled) R.string.home_not_installed else R.string.home_unsupported),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
            )
            if (notInstalled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = actions.onInstallClick) {
                        Text(stringResource(R.string.install))
                    }
                    if (state.isSELinuxPermissive) {
                        OutlinedButton(onClick = actions.onJailbreakClick) {
                            Text(stringResource(R.string.home_jailbreak))
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStatusRings(color: Color) {
    val center = Offset(size.width - 36.dp.toPx(), 32.dp.toPx())
    val stroke = 2.dp.toPx()
    drawCircle(color = color, radius = 112.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(
        color = color,
        radius = 84.dp.toPx(),
        center = center,
        style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 8.dp.toPx()))),
    )
    drawCircle(color = color, radius = 56.dp.toPx(), center = center, style = Stroke(stroke))
    drawCircle(color = color, radius = 18.dp.toPx(), center = center)
}

private fun DrawScope.drawDashedBorder(color: Color) {
    val stroke = 2.dp.toPx()
    val radius = 28.dp.toPx() - stroke / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = size.copy(width = size.width - stroke, height = size.height - stroke),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
    )
}

@Composable
private fun StatsRow(
    state: HomeUiState,
    actions: HomeActions,
) {
    val tiles = buildList<Triple<Int, Int, (() -> Unit)?>> {
        state.superuserCount?.let { add(Triple(it, R.string.superuser, actions.onSuperuserClick)) }
        state.moduleCount?.let { add(Triple(it, R.string.module, actions.onModuleClick)) }
        state.kpmCount?.let { add(Triple(it, R.string.kpm_title, null)) }
    }
    if (tiles.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { (count, label, onClick) ->
            StatTile(
                count = count,
                label = stringResource(label),
                onClick = onClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(
    count: Int,
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val content = @Composable {
        Column(
            modifier = Modifier
                .height(88.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = TileShape,
            color = MaterialTheme.colorScheme.surfaceBright,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = TileShape,
            color = MaterialTheme.colorScheme.surfaceBright,
            content = content,
        )
    }
}

@Composable
private fun WarningCard(
    message: String,
    level: WarningLevel = WarningLevel.Error,
    onClick: (() -> Unit)? = null
) {
    val containerColor = when (level) {
        WarningLevel.Error -> MaterialTheme.colorScheme.errorContainer
        WarningLevel.Notice -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.contentColorFor(containerColor)
            )
        }
    }
    if (onClick != null) {
        TonalCard(containerColor = containerColor, onClick = onClick, content = content)
    } else {
        TonalCard(containerColor = containerColor, content = content)
    }
}

@Composable
private fun SupportLinks(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val learnMoreUrl = stringResource(R.string.home_learn_kernelsu_url)

    SegmentedColumn(modifier = modifier.fillMaxWidth()) {
        item {
            SegmentedListItem(
                onClick = { onOpenUrl("https://github.com/Youzix-Star/NekoNote") },
                headlineContent = { Text(stringResource(R.string.home_support_title)) },
                supportingContent = { Text(stringResource(R.string.home_support_content)) },
                leadingContent = {
                    IconTile(Icons.Filled.VolunteerActivism, stringResource(R.string.home_support_title))
                },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            )
        }
        item {
            SegmentedListItem(
                onClick = { onOpenUrl(learnMoreUrl) },
                headlineContent = { Text(stringResource(R.string.home_learn_kernelsu)) },
                supportingContent = { Text(stringResource(R.string.home_click_to_learn_kernelsu)) },
                leadingContent = {
                    IconTile(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.home_learn_kernelsu))
                },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            )
        }
    }
}

@Composable
private fun SpecSheet(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = TileShape,
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier,
    showFullStatus: Boolean = true,
) {
    val selinuxDisplay = when (systemInfo.selinuxStatus) {
        "Enforcing" -> stringResource(R.string.selinux_status_enforcing)
        "Permissive" -> stringResource(R.string.selinux_status_permissive)
        "Disabled" -> stringResource(R.string.selinux_status_disabled)
        else -> stringResource(R.string.selinux_status_unknown)
    }
    val seccompDisplay = when (systemInfo.seccompStatus) {
        -1 -> stringResource(R.string.seccomp_status_not_supported)
        0 -> stringResource(R.string.seccomp_status_disabled)
        1 -> stringResource(R.string.seccomp_status_strict)
        2 -> stringResource(R.string.seccomp_status_filter)
        else -> stringResource(R.string.seccomp_status_unknown)
    }

    val manualHookText = stringResource(R.string.manual_hook)
    val inlineHookText = stringResource(R.string.inline_hook)
    val tracepointHookText = stringResource(R.string.tracepoint_hook)
    val unknownHookText = stringResource(R.string.selinux_status_unknown)
    val isSusfsSupported = false
    val hookTypeLabel: String? = null

    val systemRows = buildList {
        add(stringResource(R.string.home_manager_version) to systemInfo.managerVersion)
        add(stringResource(R.string.home_kernel) to systemInfo.kernelVersion)
        if (showFullStatus) {
            if (!systemInfo.kernelFullVersion.isNullOrBlank()) {
                add(stringResource(R.string.home_kernel_full_version) to systemInfo.kernelFullVersion)
            }
            if (isSusfsSupported) {
                // demo build has no SUSFS
            } else if (!hookTypeLabel.isNullOrBlank()) {
                add(stringResource(R.string.hook_type) to hookTypeLabel)
            }
            if (!systemInfo.zygiskImplementation.isNullOrBlank()) {
                add(stringResource(R.string.home_zygisk_implementation) to systemInfo.zygiskImplementation)
            }
        }
        add(stringResource(R.string.home_device_model) to systemInfo.deviceModel)
        add(stringResource(R.string.home_fingerprint) to systemInfo.fingerprint)
    }
    val securityRows = listOf(
        stringResource(R.string.home_selinux_status) to selinuxDisplay,
        stringResource(R.string.home_seccomp_status) to seccompDisplay,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpecSheet(rows = systemRows)
        SpecSheet(rows = securityRows)
    }
}



/**
 * The shell's entry point: SukiSU's home page, fed with this build's demo state.
 *
 * The original screen is kept verbatim; only the state and the string table are ours, which is why
 * the paddings below are exactly SukiSU's.
 */
@Composable
fun MaterialHomeScreen(
    outerPadding: PaddingValues,
    onOpenLink: (HomeTarget) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val actions = HomeActions(
        onOpenUrl = { uri ->
            if (uri == "neko://licenses") {
                onOpenLink(HomeTarget.Licenses)
            } else {
                runCatching { uriHandler.openUri(uri) }
            }
        },
    )
    HomePagerMaterial(
        state = demoHomeState(),
        actions = actions,
        bottomInnerPadding = outerPadding.calculateBottomPadding(),
    )
}

// ---- SukiSU's string resources, replaced by a lookup table so the screen code stays untouched ----

private object R {
    object string {
        const val app_name = 1
        const val home_working = 2
        const val home_working_version = 3
        const val safe_mode = 4
        const val jailbreak_mode = 5
        const val home_lkm_custom = 6
        const val home_lkm_update_available = 7
        const val home_pr_build_warning = 8
        const val home_pr_kernel_warning = 9
        const val require_kernel_version = 10
        const val require_kernel_version_gki = 11
        const val require_manager_version = 12
        const val grant_root_failed = 13
        const val install = 14
        const val home_click_to_install = 15
        const val home_not_installed = 16
        const val home_unsupported = 17
        const val home_unsupported_reason = 18
        const val home_jailbreak = 19
        const val home_manager_version = 20
        const val home_kernel = 21
        const val home_kernel_full_version = 22
        const val home_device_model = 23
        const val home_fingerprint = 24
        const val home_selinux_status = 25
        const val home_seccomp_status = 26
        const val home_zygisk_implementation = 27
        const val home_susfs_version = 28
        const val hook_type = 29
        const val manual_hook = 30
        const val inline_hook = 31
        const val tracepoint_hook = 32
        const val selinux_status_enforcing = 33
        const val selinux_status_permissive = 34
        const val selinux_status_disabled = 35
        const val selinux_status_unknown = 36
        const val seccomp_status_not_supported = 37
        const val seccomp_status_disabled = 38
        const val seccomp_status_strict = 39
        const val seccomp_status_filter = 40
        const val seccomp_status_unknown = 41
        const val home_support_title = 42
        const val home_support_content = 43
        const val home_learn_kernelsu = 44
        const val home_click_to_learn_kernelsu = 45
        const val home_learn_kernelsu_url = 46
        const val module_changelog = 47
        const val module_update = 48
        const val new_version_available = 49
        const val superuser = 50
        const val module = 51
        const val kpm_title = 52
    }
}

private val HOME_STRINGS: Map<Int, String> = mapOf(
    1 to "NekoNote",
    2 to "运行中 ヽ(•‿•)ノ",
    3 to "版本 %1\$s",
    4 to "安全模式",
    5 to "越狱模式",
    6 to "自定义",
    7 to "有模块更新",
    8 to "这是 PR 构建",
    9 to "这是 PR 内核",
    10 to "需要更新内核",
    11 to "当前内核不支持，需要 GKI 内核",
    12 to "需要更新管理器",
    13 to "获取 Root 失败",
    14 to "装上",
    15 to "点一下装上",
    16 to "还没装",
    17 to "不支持",
    18 to "这台设备跑不了",
    19 to "越狱",
    20 to "版本",
    21 to "系统",
    22 to "完整内核版本",
    23 to "机型",
    24 to "系统指纹",
    25 to "SELinux",
    26 to "Seccomp",
    27 to "Zygisk 实现",
    28 to "SUSFS 版本",
    29 to "Hook 方式",
    30 to "手动",
    31 to "内联",
    32 to "Tracepoint",
    33 to "Enforcing",
    34 to "Permissive",
    35 to "已禁用",
    36 to "未知",
    37 to "不支持",
    38 to "已禁用",
    39 to "严格",
    40 to "过滤",
    41 to "未知",
    42 to "看源码",
    43 to "GitHub 上的 NekoNote",
    44 to "开源许可",
    45 to "依赖的许可",
    46 to "neko://licenses",
    47 to "更新说明",
    48 to "更新",
    49 to "发现新版本 %1\$d",
    50 to "已授权",
    51 to "模块",
    52 to "KPM",
)

private fun stringResource(id: Int, vararg formatArgs: Any): String {
    val template = HOME_STRINGS[id] ?: ""
    return if (formatArgs.isEmpty()) template else String.format(template, *formatArgs)
}
