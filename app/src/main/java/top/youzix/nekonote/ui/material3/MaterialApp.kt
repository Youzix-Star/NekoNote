/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * The shell's navigation is SukiSU-Ultra's (GPL-3.0): miuix-nav's `NavDisplay` owns the back stack
 * and the platform predictive-back gesture.
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.youzix.nekonote.AppState
import top.youzix.nekonote.crash.CrashHandler
import top.youzix.nekonote.demo.HomeTarget
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.ui.material3.about.MaterialAboutScreen
import top.youzix.nekonote.ui.material3.gallery.MaterialGalleryScreen
import top.youzix.nekonote.ui.material3.home.MaterialHomeScreen
import top.youzix.nekonote.ui.material3.licenses.MaterialLicensesScreen
import top.youzix.nekonote.ui.material3.list.MaterialListScreen
import top.youzix.nekonote.ui.material3.settings.MaterialSettingsScreen
import top.youzix.nekonote.ui.material3.widgets.SwipeableSnackbarHost
import top.youzix.nekonote.ui.nav.Route
import top.youzix.nekonote.ui.rememberMainPagerState
import top.yukonga.miuix.kmp.nav.core.NavController
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.navBackStackOf
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius

private const val TAB_HOME = 0
private const val TAB_LIST = 1
private const val TAB_GALLERY = 2
private const val TAB_SETTINGS = 3

@Composable
fun MaterialApp() {
    val context = LocalContext.current
    MaterialAppTheme(colorMode = AppState.colorMode) {
        MaterialShell(
            engine = AppState.engine,
            onEngineChange = {
                AppState.engine = it
                UiEnginePrefs.save(context, it)
            },
        )
    }
}

@Composable
private fun MaterialShell(
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
) {
    val context = LocalContext.current
    val labels = listOf("首页", "列表", "组件", "设置")
    // Filled icon while selected, outlined while not: SukiSU's bar switches between the two.
    val navItems = listOf(
        MaterialNavItem("首页", Icons.Filled.Home, Icons.Outlined.Home),
        MaterialNavItem("列表", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
        MaterialNavItem("组件", Icons.Filled.Widgets, Icons.Outlined.Widgets),
        MaterialNavItem("设置", Icons.Filled.Settings, Icons.Outlined.Settings),
    )

    val pagerState = rememberPagerState(pageCount = { labels.size })
    val mainPagerState = rememberMainPagerState(pagerState)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val notify: (String) -> Unit = { message ->
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    val backStack = remember { navBackStackOf(Route.Main) }
    val navigator = remember { NavController(backStack) }

    // Adopt the pager's position after the user swipes between tabs by hand.
    LaunchedEffect(pagerState.currentPage) {
        mainPagerState.syncPage()
        CrashHandler.note("页签 ${labels.getOrNull(pagerState.currentPage) ?: pagerState.currentPage}")
    }

    // ---- level-1 back: no second-level page is open, so back returns to the first tab ----
    BackHandler(enabled = backStack.size == 1 && mainPagerState.selectedPage != 0) {
        mainPagerState.animateToPage(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            navController = navigator,
            effects = NavDisplayEffects(cornerClipRadius = rememberNavSystemCornerRadius()),
            onBack = { navigator.pop() },
        ) {
            entry<Route.Main> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    // Only the bottom bar's height leaves this scaffold: each page owns its own
                    // app bar and its own window insets, exactly like SukiSU's page scaffolds.
                    // Letting this one add the status-bar inset too is what put an extra
                    // status-bar-height gap under every page's title.
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        // Driven by selectedPage, not the pager's currentPage, so the highlight
                        // lands the moment the tab is tapped.
                        MaterialBottomBar(
                            items = navItems,
                            selectedIndex = mainPagerState.selectedPage,
                            onSelect = { mainPagerState.animateToPage(it) },
                        )
                    },
                ) { outerPadding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                    ) { page ->
                        when (page) {
                            TAB_HOME -> MaterialHomeScreen(
                                outerPadding = outerPadding,
                                onOpenLink = { target ->
                                    when (target) {
                                        HomeTarget.Gallery -> mainPagerState.animateToPage(TAB_GALLERY)
                                        HomeTarget.Licenses -> navigator.push(Route.Licenses)
                                    }
                                },
                            )

                            TAB_LIST -> MaterialListScreen(outerPadding = outerPadding)

                            TAB_GALLERY -> MaterialGalleryScreen(outerPadding = outerPadding)

                            TAB_SETTINGS -> MaterialSettingsScreen(
                                outerPadding = outerPadding,
                                engine = engine,
                                onEngineChange = onEngineChange,
                                onOpenAbout = { navigator.push(Route.About) },
                                onOpenLicenses = { navigator.push(Route.Licenses) },
                            )
                        }
                    }
                }
            }

            entry<Route.About> {
                LaunchedEffect(Unit) { CrashHandler.note("二级页面 关于") }
                MaterialAboutScreen(
                    onBack = { navigator.pop() },
                    useBlur = AppState.useBlur,
                    onOpenLicenses = { navigator.push(Route.Licenses) },
                    onNotify = notify,
                )
            }

            entry<Route.Licenses> {
                LaunchedEffect(Unit) { CrashHandler.note("二级页面 开源许可") }
                MaterialLicensesScreen(
                    onBack = { navigator.pop() },
                    useBlur = AppState.useBlur,
                )
            }
        }

        // The host lives outside the page scaffolds: a message posted from a second-level page
        // must sit above that page, the way a toast would.
        SwipeableSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = 88.dp +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
        )
    }
}
