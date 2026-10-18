/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * The page is SukiSU-Ultra's SuperUserMiuix.kt (GPL-3.0): a `SearchStatus` that animates the top
 * bar away, a fake search field in the bar, the expanded search pager, and a pull-to-refresh list.
 */

package top.youzix.nekonote.ui.miuix.list

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.youzix.nekonote.demo.AppEntry
import top.youzix.nekonote.demo.ListDemo
import top.youzix.nekonote.demo.ListFilter
import top.youzix.nekonote.demo.TagTone
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.youzix.nekonote.ui.miuix.components.SearchBarFake
import top.youzix.nekonote.ui.miuix.components.SearchBox
import top.youzix.nekonote.ui.miuix.components.SearchPager
import top.youzix.nekonote.ui.miuix.components.SearchStatus
import top.youzix.nekonote.ui.miuix.components.StatusTag
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * 列表 — a superuser-shaped list with SukiSU's expanding search.
 */
@Composable
fun ListScreen(bottomPadding: Dp) {
    var searchStatus by remember { mutableStateOf(SearchStatus(label = "搜索应用")) }
    var filter by remember { mutableStateOf(ListFilter.All) }
    var selected by remember { mutableStateOf<AppEntry?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val scrollBehavior = MiuixScrollBehavior()
    val entries = ListDemo.filter(searchStatus.searchText, filter)

    Scaffold(
        topBar = {
            BlurredBar(backdrop = null) {
                searchStatus.TopAppBarAnim {
                    TopAppBar(
                        title = "列表",
                        scrollBehavior = scrollBehavior,
                        bottomContent = {
                            Box(
                                modifier = Modifier
                                    .alpha(if (searchStatus.isCollapsed()) 1f else 0f)
                                    .onGloballyPositioned { coordinates ->
                                        with(density) {
                                            val offset = coordinates.positionInWindow().y.toDp()
                                            if (searchStatus.offsetY != offset) {
                                                searchStatus = searchStatus.copy(offsetY = offset)
                                            }
                                        }
                                    }
                                    .then(
                                        if (searchStatus.isCollapsed()) {
                                            Modifier.pointerInput(Unit) {
                                                detectTapGestures {
                                                    searchStatus = searchStatus.copy(
                                                        current = SearchStatus.Status.EXPANDING,
                                                    )
                                                }
                                            }
                                        } else {
                                            Modifier
                                        },
                                    ),
                            ) {
                                SearchBarFake(searchStatus.label, 12.dp)
                            }
                        },
                    )
                }
            }
        },
        popupHost = {
            searchStatus.SearchPager(
                onSearchStatusChange = { searchStatus = it },
                defaultResult = {
                    SearchResults(entries = entries, onSelect = { selected = it })
                },
                result = {
                    SearchResults(entries = entries, onSelect = { selected = it })
                },
            )
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        searchStatus.SearchBox {
            // Same padding for the refresh container and the list, as SukiSU does: the
            // indicator is drawn inside this box, so without it the ball sits under the app bar.
            val layoutDirection = LocalLayoutDirection.current
            val listPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 6.dp,
                start = innerPadding.calculateStartPadding(layoutDirection) + 12.dp,
                end = innerPadding.calculateEndPadding(layoutDirection) + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + bottomPadding,
            )
            PullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = {
                    if (!isRefreshing) {
                        isRefreshing = true
                        scope.launch {
                            delay(700)
                            isRefreshing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = listPadding,
                refreshTexts = listOf("下拉刷新", "松开刷新", "正在刷新", "刷新完成"),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "header") {
                        ListHeader(
                            granted = ListDemo.granted,
                            total = ListDemo.total,
                            filter = filter,
                            onFilterChange = { filter = it },
                        )
                    }
                    item(key = "apps") {
                        AppCard(entries = entries, onSelect = { selected = it })
                    }
                }
            }
        }
    }

    selected?.let { app ->
        OverlayDialog(
            show = true,
            title = app.name,
            summary = "${app.packageName} · ${app.note}",
            onDismissRequest = { selected = null },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { selected = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("关闭", style = MiuixTheme.textStyles.button)
                }
            }
        }
    }
}

@Composable
private fun SearchResults(entries: List<AppEntry>, onSelect: (AppEntry) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    ) {
        item(key = "apps") {
            AppCard(entries = entries, onSelect = onSelect)
        }
    }
}

@Composable
private fun AppCard(entries: List<AppEntry>, onSelect: (AppEntry) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        entries.forEach { app ->
            BasicComponent(
                title = app.name,
                summary = app.packageName,
                onClick = { onSelect(app) },
                endActions = {
                    val (background, content) = tagColors(app.tone)
                    StatusTag(label = app.note, backgroundColor = background, contentColor = content)
                },
            )
        }
    }
}

@Composable
private fun ListHeader(
    granted: Int,
    total: Int,
    filter: ListFilter,
    onFilterChange: (ListFilter) -> Unit,
) {
    Column(
        modifier = Modifier.padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "已授权 $granted / $total 个应用",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterButton("全部", filter == ListFilter.All) { onFilterChange(ListFilter.All) }
            FilterButton("已授权", filter == ListFilter.Granted) { onFilterChange(ListFilter.Granted) }
            FilterButton("未授权", filter == ListFilter.Denied) { onFilterChange(ListFilter.Denied) }
        }
    }
}

@Composable
private fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        text = label,
        onClick = onClick,
        colors = if (selected) {
            top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary()
        } else {
            top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors()
        },
    )
}

@Composable
private fun tagColors(tone: TagTone) = when (tone) {
    TagTone.OK -> MiuixTheme.colorScheme.primaryContainer to MiuixTheme.colorScheme.onPrimaryContainer
    TagTone.WARN -> MiuixTheme.colorScheme.errorContainer to MiuixTheme.colorScheme.onErrorContainer
    TagTone.ACCENT -> MiuixTheme.colorScheme.secondaryContainer to
        MiuixTheme.colorScheme.onSecondaryContainer
    TagTone.NEUTRAL -> MiuixTheme.colorScheme.surfaceContainerHigh to
        MiuixTheme.colorScheme.onSurfaceContainerHigh
}
