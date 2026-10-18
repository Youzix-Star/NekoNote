/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.miuix.notes

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.youzix.nekonote.data.Note
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * 标签 — the other way into the notes: by tag, by pinned, by recency.
 *
 * Tags are read out of the note bodies ([top.youzix.nekonote.markdown.Markdown.tagsOf]), so there
 * is no tag manager to keep in sync; tapping one hands the filter to the notes tab.
 */
@Composable
fun TagsScreen(
    bottomPadding: Dp,
    onOpenTag: (String) -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val tags = NoteRepository.tags()
    val pinned = NoteRepository.pinned()
    val recent = NoteRepository.recent()

    Scaffold(
        topBar = {
            BlurredBar(backdrop = null) {
                TopAppBar(title = "标签", scrollBehavior = scrollBehavior)
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
            item(key = "stats") {
                CountsCard()
            }

            item(key = "tags") {
                Column {
                    SmallTitle(text = "标签")
                    Card(modifier = Modifier.fillMaxWidth()) {
                        if (tags.isEmpty()) {
                            Text(
                                text = "还没有标签。在正文里写 #标签 就会自动出现。",
                                style = MiuixTheme.textStyles.footnote1,
                                color = colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(16.dp),
                            )
                        } else {
                            tags.forEach { tag ->
                                BasicComponent(
                                    title = "#${tag.tag}",
                                    summary = "${tag.count} 条笔记",
                                    onClick = { onOpenTag(tag.tag) },
                                    endActions = {
                                        Icon(
                                            imageVector = AppIcons.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = colorScheme.onSurfaceVariantSummary,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (pinned.isNotEmpty()) {
                item(key = "pinned") {
                    Column {
                        SmallTitle(text = "置顶")
                        Card(modifier = Modifier.fillMaxWidth()) {
                            pinned.forEach { note ->
                                NoteRow(note = note, onClick = { onOpenNote(note.id) })
                            }
                        }
                    }
                }
            }

            item(key = "recent") {
                Column {
                    SmallTitle(text = "最近编辑")
                    Card(modifier = Modifier.fillMaxWidth()) {
                        recent.forEach { note ->
                            NoteRow(note = note, onClick = { onOpenNote(note.id) })
                        }
                    }
                }
            }

            item(key = "footer") {
                Text(
                    text = "标签就是正文里的 #标签，不单独存一份。",
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CountsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Stat(value = "${NoteRepository.noteCount()}", label = "笔记", modifier = Modifier.weight(1f))
            Stat(value = "${NoteRepository.pinned().size}", label = "置顶", modifier = Modifier.weight(1f))
            Stat(value = "${NoteRepository.tags().size}", label = "标签", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.title2,
            color = colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote2,
            color = colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun NoteRow(note: Note, onClick: () -> Unit) {
    BasicComponent(
        title = note.displayTitle,
        summary = note.excerpt.ifBlank { "${note.charCount} 字" },
        onClick = onClick,
        endActions = {
            if (note.pinned) {
                Icon(
                    imageVector = AppIcons.Pin,
                    contentDescription = "已置顶",
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.primary,
                )
            }
        },
    )
}
