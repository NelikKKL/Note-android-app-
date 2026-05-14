package ai.onspace.notes.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ai.onspace.notes.data.model.Note
import ai.onspace.notes.data.storage.StorageService
import kotlinx.coroutines.launch

class EditNoteViewModel(application: Application) : AndroidViewModel(application) {

    private val storageService = StorageService(application)

    val isSaving = MutableLiveData(false)
    val saveResult = MutableLiveData<Result<Unit>?>()
    val note = MutableLiveData<Note?>()

    fun loadNote(id: String) {
        viewModelScope.launch {
            val notes = storageService.loadNotes()
            note.value = notes.find { it.id == id }
        }
    }

    fun saveNote(id: String?, title: String, content: String) {
        viewModelScope.launch {
            isSaving.value = true
            try {
                if (id != null) {
                    storageService.updateNote(id, title, content)
                } else {
                    storageService.createNote(title, content)
                }
                saveResult.value = Result.success(Unit)
            } catch (e: Exception) {
                saveResult.value = Result.failure(e)
            } finally {
                isSaving.value = false
            }
        }
    }

    fun deleteNote(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            storageService.deleteNote(id)
            onDone()
        }
    }
}
