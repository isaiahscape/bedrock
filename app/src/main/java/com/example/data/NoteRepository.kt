package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.util.MarkdownHelper
import org.json.JSONArray
import org.json.JSONObject

class NoteRepository(private val noteDao: NoteDao) {

    // Offline mode override simulation toggle
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
    }

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val allTags: Flow<List<Tag>> = noteDao.getAllTags()
    val syncLogs: Flow<List<SyncLog>> = noteDao.getSyncLogs()

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun saveNote(note: Note): Long {
        val syncStatus = if (_isOfflineMode.value) "PENDING_SYNC" else "SYNCED"
        val updatedNote = note.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = syncStatus
        )

        val id = if (note.id == 0L) {
            noteDao.insertNote(updatedNote)
        } else {
            noteDao.updateNote(updatedNote)
            note.id
        }

        val logMessage = if (note.id == 0L) "Created note: '${note.title.take(20)}'" else "Updated note: '${note.title.take(20)}'"
        noteDao.insertSyncLog(
            SyncLog(
                action = logMessage,
                status = if (_isOfflineMode.value) "OFFLINE_QUEUED" else "SUCCESS"
            )
        )
        return id
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
        noteDao.insertSyncLog(SyncLog(action = "Deleted note ID #$id", status = "SUCCESS"))
    }

    suspend fun togglePinNote(id: Long, currentIsPinned: Boolean) {
        noteDao.setPinned(id, !currentIsPinned)
    }

    suspend fun updateReminderTime(id: Long, reminderTime: Long?) {
        noteDao.updateReminderTime(id, reminderTime)
    }

    suspend fun toggleTodoInNote(note: Note, lineIndex: Int) {
        val updatedContent = MarkdownHelper.toggleTodoAtLine(note.content, lineIndex)
        val syncStatus = if (_isOfflineMode.value) "PENDING_SYNC" else "SYNCED"
        val updatedNote = note.copy(
            content = updatedContent,
            updatedAt = System.currentTimeMillis(),
            syncStatus = syncStatus
        )
        noteDao.updateNote(updatedNote)
    }

    // Add / Delete Tag
    suspend fun addTag(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            noteDao.insertTag(Tag(name = trimmed))
        }
    }

    suspend fun deleteTag(name: String) {
        noteDao.deleteTagByName(name)
    }

    // Offline Cloud Synchronization Simulation
    suspend fun triggerSyncAllNotes(): String {
        if (_isOfflineMode.value) {
            noteDao.insertSyncLog(SyncLog(action = "Manual sync attempted while Offline Mode is enabled", status = "OFFLINE_QUEUED"))
            return "Device is currently in Offline Mode. Notes saved locally."
        }

        // Simulate cloud sync process
        val pendingNotes = noteDao.getSyncLogs() // or update notes
        // Mark all notes as SYNCED
        val allCurrentNotes = noteDao.getAllNotes()
        // In Room we can update syncStatus
        noteDao.insertSyncLog(SyncLog(action = "Cloud Sync Engine: Uploaded local changes to Remote Server", status = "SUCCESS"))
        noteDao.insertSyncLog(SyncLog(action = "Cloud Sync Engine: Checked device cross-replica checksums", status = "SUCCESS"))

        return "Successfully synchronized all notes with Cloud Backup!"
    }

    suspend fun clearLogs() {
        noteDao.clearSyncLogs()
    }

    // Export Notes as JSON Backup String
    suspend fun exportNotesJson(notesList: List<Note>): String {
        val jsonArray = JSONArray()
        notesList.forEach { note ->
            val obj = JSONObject()
            obj.put("id", note.id)
            obj.put("title", note.title)
            obj.put("content", note.content)
            obj.put("createdAt", note.createdAt)
            obj.put("updatedAt", note.updatedAt)
            obj.put("isPinned", note.isPinned)
            obj.put("isEncrypted", note.isEncrypted)
            obj.put("tags", note.tags)
            obj.put("deviceOrigin", note.deviceOrigin)
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    // Import Notes from JSON Backup
    suspend fun importNotesJson(jsonString: String): Int {
        var importedCount = 0
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val note = Note(
                    title = obj.optString("title", "Imported Note"),
                    content = obj.optString("content", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                    isPinned = obj.optBoolean("isPinned", false),
                    isEncrypted = obj.optBoolean("isEncrypted", false),
                    tags = obj.optString("tags", ""),
                    deviceOrigin = "Restored Backup",
                    syncStatus = "SYNCED"
                )
                noteDao.insertNote(note)
                importedCount++
            }
            noteDao.insertSyncLog(SyncLog(action = "Imported $importedCount notes from JSON Backup", status = "SUCCESS"))
        } catch (e: Exception) {
            noteDao.insertSyncLog(SyncLog(action = "Failed to import JSON backup: ${e.message}", status = "ERROR"))
        }
        return importedCount
    }
}
