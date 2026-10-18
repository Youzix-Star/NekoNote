/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.youzix.nekonote.AppState
import top.youzix.nekonote.data.Note
import top.youzix.nekonote.data.NoteDate
import top.youzix.nekonote.data.NotePrefs
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.data.NoteSort
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold
import top.youzix.nekonote.ui.material3.components.IconTile
import top.youzix.nekonote.ui.material3.components.SearchAppBar
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedListItem

/**
 * 笔记 — the note list, Material flavour.
 *
 * Same data and same interactions as the miuix page: the engine switch changes the chrome, never
 * the notes. The search bar in the app bar is SukiSU's contained search, reused as-is.
 */
@Composable
fun MaterialNotesScreen(
    outerPadding: PaddingValues,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    var query by rememberSaveable { mutableStateOf("") }
    var showSort by remember { mutableStateOf(false) }
    val tagFilter = AppState.noteTagFilter
    val sort = AppState.noteSort
    val notes = NoteRepository.visibleNotes(query, tagFilter, sort)

    ExpressiveScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchAppBar(
                title = { Text("笔记", modifier = Modifier.padding(start = 12.dp)) },
                searchText = query,
                onSearchTextChange = { query = it },
                onClearClick = { query = "" },
                snackbarHostState = snackbarHostState,
                actions = {
                    IconButton(onClick = { showSort = true }) {
                        Icon(AppIcons.Sort, "排序")
                    }
                    IconButton(onClick = onNewNote) {
                        Icon(AppIcons.Add, "新建笔记")
                    }
                },
                scrollBehavior = scrollBehavior,
                defaultContent = { bottomPadding, _ ->
                    NotesContent(
                        notes = notes,
                        query = query,
                        bottomPadding = bottomPadding,
                        onOpenNote = onOpenNote,
                        onNewNote = onNewNote,
                        onNotify = onNotify,
                    )
                },
                searchContent = { bottomPadding, _ ->
                    NotesContent(
                        notes = notes,
                        query = query,
                        bottomPadding = bottomPadding,
                        onOpenNote = onOpenNote,
                        onNewNote = onNewNote,
                        onNotify = onNotify,
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { innerPadding ->
        NotesContent(
            notes = notes,
            query = query,
            bottomPadding = innerPadding.calculateBottomPadding() + outerPadding.calculateBottomPadding() + 16.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(outerPadding)
                .padding(top = 4.dp),
            onOpenNote = onOpenNote,
            onNewNote = onNewNote,
            onNotify = onNotify,
        )
    }

    if (showSort) {
        AlertDialog(
            onDismissRequest = { showSort = false },
            title = { Text("排序") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "置顶的笔记始终在最前面。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    NoteSort.entries.forEach { option ->
                        TextButton(
                            onClick = {
                                AppState.noteSort = option
                                NotePrefs.saveSort(context, option)
                                showSort = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = if (option == sort) "✓ ${option.label}" else option.label,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSort = false }) { Text("关闭") }
            },
        )
    }
}

@Composable
private fun NotesContent(
    notes: List<Note>,
    query: String,
    bottomPadding: Dp,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tags = NoteRepository.tags()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = bottomPadding,
        ),
    ) {
        item(key = "filters") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${notes.size} 条笔记 · 自动保存在设备里",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TagChip("全部", AppState.noteTagFilter == null) {
                        AppState.noteTagFilter = null
                    }
                    tags.forEach { tag ->
                        TagChip("#${tag.tag}", AppState.noteTagFilter == tag.tag) {
                            AppState.noteTagFilter =
                                if (AppState.noteTagFilter == tag.tag) null else tag.tag
                        }
                    }
                }
            }
        }

        if (notes.isEmpty()) {
            item(key = "empty") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val filtering = query.isNotBlank() || AppState.noteTagFilter != null
                    Text(
                        text = if (filtering) "没有匹配的笔记" else "还没有笔记",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (filtering) {
                            "换个词，或者清掉筛选再看看。"
                        } else {
                            "写下来的事情会比记在脑子里的多活一会儿。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!filtering) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNewNote) { Text("写第一条") }
                    }
                }
            }
        } else {
            items(notes, key = { it.id }) { note ->
                NoteRow(
                    note = note,
                    onClick = { onOpenNote(note.id) },
                    onLongClick = {
                        NoteRepository.togglePin(note.id)
                        onNotify(if (note.pinned) "已取消置顶" else "已置顶")
                    },
                )
            }
        }

        item(key = "footer") {
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun NoteRow(note: Note, onClick: () -> Unit, onLongClick: () -> Unit) {
    SegmentedColumn {
        item(key = note.id) {
            SegmentedListItem(
                onClick = onClick,
                onLongClick = onLongClick,
                headlineContent = {
                    Text(
                        text = note.displayTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (note.excerpt.isNotBlank()) {
                            Text(
                                text = note.excerpt,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = NoteDate.relative(note.updatedAt) +
                                " · ${note.charCount} 字" +
                                if (note.tags.isEmpty()) "" else "  " + note.tags.joinToString(" ") { "#$it" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                leadingContent = {
                    IconTile(
                        icon = if (note.pinned) AppIcons.Pin else AppIcons.Notes,
                        contentDescription = if (note.pinned) "已置顶" else null,
                    )
                },
            )
        }
    }
}

@Composable
private fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
    )
}
