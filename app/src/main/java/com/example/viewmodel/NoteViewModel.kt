package com.example.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.data.SyncLog
import com.example.data.Tag
import com.example.receiver.ReminderReceiver
import com.example.util.EncryptionUtil
import com.example.util.PreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: NoteRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Search and Tag filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Unlocked encrypted note IDs during active session
    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<Long>> = _unlockedNoteIds.asStateFlow()

    // Theme and Master Password from DataStore
    val themeMode: StateFlow<String> = preferenceManager.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "auto"
    )

    val masterPassword: StateFlow<String?> = preferenceManager.masterPassword.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "1234"
    )

    val userName: StateFlow<String> = preferenceManager.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Guest User"
    )

    val userImageUri: StateFlow<String?> = preferenceManager.userImageUri.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Sync state
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val syncLogs: Flow<List<SyncLog>> = repository.syncLogs

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val allTags: Flow<List<Tag>> = repository.allTags

    // Workspace / Tab Management
    private val _openTabs = MutableStateFlow<List<Long>>(emptyList())
    val openTabs: StateFlow<List<Long>> = _openTabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<Long?>(null)
    val activeTabId: StateFlow<Long?> = _activeTabId.asStateFlow()

    fun openNoteInTab(id: Long) {
        if (!_openTabs.value.contains(id)) {
            _openTabs.update { it + id }
        }
        _activeTabId.value = id
    }

    fun closeTab(id: Long) {
        _openTabs.update { it.filter { tabId -> tabId != id } }
        if (_activeTabId.value == id) {
            _activeTabId.value = _openTabs.value.lastOrNull()
        }
    }

    fun switchTab(id: Long) {
        if (_openTabs.value.contains(id)) {
            _activeTabId.value = id
        }
    }

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
        val targetPasscode = note.passcodeHash ?: masterPassword.value ?: ""
        val isValid = EncryptionUtil.verifyPasscode(pinInput, targetPasscode)
        if (isValid) {
            _unlockedNoteIds.update { it + noteId }
        }
        return isValid
    }

    fun lockNote(noteId: Long) {
        _unlockedNoteIds.update { it - noteId }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferenceManager.setThemeMode(mode)
        }
    }

    fun setMasterPassword(password: String) {
        viewModelScope.launch {
            preferenceManager.setMasterPassword(password)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            preferenceManager.setUserName(name)
        }
    }

    fun setUserImageUri(uri: String?) {
        viewModelScope.launch {
            preferenceManager.setUserImageUri(uri)
        }
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
        passcode: String? = null,
        type: String = "note",
        reminderTime: Long? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val noteToSave = Note(
                id = id,
                title = title.ifBlank { "Untitled Note" },
                content = content,
                tags = tags,
                isPinned = isPinned,
                isEncrypted = isEncrypted,
                passcodeHash = if (isEncrypted) passcode?.ifBlank { masterPassword.value ?: "1234" } else null,
                type = type,
                reminderTime = reminderTime
            )
            val savedId = repository.saveNote(noteToSave)
            
            if (context != null) {
                if (reminderTime != null) {
                    scheduleAlarm(context, savedId, title.ifBlank { "Untitled Note" }, reminderTime)
                } else if (id != 0L) {
                    cancelAlarm(context, id)
                }
            }

            if (isEncrypted) {
                // Auto-unlock for immediate viewing
                _unlockedNoteIds.update { it + savedId }
            }
        }
    }

    private fun scheduleAlarm(context: Context, noteId: Long, noteTitle: String, timeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("note_id", noteId)
            putExtra("note_title", noteTitle)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, noteId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
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

    // Keyboard Shortcuts
    private val _keyboardEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val keyboardEvent = _keyboardEvent.asSharedFlow()

    fun triggerKeyboardShortcut(action: String) {
        _keyboardEvent.tryEmit(action)
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

    suspend fun getEncryptedBackup(password: String): String {
        val json = repository.exportNotesJson(notes.value)
        return EncryptionUtil.encryptContent(json, password)
    }

    fun importEncryptedBackup(encryptedData: String, password: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val decryptedJson = EncryptionUtil.decryptContent(encryptedData, password)
            if (decryptedJson == "[Encrypted Content - Invalid Key]") {
                onComplete(-1)
            } else {
                val count = repository.importNotesJson(decryptedJson)
                onComplete(count)
            }
        }
    }
}

class NoteViewModelFactory(
    private val repository: NoteRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
