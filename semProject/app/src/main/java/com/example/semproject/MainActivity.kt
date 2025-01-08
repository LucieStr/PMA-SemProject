package com.example.semproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.semproject.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val books = mutableListOf<Library>()
    private lateinit var bookList: BookList
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        println("Firebase initialized successfully")
        firestore = FirebaseFirestore.getInstance()

        bookList = BookList(books, { book -> updateBook(book) }, { book -> showEditBookDialog(book) }, { book -> confirmDeleteBook(book) })

        binding.recyclerViewBooks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBooks.adapter = bookList

        binding.fabAddBook.setOnClickListener {
            showAddBookDialog()
        }

        loadBooksFromFirestore()
        listenToBooksUpdates()

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                searchBooks(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateBook(book: Library) {
        if (book.id.isNotBlank()) {
            val bookRef = firestore.collection("books").document(book.id)
            val updates = hashMapOf<String, Any>(
                "name" to book.name,
                "autor" to book.autor,
                "place" to book.place,
                "type" to book.type,
                "taken" to book.taken
            )

            bookRef.update(updates)
                .addOnSuccessListener {
                    println("Book updated successfully: ${book.name}")
                    Toast.makeText(this, "Aktualizováno", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    println("Error updating book: ${e.message}")
                    Toast.makeText(this, "Chyba", Toast.LENGTH_SHORT).show()
                }
        } else {
            println("Error: Book ID is blank")
            Toast.makeText(this, "Kniha nemá ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddBookDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Přidat knihu")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val inputName = EditText(this)
        inputName.hint = "Název knihy"
        inputName.inputType = InputType.TYPE_CLASS_TEXT
        layout.addView(inputName)

        val inputAuthor = EditText(this)
        inputAuthor.hint = "Autor knihy"
        inputAuthor.inputType = InputType.TYPE_CLASS_TEXT
        layout.addView(inputAuthor)

        val inputPlace = EditText(this)
        inputPlace.hint = "Místo"
        inputPlace.inputType = InputType.TYPE_CLASS_TEXT
        layout.addView(inputPlace)

        val inputType = EditText(this)
        inputType.hint = "Typ knihy"
        inputType.inputType = InputType.TYPE_CLASS_TEXT
        layout.addView(inputType)

        builder.setView(layout)

        builder.setPositiveButton("Přidat") { _, _ ->
            val bookName = inputName.text.toString()
            val bookAuthor = inputAuthor.text.toString()
            val bookPlace = inputPlace.text.toString()
            val bookType = inputType.text.toString()
            if (bookName.isNotBlank()) {
                addBook(bookName, bookAuthor, bookPlace, bookType)
            } else {
                Toast.makeText(this, "Kniha nemůže být bez názvu", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Zrušit") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showEditBookDialog(book: Library) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Upravit knihu")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val inputName = EditText(this)
        inputName.hint = "Název knihy"
        inputName.inputType = InputType.TYPE_CLASS_TEXT
        inputName.setText(book.name)
        layout.addView(inputName)

        val inputAuthor = EditText(this)
        inputAuthor.hint = "Autor knihy"
        inputAuthor.inputType = InputType.TYPE_CLASS_TEXT
        inputAuthor.setText(book.autor)
        layout.addView(inputAuthor)

        val inputPlace = EditText(this)
        inputPlace.hint = "Místo"
        inputPlace.inputType = InputType.TYPE_CLASS_TEXT
        inputPlace.setText(book.place)
        layout.addView(inputPlace)

        val inputType = EditText(this)
        inputType.hint = "Typ knihy"
        inputType.inputType = InputType.TYPE_CLASS_TEXT
        inputType.setText(book.type)
        layout.addView(inputType)

        builder.setView(layout)

        builder.setPositiveButton("Upravit") { _, _ ->
            val bookName = inputName.text.toString()
            val bookAuthor = inputAuthor.text.toString()
            val bookPlace = inputPlace.text.toString()
            val bookType = inputType.text.toString()
            if (bookName.isNotBlank()) {
                book.name = bookName
                book.autor = bookAuthor
                book.place = bookPlace
                book.type = bookType
                updateBook(book)
            } else {
                Toast.makeText(this, "Kniha nemůže být bez názvu", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Zrušit") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun confirmDeleteBook(book: Library) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Smazat knihu")
        builder.setMessage("Opravdu chcete smazat tuto knihu?")

        builder.setPositiveButton("Ano") { _, _ ->
            deleteBook(book)
        }
        builder.setNegativeButton("Ne") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteBook(book: Library) {
        if (book.id.isNotBlank()) {
            val bookRef = firestore.collection("books").document(book.id)
            bookRef.delete()
                .addOnSuccessListener {
                    books.remove(book)
                    bookList.notifyDataSetChanged()
                    println("Book deleted successfully: ${book.name}")
                    Toast.makeText(this, "Smazáno", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    println("Error deleting book: ${e.message}")
                    Toast.makeText(this, "Chyba", Toast.LENGTH_SHORT).show()
                }
        } else {
            println("Error: Book ID is blank")
            Toast.makeText(this, "Kniha nemá ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addBook(name: String, autor: String, place: String, type: String) {
        val newBook = Library(
            id = firestore.collection("books").document().id,
            name = name,
            autor = autor,
            place = place,
            type = type,
            taken = false
        )
        firestore.collection("books").document(newBook.id).set(newBook)
            .addOnSuccessListener {
                println("Book added to Firestore: $name")
            }
            .addOnFailureListener { e ->
                println("Error adding book: ${e.message}")
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadBooksFromFirestore() {
        firestore.collection("books").get()
            .addOnSuccessListener { result ->
                books.clear()
                for (document in result) {
                    val book = document.toObject(Library::class.java)
                    books.add(book)
                }
                bookList.notifyDataSetChanged()
                println("Books loaded from Firestore")
            }
            .addOnFailureListener { e ->
                println("Error loading books: ${e.message}")
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenToBooksUpdates() {
        firestore.collection("books").addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Listen failed: ${e.message}")
                return@addSnapshotListener
            }

            books.clear()
            for (document in snapshots!!) {
                val book = document.toObject(Library::class.java)
                books.add(book)
            }
            bookList.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchBooks(query: String) {
        if (query.isNotBlank()) {
            firestore.collection("books")
                .whereEqualTo("name", query)
                .get()
                .addOnSuccessListener { result ->
                    books.clear()
                    for (document in result) {
                        val book = document.toObject(Library::class.java)
                        books.add(book)
                    }
                     bookList.notifyDataSetChanged()
                    println("Books loaded from Firestore")
                }
                .addOnFailureListener { e ->
                    println("Error loading books: ${e.message}")
                }
        } else {
            // Pokud je vyhledávací dotaz prázdný, načtěte všechny knihy
            loadBooksFromFirestore()
        }
    }
}