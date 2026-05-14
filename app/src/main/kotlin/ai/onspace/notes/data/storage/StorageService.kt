package ai.onspace.notes.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ai.onspace.notes.data.encryption.EncryptionService
import ai.onspace.notes.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageService(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notes_encrypted_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val NOTES_KEY = "notes_encrypted"

    suspend fun loadNotes(): List<Note> = withContext(Dispatchers.IO) {
        try {
            val encryptedData = prefs.getString(NOTES_KEY, null) ?: return@withContext emptyList()
            val decrypted = EncryptionService.decrypt(encryptedData)
            val type = object : TypeToken<List<Note>>() {}.type
            gson.fromJson(decrypted, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveNotes(notes: List<Note>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(notes)
        val encrypted = EncryptionService.encrypt(json)
        prefs.edit().putString(NOTES_KEY, encrypted).apply()
    }

    suspend fun createNote(title: String, content: String): Note = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val note = Note(
            id = now.toString(),
            title = title.ifBlank { "Без названия" },
            content = content,
            createdAt = now,
            updatedAt = now
        )
        val notes = loadNotes().toMutableList()
        notes.add(0, note)
        saveNotes(notes)
        note
    }

    suspend fun updateNote(id: String, title: String, content: String) = withContext(Dispatchers.IO) {
        val notes = loadNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == id }
        if (index != -1) {
            notes[index] = notes[index].copy(
                title = title.ifBlank { "Без названия" },
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            saveNotes(notes)
        }
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        val notes = loadNotes().filter { it.id != id }
        saveNotes(notes)
    }
}
