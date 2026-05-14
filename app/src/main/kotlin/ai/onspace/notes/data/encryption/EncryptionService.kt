package ai.onspace.notes.data.encryption

import android.util.Base64
import java.security.MessageDigest

object EncryptionService {

    private const val ENCRYPTION_KEY = "notes-app-encryption-key-2025"

    private fun generateKeyHash(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest((key + ENCRYPTION_KEY).toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun encrypt(text: String): String {
        if (text.isEmpty()) return ""
        return try {
            val keyHash = generateKeyHash(ENCRYPTION_KEY)
            val keyBytes = keyHash.map { it.code }
            val textBytes = text.toByteArray(Charsets.UTF_8)
            val encrypted = ByteArray(textBytes.size) { i ->
                (textBytes[i].toInt() xor keyBytes[i % keyBytes.size]).toByte()
            }
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            text
        }
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        return try {
            val keyHash = generateKeyHash(ENCRYPTION_KEY)
            val keyBytes = keyHash.map { it.code }
            val encrypted = Base64.decode(encryptedText.trim(), Base64.DEFAULT)
            val decrypted = ByteArray(encrypted.size) { i ->
                (encrypted[i].toInt() xor keyBytes[i % keyBytes.size]).toByte()
            }
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText
        }
    }
}
