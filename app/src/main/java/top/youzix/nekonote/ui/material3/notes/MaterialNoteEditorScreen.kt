/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package top.youzix.nekonote.ui.material3.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import top.youzix.nekonote.AppState
import top.youzix.nekonote.data.NoteDate
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.markdown.Markdown
import top.youzix.nekonote.markdown.MarkdownEdit
import top.youzix.nekonote.markdown.MarkdownHighlightColors
import top.youzix.nekonote.markdown.MarkdownTokens
import top.youzix.nekonote.markdown.MarkdownVisualTransformation
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.material3.components.ExpressiveScaffold

/**
 * The editor, Material flavour: same state machine and same save rules as the miuix one, drawn with
 * Material's typography and containers.
 */
@Composable
fun MaterialNoteEditorScreen(
    noteId: String,
    onBack: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val stored = NoteRepository.find(noteId)
    var title by rememberSaveable(noteId) { mutableStateOf(stored?.title.orEmpty()) }
    var body by rememberSaveable(noteId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(stored?.body.orEmpty()))
    }
    var preview by rememberSaveable(noteId) { mutableStateOf(AppState.previewFirst) }
    var showMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    val latestTitle = rememberUpdatedState(title)
    val latestBody = rememberUpdatedState(body.text)
    val latestDeleted = rememberUpdatedState(deleted)

    fun store(newTitle: String, newBody: String) {
        if (!deleted) NoteRepository.touch(noteId, newTitle, newBody)
    }

    LaunchedEffect(title, body.text) {
        if (deleted) return@LaunchedEffect
        if (title.isBlank() && body.text.isBlank()) return@LaunchedEffect
        delay(700)
        NoteRepository.persist()
    }

    DisposableEffect(noteId) {
        onDispose {
            if (latestDeleted.value) return@onDispose
            val finalTitle = latestTitle.value
            val finalBody = latestBody.value
            if (finalTitle.isBlank() && finalBody.isBlank()) {
                NoteRepository.delete(noteId)
            } else {
                NoteRepository.touch(noteId, finalTitle, finalBody)
                NoteRepository.persist()
            }
        }
    }

    fun finish() {
        if (!deleted) {
            if (title.isBlank() && body.text.isBlank()) {
                NoteRepository.delete(noteId)
            } else {
                NoteRepository.touch(noteId, title, body.text)
                NoteRepository.persist()
            }
        }
        onBack()
    }

    fun format(transform: (TextFieldValue) -> TextFieldValue) {
        body = transform(body)
        store(title, body.text)
    }

    val highlightColors = MarkdownHighlightColors(
        syntax = MaterialTheme.colorScheme.primary,
        heading = MaterialTheme.colorScheme.onSurface,
        codeBackground = MaterialTheme.colorScheme.surfaceContainerHigh,
        codeText = MaterialTheme.colorScheme.onSurface,
        tagBackground = MaterialTheme.colorScheme.secondaryContainer,
        tagText = MaterialTheme.colorScheme.onSecondaryContainer,
        link = MaterialTheme.colorScheme.primary,
        muted = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val highlight = remember(highlightColors) { MarkdownVisualTransformation(highlightColors) }
    val bodyLineHeight = (AppState.editorTextSize.bodySp * 1.7f).sp
    val bodyLineHeightPx = with(LocalDensity.current) { bodyLineHeight.toPx() }
    val pageTopPadding = 12.dp
    val pageBottomPadding = 32.dp
    val pageShape = RoundedCornerShape(16.dp)
    val pageScroll = rememberScrollState()
    val ruleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val toolbarInset = if (imeBottom > 0.dp) 0.dp else navigationBottom

    ExpressiveScaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(if (preview) "预览" else "编辑") },
                navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(AppIcons.Back, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { preview = !preview }) {
                        Icon(
                            imageVector = if (preview) AppIcons.Edit else AppIcons.Preview,
                            contentDescription = if (preview) "编辑" else "预览",
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(AppIcons.More, "更多")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        bottomBar = {
            if (!preview) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = toolbarInset)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        FormatButton("#") { format { MarkdownEdit.heading(it) } }
                        FormatButton("*") { format { MarkdownEdit.bold(it) } }
                        FormatButton("_") { format { MarkdownEdit.italic(it) } }
                        FormatButton("~") { format { MarkdownEdit.strike(it) } }
                        FormatButton("-") { format { MarkdownEdit.bullet(it) } }
                        FormatButton("[") { format { MarkdownEdit.task(it) } }
                        FormatButton(">") { format { MarkdownEdit.quote(it) } }
                        FormatButton(MarkdownTokens.TICK) { format { MarkdownEdit.code(it) } }
                        FormatButton("{") { format { MarkdownEdit.codeBlock(it) } }
                        FormatButton("@") { format { MarkdownEdit.link(it) } }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            if (preview) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = title.ifBlank { Markdown.titleOf(body.text) }.ifBlank { "未命名笔记" },
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${Markdown.charCount(body.text)} 字 · " +
                            Markdown.tagsOf(body.text).joinToString(" ") { "#$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    MaterialMarkdown(markdown = body.text)
                    Spacer(Modifier.height(32.dp))
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The title is the note's level-one heading; the marker is visual only.
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    BasicTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            store(it, body.text)
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (title.isEmpty()) {
                                    Text(
                                        text = "标题",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${Markdown.charCount(body.text)} 字 · " +
                        Markdown.tagsOf(body.text).joinToString(" ") { "#$it" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = pageShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(pageShape)
                            .drawBehind {
                                // Ruled paper, shifted by the scroll offset so each rule stays
                                // attached to the text line it divides.
                                val step = bodyLineHeightPx
                                if (step <= 0f) return@drawBehind
                                val top = pageTopPadding.toPx()
                                val shift = pageScroll.value.toFloat() % step
                                var y = (top - shift + step) % step
                                while (y <= size.height) {
                                    drawLine(
                                        color = ruleColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                    y += step
                                }
                            },
                    ) {
                        val minimumFieldHeight = (maxHeight - pageTopPadding - pageBottomPadding)
                            .coerceAtLeast(bodyLineHeight)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(pageScroll)
                                .padding(
                                    start = 14.dp,
                                    end = 14.dp,
                                    top = pageTopPadding,
                                    bottom = pageBottomPadding,
                                ),
                        ) {
                            BasicTextField(
                                value = body,
                                onValueChange = {
                                    body = it
                                    store(title, it.text)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = minimumFieldHeight),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = AppState.editorTextSize.bodySp.sp,
                                    lineHeight = bodyLineHeight,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                visualTransformation = highlight,
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (body.text.isEmpty()) {
                                            Text(
                                                text = "开始写 Markdown…",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMenu) {
        val pinned = NoteRepository.find(noteId)?.pinned == true
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text("这条笔记") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = NoteDate.full(stored?.updatedAt ?: System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = {
                            store(title, body.text)
                            NoteRepository.togglePin(noteId)
                            onNotify(if (pinned) "已取消置顶" else "已置顶")
                            showMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (pinned) "取消置顶" else "置顶")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMenu = false
                        confirmDelete = true
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMenu = false }) { Text("关闭") }
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这条笔记？") },
            text = { Text("删掉就找不回来了。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleted = true
                        NoteRepository.delete(noteId)
                        confirmDelete = false
                        onBack()
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("算了") }
            },
        )
    }
}

@Composable
private fun FormatButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
