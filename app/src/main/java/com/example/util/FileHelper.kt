package com.example.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object FileHelper {

    /**
     * Saves a string content to a file in Download/Bedrock directory.
     * Uses MediaStore for API 29+ and standard File API for older versions.
     */
    fun saveStringToDownloadFolder(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String = "text/plain"
    ): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Bedrock")
                }
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    val outputStream: OutputStream? = resolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        stream.write(content.toByteArray())
                    }
                    "Download/Bedrock/$fileName"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val bedrockDir = File(downloadsDir, "Bedrock")
                if (!bedrockDir.exists()) bedrockDir.mkdirs()
                
                val file = File(bedrockDir, fileName)
                FileOutputStream(file).use { stream ->
                    stream.write(content.toByteArray())
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
