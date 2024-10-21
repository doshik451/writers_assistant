package com.example.writersassistant

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityBookInfoBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class BookInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookInfoBinding
    private lateinit var authorNameInput: EditText
    private lateinit var user: FirebaseUser
    private lateinit var bookDescription: EditText
    private lateinit var charactersButton: Button
    private lateinit var chaptersButton: Button
    private lateinit var dictionaryButton: Button
    private lateinit var locationButton: Button
    private lateinit var deleteBookButton: Button
    private lateinit var bookTitle: EditText
    private lateinit var database: DatabaseReference
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        binding = ActivityBookInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        val isNightMode = LoadSettings.loadTheme(this)
        bookDescription = findViewById(R.id.bookDescriptionEditText)
        charactersButton = findViewById(R.id.characterListButton)
        chaptersButton = findViewById(R.id.chaptersListButton)
        locationButton = findViewById(R.id.locationListButton)
        dictionaryButton = findViewById(R.id.dictionaryButton)
        bookTitle = findViewById(R.id.bookTitleEditText)
        deleteBookButton = findViewById(R.id.deleteBookButton)

        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else{
            bookDescription.setBackgroundResource(R.drawable.rect_base_dark)
            charactersButton.setBackgroundResource(R.drawable.rect_base_dark)
            chaptersButton.setBackgroundResource(R.drawable.rect_base_dark)
            locationButton.setBackgroundResource(R.drawable.rect_base_dark)
            deleteBookButton.setBackgroundResource(R.drawable.rect_base_dark)
            dictionaryButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        authorNameInput = findViewById(R.id.authorNameEditText)
        user = FirebaseAuth.getInstance().currentUser!!

        database = FirebaseDatabase.getInstance().reference
        authorNameInput.setText(user.displayName)
        bookId = intent.getStringExtra("BOOK_ID") ?: database.child("books").push().key
        if (bookId != null) loadBookData(bookId!!)
        setupTextChangeListeners()
        deleteBookButton.setOnClickListener { deleteBook() }

        charactersButton.setOnClickListener {
            val intent = Intent(this@BookInfoActivity, CharactersListActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }
        chaptersButton.setOnClickListener {
            val intent = Intent(this@BookInfoActivity, ChaptersListActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        locationButton.setOnClickListener {
            val intent = Intent(this@BookInfoActivity, PlacesListActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        dictionaryButton.setOnClickListener {
            val intent = Intent(this@BookInfoActivity, DictionaryBookActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }


        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@BookInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@BookInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.searchPage -> {
                    startActivity(Intent(this@BookInfoActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@BookInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }
    private fun loadBookData(bookId: String) {
        database.child("books").child(bookId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                bookTitle.setText(snapshot.child("title").value.toString())
                bookDescription.setText(snapshot.child("description").value.toString())
                authorNameInput.setText(snapshot.child("authorName").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        bookTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveBookData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        bookDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveBookData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        authorNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveBookData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveBookData() {
        val bookTitleText: String = bookTitle.text.toString()
        val authorName: String = authorNameInput.text.toString()

        if(bookTitleText.isNullOrEmpty() || authorName.isNullOrEmpty()) return

        val bookData = mapOf(
            "title" to bookTitleText,
            "description" to bookDescription.text.toString(),
            "authorId" to user.uid,
            "authorName" to authorName
        )
        bookId?.let {
            database.child("books").child(it).updateChildren(bookData)
        }
    }

    private fun deleteBook() {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingBookText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                bookId?.let {
                    database.child("books").child(it).removeValue().addOnSuccessListener {
                        Toast.makeText(this, R.string.bookDeleted, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@BookInfoActivity, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener { Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()}
                }
            }.setNegativeButton(R.string.answerNo, null).show()
    }
}