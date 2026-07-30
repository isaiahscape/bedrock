package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.data.SyncLog
import com.example.data.Tag
import com.example.util.EncryptionUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // Search and Tag filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Unlocked encrypted note IDs during active session
    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<Long>> = _unlockedNoteIds.asStateFlow()

    // Master PIN
    private val _masterPin = MutableStateFlow<String?>("1234") // Default master pin
    val masterPin: StateFlow<String?> = _masterPin.asStateFlow()

    // Sync state
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val syncLogs: Flow<List<SyncLog>> = repository.syncLogs

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val allTags: Flow<List<Tag>> = repository.allTags

    // Filtered Notes Flow
    val notes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _searchQuery,
        _selectedTag
    ) { allNotesList, query, tagFilter ->
        allNotesList.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.tags.contains(query, ignoreCase = true)

            val matchesTag = tagFilter == null || note.getTagList().contains(tagFilter)

            matchesQuery && matchesTag
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun toggleOfflineMode(enabled: Boolean) {
        repository.setOfflineMode(enabled)
    }

    fun unlockNote(noteId: Long, pinInput: String, note: Note): Boolean {
        val targetPasscode = note.passcodeHash ?: _masterPin.value ?: ""
        val isValid = EncryptionUtil.verifyPasscode(pinInput, targetPasscode)
        if (isValid) {
            _unlockedNoteIds.update { it + noteId }
        }
        return isValid
    }

    fun lockNote(noteId: Long) {
        _unlockedNoteIds.update { it - noteId }
    }

    fun setMasterPin(pin: String) {
        _masterPin.value = pin
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.togglePinNote(note.id, note.isPinned)
        }
    }

    fun toggleTodoItem(note: Note, lineIndex: Int) {
        viewModelScope.launch {
            repository.toggleTodoInNote(note, lineIndex)
        }
    }

    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        tags: String,
        isPinned: Boolean,
        isEncrypted: Boolean,
        passcode: String? = null
    ) {
        viewModelScope.launch {
            val noteToSave = Note(
                id = id,
                title = title.ifBlank { "Untitled Note" },
                content = content,
                tags = tags,
                isPinned = isPinned,
                isEncrypted = isEncrypted,
                passcodeHash = if (isEncrypted) passcode?.ifBlank { "1234" } else null
            )
            val savedId = repository.saveNote(noteToSave)
            if (isEncrypted) {
                // Auto-unlock for immediate viewing
                _unlockedNoteIds.update { it + savedId }
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            repository.addTag(name)
        }
    }

    fun deleteTag(name: String) {
        viewModelScope.launch {
            repository.deleteTag(name)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            val msg = repository.triggerSyncAllNotes()
            _syncMessage.value = msg
            _isSyncing.value = false
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun clearSyncLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    suspend fun getBackupJson(): String {
        return repository.exportNotesJson(notes.value)
    }

    fun importBackupJson(jsonStr: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importNotesJson(jsonStr)
            onComplete(count)
        }
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
