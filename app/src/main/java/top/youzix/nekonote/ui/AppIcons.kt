/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The app's mark, as text: a face assembled out of letters that already look like one, for the
 * places that want the mark without shipping artwork (the start state of the web tab).
 *
 * The launcher icon is separate art under `res/mipmap-*` and does not contain this string, so the
 * two no longer have to move together.
 *
 * Codepoints: U+29AE, U+005F, U+0020, U+0020, U+0020, U+032B, U+0020, U+005F, U+29AF.
 */
const val AppIconText = "⦮_   ̫ _⦯"

/** Material icons used by the app. */
object AppIcons {
    // The four tabs.
    val Home: ImageVector = Icons.Rounded.Home
    val List: ImageVector = Icons.AutoMirrored.Rounded.List
    val Gallery: ImageVector = Icons.Rounded.Widgets
    val Settings: ImageVector = Icons.Rounded.Settings

    // 设置页的行首图标
    val Palette: ImageVector = Icons.Rounded.Palette
    val Speed: ImageVector = Icons.Rounded.Speed
    val BlurOn: ImageVector = Icons.Rounded.BlurOn
    val ChevronRight: ImageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight
    val About: ImageVector = Icons.Rounded.Info

    val SourceCode: ImageVector = Icons.Rounded.Code
    val License: ImageVector = Icons.Rounded.Description
    val Developer: ImageVector = Icons.Rounded.Person
    val Phones: ImageVector = Icons.Rounded.PhoneAndroid
    val Log: ImageVector = Icons.AutoMirrored.Rounded.Article
    val Debug: ImageVector = Icons.Rounded.BugReport

    // 状态与动作：DSHA 首页用的就是这几个
    val Grant: ImageVector = Icons.Rounded.CheckCircle
    val Tune: ImageVector = Icons.Rounded.Tune
    val Update: ImageVector = Icons.Rounded.SystemUpdate
    val Play: ImageVector = Icons.Rounded.PlayArrow
    val Pause: ImageVector = Icons.Rounded.Stop

    // NekoNote 编辑器悬浮工具栏：描边图标，接近参考应用的细线图标。
    val Check: ImageVector = Icons.Rounded.Check
    val Task: ImageVector = Icons.Outlined.CheckBoxOutlineBlank
    val BulletList: ImageVector = Icons.Outlined.FormatListBulleted
    val Quote: ImageVector = Icons.Outlined.FormatQuote
    val CodeBlock: ImageVector = Icons.Outlined.DataObject
    val Link: ImageVector = Icons.Outlined.Link
    val TextFormat: ImageVector = Icons.Outlined.TextFields
    val Heading: ImageVector = Icons.Outlined.Title
    val Bold: ImageVector = Icons.Outlined.FormatBold
    val Italic: ImageVector = Icons.Outlined.FormatItalic
    val Strike: ImageVector = Icons.Outlined.StrikethroughS
    val InlineCode: ImageVector = Icons.Outlined.Code
    val Close: ImageVector = Icons.Outlined.Close

    // NekoNote: note list, editor and tag pages.
    val Add: ImageVector = Icons.Rounded.Add
    val Edit: ImageVector = Icons.Rounded.Edit
    val Preview: ImageVector = Icons.Rounded.Visibility
    val Pin: ImageVector = Icons.Rounded.PushPin
    val Delete: ImageVector = Icons.Rounded.Delete
    val More: ImageVector = Icons.Rounded.MoreVert
    val Sort: ImageVector = Icons.Rounded.Sort
    val Tag: ImageVector = Icons.Rounded.Tag
    val Notes: ImageVector = Icons.Rounded.Notes

    val Refresh: ImageVector = Icons.Rounded.Refresh
    val Back: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack
    val Forward: ImageVector = Icons.AutoMirrored.Rounded.ArrowForward
}
