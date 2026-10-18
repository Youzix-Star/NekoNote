/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 *
 * Route keys for the miuix-nav back stack, the same shape SukiSU-Ultra uses (GPL-3.0).
 */

package top.youzix.nekonote.ui.nav

import top.yukonga.miuix.kmp.nav.core.NavKey

/**
 * Every second-level page the shell can show.
 *
 * The stack is in-memory (`navBackStackOf`), so keys do not have to be `@Serializable`; they are
 * `data object`s anyway, which keeps entry identity and saved state stable.
 */
sealed interface Route : NavKey {
    data object Main : Route
    data object About : Route
    data object Licenses : Route
}
