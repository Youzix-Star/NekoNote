/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote

import android.app.Application
import android.os.Build
import top.youzix.nekonote.crash.CrashHandler
import top.youzix.nekonote.data.NoteRepository

/**
 * Installs the crash recorder and opens the note store before anything else can fail.
 *
 * Both belong here rather than in the activity: the notes are the app, and a process that starts
 * without them would have to be able to run a list with no data.
 */
class NekoNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // The crash screen has its own process (`:crash`); installing the handler there would let a
        // failure on that screen open another crash screen, and so on.
        if (isCrashProcess()) return
        CrashHandler.install(this)
        NoteRepository.init(this)
        CrashHandler.note("应用启动")
    }

    private fun isCrashProcess(): Boolean {
        val name = currentProcessName() ?: return false
        return name.endsWith(CRASH_PROCESS_SUFFIX)
    }

    private fun currentProcessName(): String? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            // Before API 28 the process name had to be read out of /proc.
            java.io.File("/proc/self/cmdline").readText().trim().trimEnd('\u0000')
        }
    }.getOrNull()

    private companion object {
        const val CRASH_PROCESS_SUFFIX = ":crash"
    }
}
