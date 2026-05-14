package ai.onspace.notes.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ai.onspace.notes.data.model.Note
import ai.onspace.notes.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            val preview = note.content.trim().replace('\n', ' ').take(100)
            if (preview.isNotEmpty()) {
                binding.tvPreview.text = preview
                binding.tvPreview.visibility = android.view.View.VISIBLE
            } else {
                binding.tvPreview.visibility = android.view.View.GONE
            }
            binding.tvDate.text = formatDate(note.updatedAt)
            binding.root.setOnClickListener { onNoteClick(note) }
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diffMs = now - timestamp
            val diffMins = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

            return when {
                diffMins < 1 -> "Только что"
                diffMins < 60 -> "$diffMins мин назад"
                diffHours < 24 -> "$diffHours ч назад"
                diffDays < 7 -> "$diffDays дн назад"
                else -> SimpleDateFormat("d MMM", Locale("ru")).format(Date(timestamp))
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}
