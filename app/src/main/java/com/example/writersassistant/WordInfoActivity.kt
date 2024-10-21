package com.example.writersassistant

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityChaptersInfoBinding
import com.example.writersassistant.databinding.ActivityWordInfoBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WordInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordInfoBinding
    private lateinit var wordTitleET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var deleteWordButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var bookId: String
    private var wordId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityWordInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        wordId = intent.getStringExtra("WORD_ID") ?: database.child("books").child(bookId).push().key
        wordTitleET = findViewById(R.id.wordTitleEditText)
        descriptionET = findViewById(R.id.wordDescriptionEditText)
        deleteWordButton = findViewById(R.id.deleteWordButton)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            descriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            deleteWordButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        if (wordId != null) loadWordData(bookId, wordId!!)
        deleteWordButton.setOnClickListener { deleteWord() }
        setupTextChangeListeners()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@WordInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@WordInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.searchPage -> {
                    startActivity(Intent(this@WordInfoActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@WordInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }
    private fun loadWordData(bookId: String, wordId: String) {
        database.child("books").child(bookId).child("dictionary").child(wordId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                wordTitleET.setText(snapshot.child("wordTitle").value.toString())
                descriptionET.setText(snapshot.child("description").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        wordTitleET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveWordData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveWordData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveWordData() {
        val wordTitleText: String = wordTitleET.text.toString()

        if(wordTitleText.isNullOrEmpty()) return

        val wordData = mapOf(
            "wordTitle" to wordTitleText,
            "description" to descriptionET.text.toString()
        )
        wordId?.let {
            database.child("books").child(bookId).child("dictionary").child(it).updateChildren(wordData)
        }
    }

    private fun deleteWord() {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingWordText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                wordId?.let {
                    database.child("books").child(bookId).child("dictionary").child(it).removeValue().addOnSuccessListener {
                        Toast.makeText(this, R.string.wordDeleted, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@WordInfoActivity, DictionaryBookActivity::class.java)
                        intent.putExtra("BOOK_ID", bookId)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()}
                }
            }.setNegativeButton(R.string.answerNo, null).show()
    }
}