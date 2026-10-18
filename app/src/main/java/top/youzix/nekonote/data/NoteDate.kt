/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Human dates: "刚刚", "今天 14:20", "昨天 09:05", "3月8日", "2025年12月1日". */
object NoteDate {
    private val timeOfDay = DateTimeFormatter.ofPattern("HH:mm")
    private val monthDay = DateTimeFormatter.ofPattern("M月d日")
    private val yearMonthDay = DateTimeFormatter.ofPattern("yyyy年M月d日")
    private val full = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "刚刚"
        val zone = ZoneId.systemDefault()
        val moment = Instant.ofEpochMilli(millis).atZone(zone)
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val date = moment.toLocalDate()
        return when {
            now - millis < 60_000L -> "刚刚"
            date == today -> "今天 ${moment.format(timeOfDay)}"
            date == today.minusDays(1) -> "昨天 ${moment.format(timeOfDay)}"
            date.year == today.year -> moment.format(monthDay)
            else -> moment.format(yearMonthDay)
        }
    }

    fun full(millis: Long): String = if (millis <= 0L) "未知" else {
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(full)
    }
}
