package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.CrashLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class BedrockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Setup Crash Catcher
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()
            
            // Save to private file for next startup
            try {
                val file = File(filesDir, "last_crash.txt")
                file.writeText("${throwable.javaClass.simpleName}|${throwable.message}|$stackTrace")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        // Check for crash on startup
        checkAndPersistCrash()
    }

    private fun checkAndPersistCrash() {
        val file = File(filesDir, "last_crash.txt")
        if (file.exists()) {
            val content = file.readText()
            val parts = content.split("|", limit = 3)
            if (parts.size == 3) {
                val db = AppDatabase.getDatabase(this)
                CoroutineScope(Dispatchers.IO).launch {
                    db.noteDao().insertCrashLog(
                        CrashLog(
                            exceptionName = parts[0],
                            message = parts[1],
                            stackTrace = parts[2]
                        )
                    )
                    file.delete()
                }
            } else {
                file.delete()
            }
        }
    }
}
