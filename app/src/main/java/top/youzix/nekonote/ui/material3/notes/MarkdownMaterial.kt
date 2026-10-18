/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui.material3.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.youzix.nekonote.AppState
import top.youzix.nekonote.markdown.Markdown
import top.youzix.nekonote.markdown.MdBlock

/**
 * Draws a note's Markdown with the Material engine's type.
 *
 * Same shared parse as the miuix preview, different presentation: this one follows Material's
 * typography scale and container colours, which is what makes the engine switch visible.
 */
@Composable
fun MaterialMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
    bodySize: Float = AppState.editorTextSize.bodySp,
) {
    val blocks = remember(markdown) { Markdown.parse(markdown) }
    val body = MaterialTheme.typography.bodyLarge.copy(
        fontSize = bodySize.sp,
        lineHeight = (bodySize * 1.7f).sp,
    )
    val bodyColor = colorScheme.onSurface
    val muted = colorScheme.onSurfaceVariant
    val codeBackground = colorScheme.surfaceContainerHigh
    val linkColor = colorScheme.primary

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (blocks.isEmpty()) {
            Text(text = "还没有内容，切回编辑写点什么。", style = body, color = muted)
        }
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(
                    text = remember(block.text, codeBackground, linkColor) {
                        Markdown.inline(block.text, codeBackground, linkColor)
                    },
                    fontSize = (bodySize + when (block.level) {
                        1 -> 10f
                        2 -> 6f
                        3 -> 3f
                        else -> 1f
                    }).sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = (bodySize * 1.35f).sp,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(top = if (block.level <= 2) 8.dp else 2.dp),
                )

                is MdBlock.Paragraph -> Text(
                    text = remember(block.text, codeBackground, linkColor) {
                        Markdown.inline(block.text, codeBackground, linkColor)
                    },
                    style = body,
                    color = bodyColor,
                )

                is MdBlock.Bullets -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (block.ordered) "${index + 1}." else "•",
                                style = body,
                                color = muted,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .width(if (block.ordered) 28.dp else 18.dp)
                                    .padding(end = if (block.ordered) 6.dp else 0.dp),
                            )
                            Text(
                                text = remember(item, codeBackground, linkColor) {
                                    Markdown.inline(item, codeBackground, linkColor)
                                },
                                style = body,
                                color = bodyColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp),
                            )
                        }
                    }
                }

                is MdBlock.Tasks -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (item.checked) "☑" else "☐",
                                style = body,
                                color = if (item.checked) colorScheme.primary else muted,
                                modifier = Modifier
                                    .width(24.dp)
                                    .padding(top = 1.dp),
                            )
                            Text(
                                text = remember(item.text, codeBackground, linkColor) {
                                    Markdown.inline(item.text, codeBackground, linkColor)
                                },
                                style = body.copy(
                                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                                ),
                                color = if (item.checked) muted else bodyColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp),
                            )
                        }
                    }
                }

                is MdBlock.Quote -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = remember(block.text, codeBackground, linkColor) {
                            Markdown.inline(block.text, codeBackground, linkColor)
                        },
                        style = body.copy(fontStyle = FontStyle.Italic),
                        color = colorScheme.onSecondaryContainer,
                    )
                }

                is MdBlock.Code -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(codeBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (block.language.isNotBlank()) {
                        Text(text = block.language, fontSize = 11.sp, color = muted)
                    }
                    Text(
                        text = block.code,
                        fontFamily = FontFamily.Monospace,
                        fontSize = (bodySize - 2f).coerceAtLeast(11f).sp,
                        lineHeight = (bodySize * 1.5f).sp,
                        color = bodyColor,
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    )
                }

                MdBlock.Rule -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(codeBackground),
                )
            }
        }
    }
}
