/*
 * Copyright 2026, Youzix-Star
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package top.youzix.nekonote.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject
import top.youzix.nekonote.markdown.Markdown

/** A tag and how many notes carry it. */
data class TagCount(val tag: String, val count: Int)

/**
 * Every note, in memory, backed by one JSON file under `filesDir/notes/`.
 *
 * The in-memory list is the single source of truth for the UI; writing is serialised on one
 * background thread and always replaces the file atomically, so a half-written note can never be
 * the file that loads next launch. Editing a note updates the list immediately (the list behind
 * the editor follows along) and the editor asks for a [persist] once the typing pauses.
 */
object NoteRepository {
    private const val DIR_NAME = "notes"
    private const val FILE_NAME = "notes.json"

    private val io = Executors.newSingleThreadExecutor()
    private var appContext: Context? = null

    val notes = mutableStateListOf<Note>()

    /** False until [init] has read the file, so screens can show nothing instead of flickering. */
    var loaded by mutableStateOf(false)
        private set

    /** Reads the store once; later calls are no-ops. */
    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val file = file()
        val existed = file.exists()
        val restored = if (existed) runCatching { decode(file.readText()) }.getOrDefault(emptyList()) else emptyList()
        notes.clear()
        if (restored.isEmpty() && !existed) {
            notes.addAll(seedNotes())
            loaded = true
            persist()
        } else {
            notes.addAll(restored)
            loaded = true
        }
    }

    /** A fresh id for a draft; the note itself is created on the first keystroke. */
    fun newDraftId(): String = UUID.randomUUID().toString()

    fun find(id: String): Note? = notes.firstOrNull { it.id == id }

    /**
     * Writes [title], [body] and [tags] into the note with [id], creating it when it is still a
     * draft. Returns the stored note, or null when the store has not been initialised.
     */
    fun touch(id: String, title: String, body: String, tags: List<String>): Note? {
        if (appContext == null) return null
        val index = notes.indexOfFirst { it.id == id }
        val now = System.currentTimeMillis()
        if (index >= 0) {
            val old = notes[index]
            if (old.title == title && old.body == body && old.tags == tags) return old
            val updated = old.copy(title = title, body = body, tags = tags, updatedAt = now)
            notes[index] = updated
            return updated
        }
        val created = Note(
            id = id,
            title = title,
            body = body,
            tags = tags,
            pinned = false,
            createdAt = now,
            updatedAt = now,
        )
        notes.add(0, created)
        return created
    }

    /** A brand new note, already stored. */
    fun create(title: String = "", body: String = "", tags: List<String> = emptyList()): Note {
        val now = System.currentTimeMillis()
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            tags = tags,
            pinned = false,
            createdAt = now,
            updatedAt = now,
        )
        notes.add(0, note)
        persist()
        return note
    }

    fun togglePin(id: String) {
        val index = notes.indexOfFirst { it.id == id }
        if (index < 0) return
        notes[index] = notes[index].copy(pinned = !notes[index].pinned)
        persist()
    }

    fun delete(id: String) {
        val removed = notes.removeAll { it.id == id }
        if (removed) persist()
    }

    /** Writes the current list to disk, off the main thread. */
    fun persist() {
        val context = appContext ?: return
        val snapshot = notes.toList()
        io.execute {
            runCatching {
                val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
                val target = File(dir, FILE_NAME)
                val temp = File(dir, "$FILE_NAME.tmp")
                temp.writeText(encode(snapshot))
                if (target.exists() && !target.delete()) return@runCatching
                temp.renameTo(target)
            }
        }
    }

    /** Notes matching a search, a tag and the chosen order; pinned notes always come first. */
    fun visibleNotes(query: String, tag: String?, sort: NoteSort): List<Note> {
        val needle = query.trim()
        val filtered = notes.filter { note ->
            val matchesTag = tag == null || note.tags.any { it.equals(tag, ignoreCase = true) }
            val matchesQuery = needle.isEmpty() ||
                note.displayTitle.contains(needle, ignoreCase = true) ||
                note.body.contains(needle, ignoreCase = true) ||
                note.tags.any { it.contains(needle, ignoreCase = true) }
            matchesTag && matchesQuery
        }
        val order = when (sort) {
            NoteSort.Updated -> compareByDescending<Note> { it.updatedAt }
            NoteSort.Created -> compareByDescending<Note> { it.createdAt }
            NoteSort.Title -> compareBy<Note> { it.displayTitle.lowercase() }
        }
        // Boolean is not Comparable in Kotlin, so the pin flag is ordered as an int.
        return filtered.sortedWith(
            compareByDescending<Note> { if (it.pinned) 1 else 0 }.then(order),
        )
    }

    fun pinned(): List<Note> = notes.filter { it.pinned }.sortedByDescending { it.updatedAt }

    fun recent(limit: Int = 5): List<Note> =
        notes.sortedByDescending { it.updatedAt }.take(limit)

    fun tags(): List<TagCount> = notes
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .map { TagCount(it.key, it.value) }
        .sortedWith(compareByDescending<TagCount> { it.count }.thenBy { it.tag })

    fun noteCount(): Int = notes.size

    /** The file the notes live in, for the settings page. */
    fun storagePath(): String = runCatching { file().absolutePath }.getOrDefault("")

    private fun file(): File {
        val context = appContext ?: error("NoteRepository.init was not called")
        return File(File(context.filesDir, DIR_NAME), FILE_NAME)
    }

    // ---- JSON ----

    private fun encode(notes: List<Note>): String {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("body", note.body)
                    val tagArray = JSONArray()
                    note.tags.forEach { tag -> tagArray.put(tag) }
                    put("tags", tagArray)
                    put("pinned", note.pinned)
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                },
            )
        }
        return JSONObject().apply {
            put("version", 1)
            put("notes", array)
        }.toString(2)
    }

    private fun decode(text: String): List<Note> {
        if (text.isBlank()) return emptyList()
        val root = JSONObject(text)
        val array = root.optJSONArray("notes") ?: return emptyList()
        val now = System.currentTimeMillis()
        val decoded = ArrayList<Note>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
            val created = item.optLong("createdAt", now)
            val body = item.optString("body")
            val tagArray = item.optJSONArray("tags")
            // Old files had no tag list: derive it from the body's #标签 once, then store it.
            val tags = if (tagArray != null) {
                buildList {
                    for (tagIndex in 0 until tagArray.length()) {
                        tagArray.optString(tagIndex).trim()
                            .takeIf { it.isNotEmpty() }
                            ?.let { add(it) }
                    }
                }
            } else {
                Markdown.tagsOf(body)
            }
            decoded += Note(
                id = id,
                title = item.optString("title"),
                body = body,
                tags = tags,
                pinned = item.optBoolean("pinned", false),
                createdAt = created,
                updatedAt = item.optLong("updatedAt", created),
            )
        }
        return decoded
    }

    // ---- first launch ----

    /** Four notes that double as the app's documentation. */
    private fun seedNotes(): List<Note> {
        val now = System.currentTimeMillis()
        val hour = 60L * 60L * 1000L
        val day = 24L * hour
        return listOf(
            Note(
                id = UUID.randomUUID().toString(),
                title = "欢迎来到 NekoNote",
                body = WELCOME,
                tags = listOf("入门", "NekoNote"),
                pinned = true,
                createdAt = now - 2 * day,
                updatedAt = now - 20 * 60 * 1000L,
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "Markdown 速查",
                body = CHEATSHEET,
                tags = listOf("Markdown"),
                pinned = false,
                createdAt = now - 3 * day,
                updatedAt = now - 3 * hour,
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "今天吃什么",
                body = TODOS,
                tags = listOf("生活"),
                pinned = false,
                createdAt = now - day,
                updatedAt = now - 5 * hour,
            ),
            Note(
                id = UUID.randomUUID().toString(),
                title = "慢慢来",
                body = SLOW,
                tags = listOf("随想", "句子"),
                pinned = false,
                createdAt = now - 5 * day,
                updatedAt = now - 2 * day,
            ),
        )
    }

    private val WELCOME = """
        # 欢迎来到 NekoNote

        一款只属于这台设备的 Markdown 笔记。没有账号，没有同步，没有人在看。

        ## 四件小事

        - **写**：左边是源码，右边是排版，随时切换
        - **看**：标题、列表、引用、代码块都会显示成该有的样子
        - **找**：搜索标题和正文，按标签筛选
        - **留**：写完就自动保存，退出不会丢

        > 笔记是写给未来的自己的一封信。

        正文里写 #标签 就会自动归类，比如 #入门 #NekoNote。

        - [x] 打开 NekoNote
        - [ ] 写下第一行字
        - [ ] 把这份欢迎页删掉
    """.trimIndent()

    private val CHEATSHEET = """
        # Markdown 速查

        ## 文字

        **粗体**、*斜体*、~~删除线~~、`行内代码`。

        链接写成 [NekoNote 仓库](https://github.com/Youzix-Star/NekoNote)。

        ## 列表

        - 一颗猫
        - 一杯茶
          - 缩进不是必须的，但看起来舒服
        - 一整晚

        1. 打开
        2. 写
        3. 关掉

        ## 待办

        - [x] 支持复选框
        - [ ] 支持拖拽排序（以后再说）

        ## 引用与代码

        > 不要为了记而记。

        ```kotlin
        val note = Note(title = "标题", body = "# 正文")
        ```

        ---

        写在正文里的 #标签 会被识别成标签，不需要另外维护一份。
    """.trimIndent()

    private val TODOS = """
        # 今天吃什么

        - [ ] 火锅
        - [x] 面条
        - [ ] 寿司

        最后大概率还是泡面。 #生活
    """.trimIndent()

    private val SLOW = """
        # 慢慢来

        写下来的事情会比记在脑子里的多活一会儿。

        > 比较是偷走快乐的小偷。

        #随想 #句子
    """.trimIndent()
}
