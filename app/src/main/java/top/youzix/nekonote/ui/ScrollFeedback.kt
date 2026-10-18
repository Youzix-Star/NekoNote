/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * A small haptic when a vertical scroll is dragged past its bottom edge.
 *
 * Unlike a ScrollState-based effect this also fires when the content is shorter than the viewport:
 * the scrollable cannot consume the drag, so the delta arrives here even though there is no scroll
 * range to reach the end of. The 500ms gate keeps a continuous drag from buzzing on every frame.
 */
@Composable
fun rememberBottomEdgeHaptic(): NestedScrollConnection {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        object : NestedScrollConnection {
            private var lastFired = 0L

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f) {
                    val now = System.currentTimeMillis()
                    if (now - lastFired > 500L) {
                        lastFired = now
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    }
                }
                return Offset.Zero
            }
        }
    }
}
