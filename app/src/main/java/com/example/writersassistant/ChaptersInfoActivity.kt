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
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChaptersInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChaptersInfoBinding
    private lateinit var chapterNameET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var keyPointsET: EditText
    private lateinit var deleteChapterButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var bookId: String
    private var chapterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityChaptersInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        chapterId = intent.getStringExtra("CHAPTER_ID") ?: database.child("books").child(bookId).push().key
        chapterNameET = findViewById(R.id.chapterTitleEditText)
        descriptionET = findViewById(R.id.chapterDescriptionEditText)
        keyPointsET = findViewById(R.id.keyPointsEditText)
        deleteChapterButton = findViewById(R.id.deleteChapterButton)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            descriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            keyPointsET.setBackgroundResource(R.drawable.rect_base_dark)
            deleteChapterButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        if (chapterId != null) loadChapterData(bookId, chapterId!!)
        deleteChapterButton.setOnClickListener { deleteChapter(bookId) }
        setupTextChangeListeners()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@ChaptersInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@ChaptersInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.searchPage -> {
                    startActivity(Intent(this@ChaptersInfoActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@ChaptersInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> {startActivity(Intent(this@ChaptersInfoActivity, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }
    private fun loadChapterData(bookId: String, chapterId: String) {
        database.child("books").child(bookId).child("chapters").child(chapterId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                chapterNameET.setText(snapshot.child("chapterName").value.toString())
                descriptionET.setText(snapshot.child("description").value.toString())
                keyPointsET.setText(snapshot.child("chapterKeyPoints").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        chapterNameET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveChapterData(bookId.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveChapterData(bookId.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        keyPointsET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveChapterData(bookId.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveChapterData(bookId: String) {
        val chapterNameText: String = chapterNameET.text.toString()

        if(chapterNameText.isNullOrEmpty()) return

        val chapterData = mapOf(
            "chapterName" to chapterNameText,
            "description" to descriptionET.text.toString(),
            "chapterKeyPoints" to keyPointsET.text.toString()
        )
        chapterId?.let {
            database.child("books").child(bookId).child("chapters").child(it).updateChildren(chapterData)
        }
    }

    private fun deleteChapter(bookId: String) {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingChapterText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                chapterId?.let {
                    database.child("books").child(bookId).child("chapters").child(it).removeValue().addOnSuccessListener {
                        Toast.makeText(this, R.string.chapterDeleted, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ChaptersInfoActivity, ChaptersListActivity::class.java)
                        intent.putExtra("BOOK_ID", bookId)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()}
                }
            }.setNegativeButton(R.string.answerNo, null).show()
    }
}