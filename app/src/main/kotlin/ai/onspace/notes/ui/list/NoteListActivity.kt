package ai.onspace.notes.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ai.onspace.notes.databinding.ActivityNoteListBinding
import ai.onspace.notes.ui.edit.EditNoteActivity

class NoteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteListBinding
    private val viewModel: NoteListViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    private val editNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.loadNotes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter { note ->
            val intent = Intent(this, EditNoteActivity::class.java).apply {
                putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id)
            }
            editNoteLauncher.launch(intent)
        }
        binding.rvNotes.adapter = adapter
        binding.rvNotes.setHasFixedSize(true)
    }

    private fun setupFab() {
        binding.fabCreate.setOnClickListener {
            val intent = Intent(this, EditNoteActivity::class.java)
            editNoteLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.notes.observe(this) { notes ->
            adapter.submitList(notes)
            if (notes.isEmpty() && viewModel.loading.value == false) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvNotes.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvNotes.visibility = View.VISIBLE
            }
        }
    }
}
