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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import top.youzix.nekonote.AppState
import top.youzix.nekonote.data.NoteDate
import top.youzix.nekonote.data.NoteRepository
import top.youzix.nekonote.markdown.Markdown
import top.youzix.nekonote.markdown.MarkdownEdit
import top.youzix.nekonote.markdown.MarkdownHighlightColors
import top.youzix.nekonote.markdown.MarkdownVisualTransformation
import top.youzix.nekonote.ui.AppIcons
import top.youzix.nekonote.ui.miuix.components.BlurredBar
import top.youzix.nekonote.ui.miuix.components.StatusTag
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/**
 * The editor, aligned with the reference layout: a borderless page, a light title/meta pair, the
 * Markdown source, and a floating pill toolbar that sits above the keyboard.
 *
 * The pill has two states, like the reference: six everyday actions, then a format state reached
 * from the "T" icon with heading/emphasis/inline-code actions and a close button. Icons are drawn
 * as outlined Material glyphs rather than text symbols.
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
    var tagsText by rememberSaveable(noteId) {
        mutableStateOf(Markdown.formatTags(stored?.tags.orEmpty()))
    }
    var preview by rememberSaveable(noteId) { mutableStateOf(AppState.previewFirst) }
    var formatMode by rememberSaveable(noteId) { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

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
    val latestTitle = rememberUpdatedState(title)
    val latestBody = rememberUpdatedState(body.text)
    val latestTags = rememberUpdatedState(tagsText)
    val latestDeleted = rememberUpdatedState(deleted)

    fun store(newTitle: String, newBody: String, newTags: List<String>) {
        if (!deleted) NoteRepository.touch(noteId, newTitle, newBody, newTags)
    }

    // Typing pauses, then the file follows the memory list.
    LaunchedEffect(title, body.text, tagsText) {
        if (deleted) return@LaunchedEffect
        if (title.isBlank() && body.text.isBlank() && Markdown.parseTags(tagsText).isEmpty()) {
            return@LaunchedEffect
        }
        delay(700)
        NoteRepository.persist()
    }

    // Leaving the page (button, system gesture or engine switch) is the last save point.
    DisposableEffect(noteId) {
        onDispose {
            if (latestDeleted.value) return@onDispose
            val finalTitle = latestTitle.value
            val finalBody = latestBody.value
            val finalTags = Markdown.parseTags(latestTags.value)
            if (finalTitle.isBlank() && finalBody.isBlank() && finalTags.isEmpty()) {
                NoteRepository.delete(noteId)
            } else {
                NoteRepository.touch(noteId, finalTitle, finalBody, finalTags)
                NoteRepository.persist()
            }
        }
    }

    fun finish() {
        val finalTitle = title
        val finalBody = body.text
        val finalTags = Markdown.parseTags(tagsText)
        if (!deleted) {
            if (finalTitle.isBlank() && finalBody.isBlank() && finalTags.isEmpty()) {
                NoteRepository.delete(noteId)
            } else {
                NoteRepository.touch(noteId, finalTitle, finalBody, finalTags)
                NoteRepository.persist()
            }
        }
        onBack()
    }

    fun format(transform: (TextFieldValue) -> TextFieldValue) {
        body = transform(body)
        store(title, body.text, Markdown.parseTags(tagsText))
    }

    fun apply(action: EditorAction) {
        format { value ->
            when (action) {
                EditorAction.Task -> MarkdownEdit.task(value)
                EditorAction.Bullet -> MarkdownEdit.bullet(value)
                EditorAction.Quote -> MarkdownEdit.quote(value)
                EditorAction.CodeBlock -> MarkdownEdit.codeBlock(value)
                EditorAction.Link -> MarkdownEdit.link(value)
                EditorAction.Heading -> MarkdownEdit.heading(value)
                EditorAction.Bold -> MarkdownEdit.bold(value)
                EditorAction.Italic -> MarkdownEdit.italic(value)
                EditorAction.Strike -> MarkdownEdit.strike(value)
                EditorAction.InlineCode -> MarkdownEdit.code(value)
            }
        }
    }

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val toolbarInset = if (imeBottom > 0.dp) 0.dp else navigationBottom

    val createdAt = stored?.createdAt ?: System.currentTimeMillis()
    val tags = Markdown.parseTags(tagsText)
    val metaText = "${NoteDate.full(createdAt)} · ${Markdown.charCount(body.text)} 字"

    val titleStyle = MiuixTheme.textStyles.title2.copy(
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onSurfaceVariantSummary,
    )
    val tagStyle = MiuixTheme.textStyles.footnote1.copy(color = colorScheme.primary)
    val metaStyle = MiuixTheme.textStyles.footnote2
    val bodyStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = AppState.editorTextSize.bodySp.sp,
        lineHeight = (AppState.editorTextSize.bodySp * 1.7f).sp,
        color = colorScheme.onSurface,
    )
    val bodyPlaceholderStyle = MiuixTheme.textStyles.body1.copy(
        fontSize = AppState.editorTextSize.bodySp.sp,
        color = colorScheme.onSurfaceVariantSummary,
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = colorScheme.surface,
        topBar = {
            BlurredBar(backdrop = null) {
                SmallTopAppBar(
                    title = "",
                    color = colorScheme.surface,
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
                FloatingFormatBar(
                    formatMode = formatMode,
                    bottomInset = toolbarInset,
                    onToggleFormatMode = { formatMode = !formatMode },
                    onAction = { apply(it) },
                )
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
                .then(if (preview) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            if (preview) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = title.ifBlank { Markdown.titleOf(body.text) }
                            .ifBlank { "未命名笔记" },
                        style = titleStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (tags.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            tags.forEach { tag ->
                                StatusTag(
                                    label = tagLabel(tag),
                                    backgroundColor = colorScheme.secondaryContainer,
                                    contentColor = colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = metaText,
                        style = metaStyle,
                        color = colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(16.dp))
                    MiuixMarkdown(markdown = body.text)
                    Spacer(Modifier.height(32.dp))
                }
            } else {
                BasicTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        store(it, body.text, Markdown.parseTags(tagsText))
                    },
                    singleLine = true,
                    textStyle = titleStyle,
                    cursorBrush = SolidColor(colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = "未命名笔记",
                                    style = titleStyle,
                                    color = colorScheme.onSurfaceVariantSummary.copy(alpha = 0.7f),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Spacer(Modifier.height(2.dp))
                BasicTextField(
                    value = tagsText,
                    onValueChange = {
                        tagsText = it
                        store(title, body.text, Markdown.parseTags(it))
                    },
                    singleLine = true,
                    textStyle = tagStyle,
                    cursorBrush = SolidColor(colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (tagsText.isEmpty()) {
                                Text(
                                    text = "添加标签…",
                                    style = tagStyle,
                                    color = colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = metaText,
                    style = metaStyle,
                    color = colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = body,
                    onValueChange = {
                        body = it
                        store(title, it.text, Markdown.parseTags(tagsText))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = bodyStyle,
                    cursorBrush = SolidColor(colorScheme.primary),
                    visualTransformation = highlight,
                    decorationBox = { innerTextField ->
                        Box {
                            if (body.text.isEmpty()) {
                                Text(
                                    text = "开始书写…",
                                    style = bodyPlaceholderStyle,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
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
                        store(title, body.text, Markdown.parseTags(tagsText))
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

/** The floating pill: six everyday actions, or the format state after the "T" icon. */
@Composable
private fun FloatingFormatBar(
    formatMode: Boolean,
    bottomInset: Dp,
    onToggleFormatMode: () -> Unit,
    onAction: (EditorAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomInset + 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            color = colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!formatMode) {
                    ToolIcon(AppIcons.Task, "待办") { onAction(EditorAction.Task) }
                    ToolIcon(AppIcons.BulletList, "列表") { onAction(EditorAction.Bullet) }
                    ToolIcon(AppIcons.Quote, "引用") { onAction(EditorAction.Quote) }
                    ToolIcon(AppIcons.CodeBlock, "代码块") { onAction(EditorAction.CodeBlock) }
                    ToolIcon(AppIcons.Link, "链接") { onAction(EditorAction.Link) }
                    ToolIcon(AppIcons.TextFormat, "文本格式", onToggleFormatMode)
                } else {
                    ToolIcon(AppIcons.Heading, "标题") { onAction(EditorAction.Heading) }
                    ToolIcon(AppIcons.Bold, "粗体") { onAction(EditorAction.Bold) }
                    ToolIcon(AppIcons.Italic, "斜体") { onAction(EditorAction.Italic) }
                    ToolIcon(AppIcons.Strike, "删除线") { onAction(EditorAction.Strike) }
                    ToolIcon(AppIcons.InlineCode, "行内代码") { onAction(EditorAction.InlineCode) }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(colorScheme.dividerLine),
                    )
                    ToolIcon(AppIcons.Close, "收起格式", onToggleFormatMode)
                }
            }
        }
    }
}

@Composable
private fun ToolIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = colorScheme.onSurface,
        )
    }
}

private enum class EditorAction {
    Task,
    Bullet,
    Quote,
    CodeBlock,
    Link,
    Heading,
    Bold,
    Italic,
    Strike,
    InlineCode,
}

/** Keeps a long tag from pushing the rest of the row off the card. */
private fun tagLabel(tag: String): String =
    if (tag.length <= 12) "#$tag" else "#${tag.take(11)}…"
