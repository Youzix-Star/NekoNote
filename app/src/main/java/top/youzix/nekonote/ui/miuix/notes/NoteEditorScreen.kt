/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.miuix.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/**
 * The editor: a Markdown source field, a formatting rail, and a rendered preview.
 *
 * A note is stored on the first keystroke ([NoteRepository.touch] creates it when the id is still a
 * draft), so the list behind this page is never out of date. Leaving with an entirely blank note
 * deletes it instead of storing an empty one.
 */
@Composable
fun NoteEditorScreen(
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

    val scrollBehavior = MiuixScrollBehavior()
    val highlightColors = MarkdownHighlightColors(
        syntax = colorScheme.primary,
        heading = colorScheme.onSurface,
        codeBackground = colorScheme.surfaceContainerHigh,
        codeText = colorScheme.onSurface,
        tagBackground = colorScheme.secondaryContainer,
        tagText = colorScheme.onSecondaryContainer,
        link = colorScheme.primary,
        muted = colorScheme.onSurfaceVariantSummary,
    )
    val highlight = remember(highlightColors) { MarkdownVisualTransformation(highlightColors) }
    val bodyLineHeight = (AppState.editorTextSize.bodySp * 1.7f).sp
    val bodyLineHeightPx = with(LocalDensity.current) { bodyLineHeight.toPx() }
    val pageTopPadding = 12.dp
    val pageShape = RoundedCornerShape(14.dp)
    val pageScroll = rememberScrollState()
    val ruleColor = colorScheme.onSurface.copy(alpha = 0.08f)
    val latestTitle = rememberUpdatedState(title)
    val latestBody = rememberUpdatedState(body.text)
    val latestDeleted = rememberUpdatedState(deleted)

    fun store(newTitle: String, newBody: String) {
        if (!deleted) NoteRepository.touch(noteId, newTitle, newBody)
    }

    // Typing pauses, then the file follows the memory list.
    LaunchedEffect(title, body.text) {
        if (deleted) return@LaunchedEffect
        if (title.isBlank() && body.text.isBlank()) return@LaunchedEffect
        delay(700)
        NoteRepository.persist()
    }

    // Leaving the page (button, system gesture or engine switch) is the last save point.
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
        val finalTitle = title
        val finalBody = body.text
        if (!deleted) {
            if (finalTitle.isBlank() && finalBody.isBlank()) {
                NoteRepository.delete(noteId)
            } else {
                NoteRepository.touch(noteId, finalTitle, finalBody)
                NoteRepository.persist()
            }
        }
        onBack()
    }

    fun format(transform: (TextFieldValue) -> TextFieldValue) {
        body = transform(body)
        store(title, body.text)
    }

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val toolbarInset = if (imeBottom > 0.dp) 0.dp else navigationBottom

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            BlurredBar(backdrop = null) {
                TopAppBar(
                    title = if (preview) "预览" else "编辑",
                    subtitle = NoteDate.relative(stored?.updatedAt ?: System.currentTimeMillis()),
                    scrollBehavior = scrollBehavior,
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
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(AppIcons.More, "更多", Modifier.size(22.dp))
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!preview) {
                Surface(color = colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = toolbarInset)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
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
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
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
                        style = MiuixTheme.textStyles.title1,
                        color = colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${Markdown.charCount(body.text)} 字 · " +
                            "${Markdown.tagsOf(body.text).joinToString(" ") { "#$it" }}",
                        style = MiuixTheme.textStyles.footnote2,
                        color = colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.height(16.dp))
                    MiuixMarkdown(markdown = body.text)
                    Spacer(Modifier.height(32.dp))
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The note title is the note's level-one heading; the marker is an editing
                    // affordance and is never stored in the body.
                    Text(
                        text = "#",
                        style = MiuixTheme.textStyles.title2.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    BasicTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            store(it, body.text)
                        },
                        singleLine = true,
                        textStyle = MiuixTheme.textStyles.title2.copy(color = colorScheme.onBackground),
                        cursorBrush = SolidColor(colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (title.isEmpty()) {
                                    Text(
                                        text = "标题",
                                        style = MiuixTheme.textStyles.title2,
                                        color = colorScheme.onSurfaceVariantSummary,
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
                        "${Markdown.tagsOf(body.text).joinToString(" ") { "#$it" }}",
                    style = MiuixTheme.textStyles.footnote2,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(pageShape)
                        .background(colorScheme.surfaceContainer)
                        .drawBehind {
                            // Ruled paper: one faint line per text line, shifted by the scroll
                            // offset so the rule and the line it belongs to move together.
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(pageScroll)
                            .padding(horizontal = 14.dp, vertical = pageTopPadding),
                    ) {
                        BasicTextField(
                            value = body,
                            onValueChange = {
                                body = it
                                store(title, it.text)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = AppState.editorTextSize.bodySp.sp,
                                lineHeight = bodyLineHeight,
                                color = colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(colorScheme.primary),
                            visualTransformation = highlight,
                            decorationBox = { innerTextField ->
                                Box {
                                    if (body.text.isEmpty()) {
                                        Text(
                                            text = "开始写 Markdown…",
                                            style = MiuixTheme.textStyles.body2,
                                            color = colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }

    if (showMenu) {
        val pinned = NoteRepository.find(noteId)?.pinned == true
        OverlayDialog(
            show = true,
            title = "这条笔记",
            summary = NoteDate.full(stored?.updatedAt ?: System.currentTimeMillis()),
            onDismissRequest = { showMenu = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        store(title, body.text)
                        NoteRepository.togglePin(noteId)
                        onNotify(if (pinned) "已取消置顶" else "已置顶")
                        showMenu = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (pinned) "取消置顶" else "置顶",
                        style = MiuixTheme.textStyles.button,
                    )
                }
                Button(
                    onClick = {
                        showMenu = false
                        confirmDelete = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("删除笔记", style = MiuixTheme.textStyles.button)
                }
                Button(
                    onClick = { showMenu = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("关闭", style = MiuixTheme.textStyles.button)
                }
            }
        }
    }

    if (confirmDelete) {
        OverlayDialog(
            show = true,
            title = "删除这条笔记？",
            summary = "删掉就找不回来了。",
            onDismissRequest = { confirmDelete = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        deleted = true
                        NoteRepository.delete(noteId)
                        confirmDelete = false
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("删除", style = MiuixTheme.textStyles.button)
                }
                Button(
                    onClick = { confirmDelete = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("算了", style = MiuixTheme.textStyles.button)
                }
            }
        }
    }
}

@Composable
private fun FormatButton(label: String, onClick: () -> Unit) {
    TextButton(
        text = label,
        onClick = onClick,
        minHeight = 36.dp,
        minWidth = 0.dp,
        insideMargin = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        textStyle = MiuixTheme.textStyles.footnote1,
    )
}
