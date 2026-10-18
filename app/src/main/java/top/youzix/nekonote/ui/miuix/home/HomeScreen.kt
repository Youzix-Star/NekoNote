package top.youzix.nekonote.ui.miuix.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.isDynamicColor
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.youzix.nekonote.demo.HomeActions
import top.youzix.nekonote.demo.HomeTarget
import top.youzix.nekonote.demo.HomeUiState
import top.youzix.nekonote.demo.SystemInfo
import top.youzix.nekonote.demo.demoHomeState
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.youzix.nekonote.ui.miuix.components.StatusTag
import top.youzix.nekonote.ui.miuix.components.WarningCard
import top.youzix.nekonote.ui.miuix.components.WarningLevel
import top.youzix.nekonote.ui.miuix.isInDarkTheme

@Composable
fun HomePagerMiuix(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop: LayerBackdrop? = null
    val barColor = colorScheme.surface
    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                backdrop = backdrop,
                barColor = barColor,
            )
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
                        StatusCard(
                            state = state,
                            actions = actions,
                        )
                        InfoCard(
                            systemInfo = state.systemInfo,
                            showFullStatus = state.showFullStatus,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SupportLinks(
                            onOpenUrl = actions.onOpenUrl,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(
                            Modifier.height(
                                bottomInnerPadding + if (!true)
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop?,
    barColor: Color,
) {
    BlurredBar(backdrop) {
        TopAppBar(
            color = barColor,
            title = stringResource(R.string.app_name),
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun StatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    Column {
        when {
            state.ksuVersion != null -> {
                val workingState = buildString {
                    if (state.isSafeMode) {
                        append(" [${stringResource(id = R.string.safe_mode)}]")
                    }
                    if (state.isLateLoadMode) {
                        append(" [${stringResource(id = R.string.jailbreak_mode)}]")
                    }
                }
                val workingMode = when (state.lkmMode) {
                    null -> null
                    true -> "LKM"
                    else -> "Built-in"
                }
                val workingText = "${stringResource(id = R.string.home_working)}$workingState"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(
                            color = when {
                                isDynamicColor -> colorScheme.secondaryContainer
                                isInDarkTheme() -> Color(0xFF1A3825)
                                else -> Color(0xFFDFFAE4)
                            }
                        ),
                        onClick = {
                            if (!state.isLateLoadMode) {
                                actions.onInstallClick()
                            }
                        },
                        showIndication = !state.isLateLoadMode,
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(27.dp, 31.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Icon(
                                    modifier = Modifier.size(110.dp),
                                    imageVector = Icons.Rounded.CheckCircleOutline,
                                    tint = if (isDynamicColor) {
                                        colorScheme.primary.copy(alpha = 0.8f)
                                    } else {
                                        Color(0xFF36D167)
                                    },
                                    contentDescription = null
                                )
                            }
                            if (workingMode != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp, 10.dp),
                                    contentAlignment = Alignment.BottomStart,
                                ) {
                                    Text(
                                        text = workingMode,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp, 14.dp),
                                contentAlignment = Alignment.TopStart,
                            ) {
                                Column {
                                    Text(
                                        text = workingText,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(1.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(
                                                R.string.home_working_version,
                                                "${state.ksuVersion}-${state.kernelUAPIVersion}"
                                            ),
                                            modifier = Modifier.weight(1f, fill = false),
                                            fontSize = 15.sp,
                                        )
                                        if (state.showCustomLkmBadge) {
                                            Spacer(Modifier.width(8.dp))
                                            StatusTag(
                                                label = stringResource(R.string.home_lkm_custom),
                                                contentColor = if (isDynamicColor) {
                                                    colorScheme.onTertiaryContainer
                                                } else if (isInDarkTheme()) {
                                                    Color(0xFFB8E8C5)
                                                } else {
                                                    Color(0xFF164A29)
                                                },
                                                backgroundColor = if (isDynamicColor) {
                                                    colorScheme.tertiaryContainer
                                                } else if (isInDarkTheme()) {
                                                    Color(0xFF315D3E)
                                                } else {
                                                    Color(0xFFB8E8C5)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            state.kernelVersion.isGKI() -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!state.isLateLoadMode) {
                                actions.onInstallClick()
                            }
                        },
                        showIndication = !state.isLateLoadMode,
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        BasicComponent(
                            title = stringResource(R.string.home_not_installed),
                            summary = stringResource(R.string.home_click_to_install),
                            startAction = {
                                Icon(
                                    Icons.Rounded.ErrorOutline,
                                    stringResource(R.string.home_not_installed),
                                    modifier = Modifier.padding(end = 6.dp),
                                    tint = colorScheme.onBackground,
                                )
                            },
                            endActions = {
                                if (state.isSELinuxPermissive) {
                                    TextButton(
                                        text = stringResource(R.string.home_jailbreak),
                                        onClick = actions.onJailbreakClick,
                                        colors = ButtonDefaults.textButtonColorsPrimary()
                                    )
                                }
                            }
                        )
                    }
                }
            }

            else -> {
                Card(
                    onClick = {
                        if (!state.isLateLoadMode) {
                            actions.onInstallClick()
                        }
                    },
                    showIndication = !state.isLateLoadMode,
                    pressFeedbackType = PressFeedbackType.Tilt
                ) {
                    BasicComponent(
                        title = stringResource(R.string.home_unsupported),
                        summary = stringResource(R.string.home_unsupported_reason),
                        startAction = {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                stringResource(R.string.home_unsupported),
                                modifier = Modifier.padding(end = 16.dp),
                                tint = colorScheme.onBackground,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportLinks(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val learnMoreUrl = stringResource(R.string.home_learn_kernelsu_url)

    Card(modifier = modifier) {
        ArrowPreference(
            title = stringResource(R.string.home_support_title),
            summary = stringResource(R.string.home_support_content),
            startAction = {
                Icon(
                    imageVector = Icons.Filled.VolunteerActivism,
                    contentDescription = stringResource(R.string.home_support_title),
                    modifier = Modifier.padding(end = 6.dp),
                    tint = colorScheme.onBackground,
                )
            },
            onClick = { onOpenUrl("https://patreon.com/weishu") },
        )
        ArrowPreference(
            title = stringResource(R.string.home_learn_kernelsu),
            summary = stringResource(R.string.home_click_to_learn_kernelsu),
            startAction = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = stringResource(R.string.home_learn_kernelsu),
                    modifier = Modifier.padding(end = 6.dp),
                    tint = colorScheme.onBackground,
                )
            },
            onClick = { onOpenUrl(learnMoreUrl) },
        )
    }
}

@Composable
private fun InfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier,
    showFullStatus: Boolean = true,
) {
    @Composable
    fun InfoText(
        icon: ImageVector,
        title: String,
        content: String,
        bottomPadding: Dp = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp),
                tint = colorScheme.onSurface,
            )
            Column {
                Text(
                    text = title,
                    fontSize = MiuixTheme.textStyles.headline1.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = content,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoText(
                    icon = Icons.Filled.Tag,
                    title = stringResource(R.string.home_manager_version),
                    content = systemInfo.managerVersion,
                )
                InfoText(
                    icon = Icons.Filled.DeveloperBoard,
                    title = stringResource(R.string.home_kernel),
                    content = systemInfo.kernelVersion,
                )
                if (showFullStatus) {
                    if (!systemInfo.kernelFullVersion.isNullOrBlank()) {
                        InfoText(
                            icon = Icons.Filled.Info,
                            title = stringResource(R.string.home_kernel_full_version),
                            content = systemInfo.kernelFullVersion,
                        )
                    }
                    if (!hookTypeLabel.isNullOrBlank()) {
                        InfoText(
                            icon = Icons.Filled.Build,
                            title = stringResource(R.string.hook_type),
                            content = hookTypeLabel,
                        )
                    }
                    if (!systemInfo.zygiskImplementation.isNullOrBlank()) {
                        InfoText(
                            icon = Icons.Filled.Extension,
                            title = stringResource(R.string.home_zygisk_implementation),
                            content = systemInfo.zygiskImplementation,
                        )
                    }
                }
                InfoText(
                    icon = Icons.Filled.Smartphone,
                    title = stringResource(R.string.home_device_model),
                    content = systemInfo.deviceModel,
                )
                InfoText(
                    icon = Icons.Filled.Fingerprint,
                    title = stringResource(R.string.home_fingerprint),
                    content = systemInfo.fingerprint,
                    bottomPadding = 0.dp,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoText(
                    icon = Icons.Filled.Security,
                    title = stringResource(R.string.home_selinux_status),
                    content = selinuxDisplay,
                )
                InfoText(
                    icon = Icons.Filled.FilterList,
                    title = stringResource(R.string.home_seccomp_status),
                    content = seccompDisplay,
                    bottomPadding = 0.dp,
                )
            }
        }
    }
}


/** The shell's entry point: SukiSU's miuix home page, fed with this build's demo state. */
@Composable
fun HomeScreen(
    bottomPadding: Dp,
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
    HomePagerMiuix(
        state = demoHomeState(),
        actions = actions,
        bottomInnerPadding = bottomPadding,
    )
}

// ---- 同一张字符串表：界面里的文案都在这儿 ----

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
