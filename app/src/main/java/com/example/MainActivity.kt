package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            val context = this
            val preferenceManager = remember { PreferenceManager(context) }
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { NoteRepository(database.noteDao()) }
            val viewModel: NoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = NoteViewModelFactory(repository, preferenceManager)
            )

            val themeMode by viewModel.themeMode.collectAsState()

            BedrockTheme(themeMode = themeMode) {
                BedrockApp(viewModel = viewModel)
            }
        }
    }
}

