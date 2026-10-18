/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.notes

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold
import top.youzix.nekonote.ui.material3.components.IconTile
import top.youzix.nekonote.ui.material3.components.SegmentedColumn
import top.youzix.nekonote.ui.material3.components.SegmentedListItem
import top.youzix.nekonote.ui.material3.components.TonalCard
import top.youzix.nekonote.ui.material3.components.expressiveTopAppBarColors

/**
 * 标签 — the note store seen by tag, pinned and recency, Material flavour.
 */
@Composable
fun MaterialTagsScreen(
    outerPadding: PaddingValues,
    onOpenTag: (String) -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val tags = NoteRepository.tags()
    val pinned = NoteRepository.pinned()
    val recent = NoteRepository.recent()

    ExpressiveScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("标签") },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = paddingValues.calculateBottomPadding() + outerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "stats") {
                CountsCard()
            }

            item(key = "tags") {
                Column {
                    if (tags.isEmpty()) {
                        Text(
                            text = "还没有标签。在正文里写 #标签 就会自动出现。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    } else {
                        SegmentedColumn(title = "标签") {
                            tags.forEach { tag ->
                                item(key = tag.tag) {
                                    SegmentedListItem(
                                        onClick = { onOpenTag(tag.tag) },
                                        headlineContent = { Text("#${tag.tag}") },
                                        supportingContent = { Text("${tag.count} 条笔记") },
                                        leadingContent = { IconTile(AppIcons.Tag, null) },
                                        trailingContent = {
                                            Icon(AppIcons.ChevronRight, null)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (pinned.isNotEmpty()) {
                item(key = "pinned") {
                    SegmentedColumn(title = "置顶") {
                        pinned.forEach { note ->
                            item(key = note.id) {
                                SegmentedListItem(
                                    onClick = { onOpenNote(note.id) },
                                    headlineContent = {
                                        Text(
                                            text = note.displayTitle,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = note.excerpt.ifBlank { "${note.charCount} 字" },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    leadingContent = { IconTile(AppIcons.Pin, "已置顶") },
                                )
                            }
                        }
                    }
                }
            }

            item(key = "recent") {
                SegmentedColumn(title = "最近编辑") {
                    recent.forEach { note ->
                        item(key = note.id) {
                            SegmentedListItem(
                                onClick = { onOpenNote(note.id) },
                                headlineContent = {
                                    Text(
                                        text = note.displayTitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = note.excerpt.ifBlank { "${note.charCount} 字" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                leadingContent = { IconTile(AppIcons.Notes, null) },
                            )
                        }
                    }
                }
            }

            item(key = "footer") {
                Text(
                    text = "标签就是正文里的 #标签，不单独存一份。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CountsCard() {
    TonalCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Stat("${NoteRepository.noteCount()}", "笔记", Modifier.weight(1f))
            Stat("${NoteRepository.pinned().size}", "置顶", Modifier.weight(1f))
            Stat("${NoteRepository.tags().size}", "标签", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
