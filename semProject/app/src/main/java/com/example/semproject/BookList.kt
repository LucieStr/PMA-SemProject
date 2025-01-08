package com.example.semproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.semproject.databinding.BookBinding

class BookList (
    private val books: List<Library>, //seznam knih
    private val onBookTaken: (Library) -> Unit,//Calback pro změnu stavu
    private val onEditBook: (Library) -> Unit,
    private val onDeleteBook: (Library) -> Unit
    ) : RecyclerView.Adapter<BookList.BookViewHolder>(){



        inner class BookViewHolder(private val binding: BookBinding) :RecyclerView.ViewHolder(binding.root) {
            fun bind(book: Library) {
                binding.tvBookName.text = book.name
                binding.tvAutor.text = book.autor
                binding.tvType.text = book.type
                binding.tvPlace.text = book.place
                // Temporarily remove the listener to avoid triggering it during binding
                binding.cbBookTaken.setOnCheckedChangeListener(null)
                binding.cbBookTaken.isChecked = book.taken

                // Re-add the listener after setting the checkbox state
                binding.cbBookTaken.setOnCheckedChangeListener { _, isChecked ->
                    book.taken = isChecked
                    onBookTaken(book)
                }
                binding.btnEdit.setOnClickListener {
                    onEditBook(book)
                }

                binding.btnDelete.setOnClickListener {
                    onDeleteBook(book)
                }

            }
        }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
    val binding = BookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return BookViewHolder(binding)
}

override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
    holder.bind(books[position])
}

override fun getItemCount(): Int = books.size



}

