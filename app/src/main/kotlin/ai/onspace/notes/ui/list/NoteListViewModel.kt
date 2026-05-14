package ai.onspace.notes.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ai.onspace.notes.data.model.Note
import ai.onspace.notes.data.storage.StorageService
import kotlinx.coroutines.launch

class NoteListViewModel(application: Application) : AndroidViewModel(application) {

    private val storageService = StorageService(application)

    private val _notes = MutableLiveData<List<Note>>(emptyList())
    val notes: LiveData<List<Note>> = _notes

    private val _loading = MutableLiveData(true)
    val loading: LiveData<Boolean> = _loading

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _loading.value = true
            _notes.value = storageService.loadNotes()
            _loading.value = false
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            storageService.deleteNote(id)
            loadNotes()
        }
    }
}
