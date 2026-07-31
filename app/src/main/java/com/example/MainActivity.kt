package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.ui.BedrockApp
import com.example.ui.theme.BedrockTheme
import com.example.util.NotificationHelper
import com.example.util.PreferenceManager
import com.example.viewmodel.NoteViewModel
import com.example.viewmodel.NoteViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            val context = this
            val preferenceManager = remember { PreferenceManager(context) }
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { NoteRepository(database.noteDao()) }
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = NoteViewModelFactory(repository, preferenceManager)
            )
            viewModel.cleanupTrash()

            val themeMode by viewModel.themeMode.collectAsState()

            BedrockTheme(themeMode = themeMode) {
                BedrockApp(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed) {
            val shortcut = when (keyCode) {
                KeyEvent.KEYCODE_N -> "new_note"
                KeyEvent.KEYCODE_S -> "save_note"
                KeyEvent.KEYCODE_P -> "command_palette"
                KeyEvent.KEYCODE_F -> "focus_search"
                KeyEvent.KEYCODE_W -> "close_tab"
                KeyEvent.KEYCODE_B -> "bold"
                KeyEvent.KEYCODE_I -> "italic"
                KeyEvent.KEYCODE_U -> "underline"
                KeyEvent.KEYCODE_1 -> "header_1"
                KeyEvent.KEYCODE_2 -> "header_2"
                else -> null
            }
            if (shortcut != null) {
                viewModel.triggerKeyboardShortcut(shortcut)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

