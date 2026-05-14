package ai.onspace.notes.ui.edit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ai.onspace.notes.R
import ai.onspace.notes.databinding.ActivityEditNoteBinding

class EditNoteActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }

    private lateinit var binding: ActivityEditNoteBinding
    private val viewModel: EditNoteViewModel by viewModels()
    private var noteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        noteId?.let { viewModel.loadNote(it) }

        observeViewModel()
        setupSaveButton()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { performSave() }
    }

    private fun performSave() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            showDialog("Пустая заметка", "Введите заголовок или текст заметки", null)
            return
        }
        viewModel.saveNote(noteId, title, content)
    }

    private fun observeViewModel() {
        viewModel.note.observe(this) { note ->
            note?.let {
                binding.etTitle.setText(it.title)
                binding.etContent.setText(it.content)
            }
            if (noteId == null) {
                binding.etContent.requestFocus()
            }
        }

        viewModel.isSaving.observe(this) { saving ->
            binding.btnSave.isEnabled = !saving
            binding.progressSave.visibility = if (saving) View.VISIBLE else View.GONE
        }

        viewModel.saveResult.observe(this) { result ->
            result ?: return@observe
            if (result.isSuccess) {
                setResult(RESULT_OK)
                finish()
            } else {
                showDialog("Ошибка", "Не удалось сохранить заметку", null)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (noteId != null) menuInflater.inflate(R.menu.menu_edit_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBack()
                true
            }
            R.id.action_delete -> {
                confirmDelete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleBack() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val original = viewModel.note.value
        val hasChanges = (original == null && (title.isNotEmpty() || content.isNotEmpty())) ||
                (original != null && (title != original.title || content != original.content))

        if (hasChanges) {
            AlertDialog.Builder(this)
                .setTitle("Несохранённые изменения")
                .setMessage("Сохранить заметку перед выходом?")
                .setNegativeButton("Не сохранять") { _, _ -> finish() }
                .setNeutralButton("Отмена", null)
                .setPositiveButton("Сохранить") { _, _ -> performSave() }
                .show()
        } else {
            finish()
        }
    }

    private fun confirmDelete() {
        showDialog(
            "Удалить заметку?",
            "Это действие нельзя отменить"
        ) {
            viewModel.deleteNote(noteId!!) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun showDialog(title: String, message: String, onConfirm: (() -> Unit)?) {
        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
        if (onConfirm != null) {
            builder.setPositiveButton("Удалить") { _, _ -> onConfirm() }
                .setNegativeButton("Отмена", null)
        } else {
            builder.setPositiveButton("OK", null)
        }
        builder.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBack()
    }
}
