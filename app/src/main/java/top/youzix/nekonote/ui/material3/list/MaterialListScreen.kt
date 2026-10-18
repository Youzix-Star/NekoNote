/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * The page follows SukiSU-Ultra's SuperUserMaterial.kt (GPL-3.0): the granted/total summary, the
 * filter chips, the search app bar, the pull-to-refresh body and segmented rows.
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.youzix.nekonote.demo.AppEntry
import top.youzix.nekonote.demo.ListDemo
import top.youzix.nekonote.demo.ListFilter
import top.youzix.nekonote.demo.TagTone
import top.youzix.nekonote.ui.material3.components.AppDialog
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold
import top.youzix.nekonote.ui.material3.components.SearchAppBar
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedListItem
import top.youzix.nekonote.ui.material3.components.StatusTag

/**
 * 列表 — a searchable superuser-style list with pull-to-refresh.
 */
@Composable
fun MaterialListScreen(
    outerPadding: PaddingValues,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ListFilter.All) }
    var selected by remember { mutableStateOf<AppEntry?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val entries = ListDemo.filter(query, filter)

    ExpressiveScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchAppBar(
                title = { Text("列表", modifier = Modifier.padding(start = 12.dp)) },
                searchText = query,
                onSearchTextChange = { query = it },
                onClearClick = { query = "" },
                snackbarHostState = snackbarHostState,
                scrollBehavior = scrollBehavior,
                defaultContent = { bottomPadding, _ ->
                    SearchResults(
                        entries = entries,
                        onSelect = { selected = it },
                        bottomPadding = bottomPadding,
                    )
                },
                searchContent = { bottomPadding, _ ->
                    SearchResults(
                        entries = entries,
                        onSelect = { selected = it },
                        bottomPadding = bottomPadding,
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(outerPadding),
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
            state = pullState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullState,
                )
            },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
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
                    AppColumn(entries = entries, onSelect = { selected = it })
                }
            }
        }
    }

    selected?.let { app ->
        AppDialog(title = app.name, onDismissRequest = { selected = null }) {
            Text(text = app.packageName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = app.note,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { selected = null }) { Text("关闭") }
        }
    }
}

/** The granted/total summary and the filter chips, exactly where SukiSU puts them. */
@Composable
private fun ListHeader(
    granted: Int,
    total: Int,
    filter: ListFilter,
    onFilterChange: (ListFilter) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    @Composable
    fun Chip(selected: Boolean, label: String, onClick: () -> Unit) {
        FilterChip(
            selected = selected,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            },
            label = { Text(label) },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
                selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            ),
        )
    }

    Column(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "已授权 $granted / $total 个应用",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Chip(filter == ListFilter.All, "全部") { onFilterChange(ListFilter.All) }
            Chip(filter == ListFilter.Granted, "已授权") { onFilterChange(ListFilter.Granted) }
            Chip(filter == ListFilter.Denied, "未授权") { onFilterChange(ListFilter.Denied) }
        }
    }
}

/** The rows rendered inside the expanded search, where pull-to-refresh has no place. */
@Composable
private fun SearchResults(
    entries: List<AppEntry>,
    onSelect: (AppEntry) -> Unit,
    bottomPadding: Dp,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp + bottomPadding,
        ),
    ) {
        item(key = "apps") {
            AppColumn(entries = entries, onSelect = onSelect)
        }
    }
}

@Composable
private fun AppColumn(entries: List<AppEntry>, onSelect: (AppEntry) -> Unit) {
    SegmentedColumn {
        entries.forEach { app ->
            item(key = app.id) {
                SegmentedListItem(
                    onClick = { onSelect(app) },
                    headlineContent = { Text(app.name) },
                    supportingContent = { Text(app.packageName) },
                    trailingContent = {
                        val (background, content) = tagColors(app.tone)
                        StatusTag(
                            label = app.note,
                            backgroundColor = background,
                            contentColor = content,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun tagColors(tone: TagTone) = when (tone) {
    TagTone.OK -> MaterialTheme.colorScheme.primaryContainer to
        MaterialTheme.colorScheme.onPrimaryContainer
    TagTone.WARN -> MaterialTheme.colorScheme.errorContainer to
        MaterialTheme.colorScheme.onErrorContainer
    TagTone.ACCENT -> MaterialTheme.colorScheme.secondaryContainer to
        MaterialTheme.colorScheme.onSecondaryContainer
    TagTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHigh to
        MaterialTheme.colorScheme.onSurfaceVariant
}
