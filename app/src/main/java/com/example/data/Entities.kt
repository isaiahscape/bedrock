package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isEncrypted: Boolean = false,
    val passcodeHash: String? = null,
    val tags: String = "", // Comma-separated list of tags e.g. "Work,Docs,Todo"
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_SYNC, LOCAL_ONLY
    val deviceOrigin: String = "This Device",
    val isArchived: Boolean = false,
    val type: String = "note", // note, markdown, todo
    val reminderTime: Long? = null // epoch timestamp
) {
    fun getTagList(): List<String> {
        if (tags.isBlank()) return emptyList()
        return tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    // Helper to extract todo completion stats from content
    fun getTodoStats(): Pair<Int, Int> {
        val lines = content.lines()
        var total = 0
        var completed = 0
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]")) {
                total++
            } else if (trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]") ||
                       trimmed.startsWith("* [x]") || trimmed.startsWith("* [X]")) {
                total++
                completed++
            }
        }
        return Pair(completed, total)
    }
}

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "sync_logs")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val status: String // SUCCESS, OFFLINE_QUEUED, ERROR
)

@Entity(tableName = "crash_logs")
data class CrashLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val exceptionName: String,
    val message: String,
    val stackTrace: String
)
