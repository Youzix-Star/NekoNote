/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * SukiSU-Ultra's home state model (GPL-3.0), filled with demo values: the screen files are ported
 * verbatim, so they expect exactly these shapes.
 */

package top.youzix.nekonote.demo

import android.os.Build
import androidx.compose.runtime.Immutable
import top.youzix.nekonote.AppState
import top.youzix.nekonote.BuildConfig

/** The kernel version string, plus the one question the home page asks about it. */
@Immutable
data class KernelVersion(val version: String) {
    fun isGKI(): Boolean = version.contains("android")
}

/** The system sheet's rows. */
@Immutable
data class SystemInfo(
    val kernelVersion: String,
    val managerVersion: String,
    val deviceModel: String,
    val kernelFullVersion: String? = null,
    val fingerprint: String = "",
    val selinuxStatus: String = "Enforcing",
    val seccompStatus: Int = 2,
    val zygiskImplementation: String? = null,
)

/** A release the update card would offer; `0` means there is none. */
@Immutable
data class LatestVersionInfo(
    val versionCode: Long = 0,
    val changelog: String = "",
    val downloadUrl: String = "",
)

@Immutable
data class HomeUiState(
    val kernelVersion: KernelVersion,
    val ksuVersion: Int?,
    val managerUAPIVersion: Int,
    val kernelUAPIVersion: Int?,
    val lkmMode: Boolean?,
    val isLkmBundled: Boolean,
    val isManager: Boolean,
    val isManagerPrBuild: Boolean,
    val isKernelPrBuild: Boolean,
    val requiresNewKernel: Boolean,
    val requiresNewManager: Boolean,
    val isRootAvailable: Boolean,
    val isSafeMode: Boolean,
    val isLateLoadMode: Boolean,
    val checkUpdateEnabled: Boolean,
    val latestVersionInfo: LatestVersionInfo,
    val currentManagerVersionCode: Long,
    val systemInfo: SystemInfo,
    val showFullStatus: Boolean = true,
    val superuserCount: Int? = null,
    val moduleCount: Int? = null,
    val kpmCount: Int? = null,
) {
    val isSELinuxPermissive: Boolean
        get() = systemInfo.selinuxStatus == "Permissive"

    val showLkmUpdate: Boolean
        get() = isManager &&
            lkmMode == true &&
            isLkmBundled &&
            ksuVersion?.toLong() != currentManagerVersionCode &&
            !requiresNewKernel &&
            !requiresNewManager

    // Jailbreak mode runs on locked bootloaders, so flashing a boot image would brick the device.
    val canInstallKernelUpdate: Boolean
        get() = lkmMode == true && !isLateLoadMode

    val showCustomLkmBadge: Boolean
        get() = lkmMode == true && !isLkmBundled

    val showRootWarning: Boolean
        get() = ksuVersion != null && !isRootAvailable

    val showManagerPrBuildWarning: Boolean
        get() = isManager && isManagerPrBuild

    val showKernelPrBuildWarning: Boolean
        get() = isManager && !isManagerPrBuild && isKernelPrBuild

    val hasUpdate: Boolean
        get() = latestVersionInfo.versionCode > currentManagerVersionCode
}

@Immutable
data class HomeActions(
    val onInstallClick: () -> Unit = {},
    val onOpenUrl: (String) -> Unit = {},
    val onJailbreakClick: () -> Unit = {},
    val onSuperuserClick: () -> Unit = {},
    val onModuleClick: () -> Unit = {},
)

/** A demo state that shows the "working" card, with this build's real versions in the sheet. */
fun demoHomeState(): HomeUiState = HomeUiState(
    kernelVersion = KernelVersion("Android ${Build.VERSION.RELEASE}"),
    ksuVersion = 1,
    managerUAPIVersion = 1,
    kernelUAPIVersion = 1,
    lkmMode = true,
    isLkmBundled = true,
    isManager = true,
    isManagerPrBuild = false,
    isKernelPrBuild = false,
    requiresNewKernel = false,
    requiresNewManager = false,
    isRootAvailable = true,
    isSafeMode = false,
    isLateLoadMode = false,
    checkUpdateEnabled = false,
    latestVersionInfo = LatestVersionInfo(),
    currentManagerVersionCode = 1,
    systemInfo = SystemInfo(
        kernelVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        managerVersion = BuildConfig.VERSION_NAME,
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
        fingerprint = Build.FINGERPRINT,
        selinuxStatus = "Enforcing",
        seccompStatus = 2,
        zygiskImplementation = AppState.engine.label,
    ),
    superuserCount = 5,
    moduleCount = 24,
    kpmCount = 2,
)
