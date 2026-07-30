package com.example.util

import java.security.MessageDigest
import android.util.Base64

object EncryptionUtil {

    fun hashPasscode(passcode: String): String {
        val bytes = passcode.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest(bytes)
        return hashed.joinToString("") { "%02x".format(it) }
    }

    fun verifyPasscode(passcode: String, hash: String): Boolean {
        if (hash.isBlank()) return false
        val computed = hashPasscode(passcode)
        // Also support plain simple 4-digit comparison for user convenience if hash matches plain pin
        return computed == hash || passcode == hash
    }

    // Obfuscate / encrypt string content using XOR key derived from passcode
    fun encryptContent(content: String, secretKey: String): String {
        if (secretKey.isEmpty()) return content
        val keyBytes = hashPasscode(secretKey).toByteArray(Charsets.UTF_8)
        val contentBytes = content.toByteArray(Charsets.UTF_8)
        val encrypted = ByteArray(contentBytes.size)
        for (i in contentBytes.indices) {
            encrypted[i] = (contentBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptContent(encryptedBase64: String, secretKey: String): String {
        if (secretKey.isEmpty()) return encryptedBase64
        return try {
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            val keyBytes = hashPasscode(secretKey).toByteArray(Charsets.UTF_8)
            val decrypted = ByteArray(encryptedBytes.size)
            for (i in encryptedBytes.indices) {
                decrypted[i] = (encryptedBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            "[Encrypted Content - Invalid Key]"
        }
    }
}
