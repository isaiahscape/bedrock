package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, Tag::class, SyncLog::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bedrock_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.noteDao())
                    }
                }
            }

            suspend fun populateDatabase(noteDao: NoteDao) {
                // Insert default tags
                val tags = listOf("Documentation", "Todo", "Ideas", "Project", "Personal")
                tags.forEach { noteDao.insertTag(Tag(name = it)) }

                val now = System.currentTimeMillis()

                // Note 1: Welcome & Markdown Formatting Guide
                val welcomeNote = Note(
                    title = "Welcome to Bedrock 📓",
                    content = """
# Getting Started with Bedrock

Bedrock is a high-contrast **monochrome notes editor** built for focused markdown writing, structured task management, and offline-first data sync.

---

### Features Overview
- **Markdown Text Editing**: Support for headers, bold, italics, code blocks, quote blocks, and horizontal rules.
- **Embedded Checklists**: Add interactive to-do items directly inside any note!
- **Tag Organization**: Categorize notes easily with custom tags.
- **Offline Sync Engine**: Automatic caching with cloud sync status tracking.
- **Encrypted Local Storage**: Lock sensitive notes behind a passcode or PIN.

---

### Markdown Formatting Examples
*Italic text*, **Bold text**, and `inline code`.

> "Simplicity is the ultimate sophistication." — Leonardo da Vinci

```kotlin
fun syncNotesOffline(): Boolean {
    val database = Room.databaseBuilder(...)
    return true
}
```

### Today's Quick Tasks
- [x] Download Bedrock App
- [x] Read the Markdown guide
- [ ] Create my first tagged note
- [ ] Test the offline synchronization status
- [ ] Enable local encryption on a private note
                    """.trimIndent(),
                    createdAt = now - 3600000,
                    updatedAt = now - 3600000,
                    isPinned = true,
                    tags = "Documentation,Todo",
                    syncStatus = "SYNCED",
                    deviceOrigin = "Primary Device",
                    type = "markdown"
                )

                // Note 2: Project Roadmap & Tasks
                val projectNote = Note(
                    title = "Bedrock Architecture & Roadmap 🚀",
                    content = """
# Development Sprint Backlog

Track project milestones and feature implementations directly with embedded to-do items:

### Phase 1: Core Engine
- [x] Material Design 3 Monochrome Theme setup
- [x] Room Database schema for Notes, Tags, and Sync Logs
- [x] High-contrast typography & custom app launcher icon

### Phase 2: Markdown & Tasks
- [x] Interactive markdown preview and editing toolbar
- [x] Embedded checkbox toggling directly from card previews
- [x] Dynamic tag filtering & instant text search

### Phase 3: Security & Offline Sync
- [x] Encrypted note locking with passcode protection
- [x] Offline state indicator & simulated cloud sync engine
- [x] JSON backup export and import for multi-device simulation
                    """.trimIndent(),
                    createdAt = now - 1800000,
                    updatedAt = now - 1800000,
                    isPinned = true,
                    tags = "Project,Todo",
                    syncStatus = "SYNCED",
                    deviceOrigin = "MacBook Air",
                    type = "markdown"
                )

                // Note 3: Encrypted Sample Note
                val encryptedNote = Note(
                    title = "Secret Vault Note 🔐",
                    content = """
# Confidential Project Notes

This note is stored with **Encrypted Local Storage**.

### Secure Credentials & Recovery Keys
- Recovery Seed: `mono-vault-8891-secure-key`
- Access Level: Confidential / Strictly Encrypted
- Algorithm: SHA-256 Key Derivation + Local Cipher Storage

### Security Checklist
- [x] Master Password setup verified
- [x] Encrypted note toggle active
- [ ] Password change cycle scheduled
                    """.trimIndent(),
                    createdAt = now - 900000,
                    updatedAt = now - 900000,
                    isPinned = false,
                    isEncrypted = true,
                    passcodeHash = "1234", // Simple default password for demonstration
                    tags = "Personal",
                    syncStatus = "LOCAL_ONLY",
                    deviceOrigin = "Encrypted Vault",
                    type = "note"
                )

                noteDao.insertNote(welcomeNote)
                noteDao.insertNote(projectNote)
                noteDao.insertNote(encryptedNote)

                noteDao.insertSyncLog(SyncLog(action = "Initial database setup and seed notes loaded", status = "SUCCESS"))
                noteDao.insertSyncLog(SyncLog(action = "Simulated sync with cloud server", status = "SUCCESS"))
            }
        }
    }
}
