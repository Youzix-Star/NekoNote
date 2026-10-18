/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * The shell's navigation is SukiSU-Ultra's (GPL-3.0): miuix-nav's `NavDisplay` owns the back stack
 * and the platform predictive-back gesture, so there is no hand-written back animation here.
 */

package top.youzix.nekonote.ui.miuix

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.UiEngine
import top.youzix.nekonote.ui.UiEnginePrefs
import top.youzix.nekonote.ui.miuix.about.AboutScreen
import top.youzix.nekonote.ui.miuix.licenses.LicensesScreen
import top.youzix.nekonote.ui.miuix.liquid.FloatingBottomBar
import top.youzix.nekonote.ui.miuix.notes.NoteEditorScreen
import top.youzix.nekonote.ui.miuix.notes.NotesScreen
import top.youzix.nekonote.ui.miuix.notes.TagsScreen
import top.youzix.nekonote.ui.miuix.settings.SettingsScreen
import top.youzix.nekonote.ui.nav.Route
import top.youzix.nekonote.ui.rememberMainPagerState
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.nav.core.NavController
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.navBackStackOf
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.theme.MiuixTheme

const val TAB_NOTES = 0
const val TAB_TAGS = 1
const val TAB_SETTINGS = 2

/** The miuix engine: large-title app bar, a pager of tabs, and the liquid-glass bottom bar. */
@Composable
fun MiuixApp() {
    val context = LocalContext.current
    MiuixAppTheme(colorMode = AppState.colorMode) {
        MiuixShell(
            engine = AppState.engine,
            onEngineChange = {
                AppState.engine = it
                UiEnginePrefs.save(context, it)
            },
        )
    }
}

@Composable
fun MiuixShell(
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
) {
    val useLiquidGlass = AppState.useBlur
    val navigationItems = remember {
        listOf(
            NavigationItem(label = "笔记", icon = AppIcons.Notes),
            NavigationItem(label = "标签", icon = AppIcons.Tag),
            NavigationItem(label = "设置", icon = AppIcons.Settings),
        )
    }
    val titles = remember { listOf("笔记", "标签", "设置") }

    val pagerState = rememberPagerState(pageCount = { navigationItems.size })
    val mainPagerState = rememberMainPagerState(pagerState)
    val mainScrollBehavior = MiuixScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    val backStack = remember { navBackStackOf(Route.Main) }
    val navigator = remember { NavController(backStack) }
    val notify: (String) -> Unit = { message ->
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Adopt the pager's position after the user swipes between tabs by hand.
    LaunchedEffect(pagerState.currentPage) {
        mainPagerState.syncPage()
        CrashHandler.note("页签 ${titles.getOrNull(pagerState.currentPage) ?: pagerState.currentPage}")
    }

    // ---- level-1 back: no second-level page is open, so back returns to the first tab ----
    BackHandler(enabled = backStack.size == 1 && mainPagerState.selectedPage != TAB_NOTES) {
        mainPagerState.animateToPage(TAB_NOTES)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            navController = navigator,
            effects = NavDisplayEffects(cornerClipRadius = rememberNavSystemCornerRadius()),
            onBack = { navigator.pop() },
        ) {
            entry<Route.Main> {
                LaunchedEffect(Unit) { CrashHandler.note("一级页面") }
                MiuixTabs(
                    titles = titles,
                    navigationItems = navigationItems,
                    pagerState = pagerState,
                    selectedPage = mainPagerState.selectedPage,
                    scrollBehavior = mainScrollBehavior,
                    backdrop = backdrop,
                    useLiquidGlass = useLiquidGlass,
                    engine = engine,
                    onEngineChange = onEngineChange,
                    onNotify = notify,
                    onOpenNote = { navigator.push(Route.Editor(it)) },
                    onNewNote = { navigator.push(Route.Editor(NoteRepository.newDraftId())) },
                    onOpenAbout = { navigator.push(Route.About) },
                    onOpenLicenses = { navigator.push(Route.Licenses) },
                    onTabSelected = { index -> mainPagerState.animateToPage(index) },
                )
            }

            entry<Route.Editor> { route ->
                LaunchedEffect(route.noteId) { CrashHandler.note("二级页面 编辑") }
                NoteEditorScreen(
                    noteId = route.noteId,
                    onBack = { navigator.pop() },
                    onNotify = notify,
                )
            }

            entry<Route.About> {
                LaunchedEffect(Unit) { CrashHandler.note("二级页面 关于") }
                AboutScreen(
                    onBack = { navigator.pop() },
                    onOpenLicenses = { navigator.push(Route.Licenses) },
                    onNotify = notify,
                )
            }

            entry<Route.Licenses> {
                LaunchedEffect(Unit) { CrashHandler.note("二级页面 开源许可") }
                LicensesScreen(onBack = { navigator.pop() })
            }
        }

        // The host lives outside the page scaffolds: a message posted from a second-level page
        // must sit above that page, the way a toast would.
        SnackbarHost(
            state = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = 84.dp +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
        )
    }
}

/** Level one: the three tabs. Switching between them is a pager animation, nothing more. */
@Composable
private fun MiuixTabs(
    titles: List<String>,
    navigationItems: List<NavigationItem>,
    pagerState: PagerState,
    selectedPage: Int,
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop,
    useLiquidGlass: Boolean,
    engine: UiEngine,
    onEngineChange: (UiEngine) -> Unit,
    onNotify: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLicenses: () -> Unit,
    onTabSelected: (Int) -> Unit,
) {
    // Each page owns its own top bar (SukiSU does the same), so this Scaffold only owns the
    // bottom bar and the horizontal insets.
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
        bottomBar = {
            // The bar belongs to the bottom edge of the screen, keyboard or no keyboard: it
            // consumes the IME inset here so that nothing above it can lift it out of place.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .consumeWindowInsets(WindowInsets.ime),
            ) {
                FloatingBottomBar(
                    items = navigationItems,
                    selectedIndex = selectedPage,
                    onItemClick = onTabSelected,
                    backdrop = backdrop,
                    isBlurActive = useLiquidGlass,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = 12.dp +
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding(),
                        ),
                )
            }
        },
    ) { innerPadding ->
        val bottomPadding = innerPadding.calculateBottomPadding() + 12.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                // The glass bottom bar samples this layer.
                .layerBackdrop(backdrop),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { page ->
                when (page) {
                    TAB_NOTES -> NotesScreen(
                        bottomPadding = bottomPadding,
                        onOpenNote = onOpenNote,
                        onNewNote = onNewNote,
                        onNotify = onNotify,
                    )

                    TAB_TAGS -> TagsScreen(
                        bottomPadding = bottomPadding,
                        onOpenTag = { tag ->
                            AppState.noteTagFilter = tag
                            onTabSelected(TAB_NOTES)
                        },
                        onOpenNote = onOpenNote,
                    )

                    else -> SettingsScreen(
                        bottomPadding = bottomPadding,
                        engine = engine,
                        onEngineChange = onEngineChange,
                        onOpenAbout = onOpenAbout,
                        onOpenLicenses = onOpenLicenses,
                    )
                }
            }
        }
    }
}
