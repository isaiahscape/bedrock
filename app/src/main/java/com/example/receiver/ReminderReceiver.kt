package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("note_id", -1L)
        val noteTitle = intent.getStringExtra("note_title") ?: "Note Reminder"
        
        if (noteId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", noteId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            noteId.toInt(), 
            contentIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "reminders_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a better icon if available
            .setContentTitle("Bedrock Reminder")
            .setContentText(noteTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(noteId.toInt(), notification)
    }
}
