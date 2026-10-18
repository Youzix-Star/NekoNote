/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.miuix.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.youzix.nekonote.ui.miuix.components.SearchField
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * 笔记 — the note list.
 *
 * The page owns only how the list is being looked at (search text, which tag, sort dialog); the
 * notes themselves come from [NoteRepository] and the filter lives in [AppState] so the tags tab
 * can hand one over before switching here.
 */
@Composable
fun NotesScreen(
    bottomPadding: Dp,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    var query by rememberSaveable { mutableStateOf("") }
    var showSort by remember { mutableStateOf(false) }
    val tagFilter = AppState.noteTagFilter
    val sort = AppState.noteSort
    val notes = NoteRepository.visibleNotes(query, tagFilter, sort)
    val tags = NoteRepository.tags()

    Scaffold(
        topBar = {
            BlurredBar(backdrop = null) {
                TopAppBar(
                    title = "笔记",
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = { showSort = true }) {
                            Icon(AppIcons.Sort, "排序", Modifier.size(22.dp))
                        }
                        IconButton(onClick = onNewNote) {
                            Icon(AppIcons.Add, "新建笔记", Modifier.size(24.dp))
                        }
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .scrollEndHaptic()
                .overScrollVertical(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = innerPadding.calculateTopPadding() + 6.dp,
                bottom = innerPadding.calculateBottomPadding() + bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item(key = "search") {
                SearchField(
                    text = query,
                    onTextChange = { query = it },
                    label = "搜索标题和正文",
                )
            }

            item(key = "tags") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterButton("全部", tagFilter == null) {
                            AppState.noteTagFilter = null
                        }
                        tags.forEach { tag ->
                            FilterButton("#${tag.tag}", tagFilter == tag.tag) {
                                AppState.noteTagFilter = if (tagFilter == tag.tag) null else tag.tag
                            }
                        }
                    }
                    if (tags.isEmpty()) {
                        Text(
                            text = "在正文里写 #标签，就会出现在这里",
                            style = MiuixTheme.textStyles.footnote2,
                            color = colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }

            if (notes.isEmpty()) {
                item(key = "empty") {
                    EmptyNotes(
                        hasQuery = query.isNotBlank() || tagFilter != null,
                        onNewNote = onNewNote,
                    )
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
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
                Text(
                    text = "${notes.size} 条笔记 · ${sort.label} · 自动保存在设备里",
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                )
            }
        }
    }

    if (showSort) {
        OverlayDialog(
            show = true,
            title = "排序",
            summary = "置顶的笔记始终在最前面",
            onDismissRequest = { showSort = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteSort.entries.forEach { option ->
                    Button(
                        onClick = {
                            AppState.noteSort = option
                            NotePrefs.saveSort(context, option)
                            showSort = false
                        },
                        colors = if (option == sort) {
                            ButtonDefaults.buttonColorsPrimary()
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (option == sort) "✓ ${option.label}" else option.label,
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        text = label,
        onClick = onClick,
        colors = if (selected) {
            ButtonDefaults.textButtonColorsPrimary()
        } else {
            ButtonDefaults.textButtonColors()
        },
    )
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
        onClick = onClick,
        onLongPress = onLongClick,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (note.pinned) {
                    Icon(
                        imageVector = AppIcons.Pin,
                        contentDescription = "已置顶",
                        modifier = Modifier.size(15.dp),
                        tint = colorScheme.primary,
                    )
                    Spacer(Modifier.size(6.dp))
                }
                Text(
                    text = note.displayTitle,
                    style = MiuixTheme.textStyles.title4,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            if (note.excerpt.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note.excerpt,
                    style = MiuixTheme.textStyles.body2,
                    color = colorScheme.onSurfaceVariantSummary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = NoteDate.relative(note.updatedAt),
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = " · ${note.charCount} 字",
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
            if (note.tags.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.tags.joinToString("  ") { "#$it" },
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyNotes(hasQuery: Boolean, onNewNote: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (hasQuery) "没有匹配的笔记" else "还没有笔记",
            style = MiuixTheme.textStyles.title3,
            color = colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (hasQuery) {
                "换个词，或者清掉筛选再看看。"
            } else {
                "写下来的事情会比记在脑子里的多活一会儿。"
            },
            style = MiuixTheme.textStyles.footnote1,
            color = colorScheme.onSurfaceVariantSummary,
        )
        if (!hasQuery) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNewNote) {
                Text("写第一条", style = MiuixTheme.textStyles.button)
            }
        }
    }
}
