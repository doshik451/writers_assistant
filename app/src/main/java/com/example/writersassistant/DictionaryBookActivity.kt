package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityDictionaryBookBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DictionaryBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDictionaryBookBinding
    private lateinit var addWordButton: ImageView
    private lateinit var bookId: String
    private lateinit var database: DatabaseReference
    private lateinit var wordsListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityDictionaryBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addWordButton = findViewById(R.id.plusWordButton)
        bookId = intent.getStringExtra("BOOK_ID").toString()
        wordsListLayout = findViewById(R.id.wordsListLayout)
        database = FirebaseDatabase.getInstance().reference.child("books")
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addWordButton.setImageResource(R.drawable.circle_plus_button_base_dark)

        loadWords()
        addWordButton.setOnClickListener {
            val intent = Intent(this@DictionaryBookActivity, WordInfoActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@DictionaryBookActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@DictionaryBookActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@DictionaryBookActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadWords() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val currentBook = intent.getStringExtra("BOOK_ID")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                wordsListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val authorId = bookSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        if(bookId == currentBook) {
                            for (dictionarySnapshot in bookSnapshot.child("dictionary").children) {
                                val wordId = dictionarySnapshot.key ?: continue
                                val wordTitle = dictionarySnapshot.child("wordTitle").value.toString()
                                val wordButton = Button(this@DictionaryBookActivity).apply {
                                    text = wordTitle
                                    layoutParams = LinearLayout.LayoutParams(
                                        (350 * density).toInt(),
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(0, (10 * density).toInt(), 0, (10 * density).toInt())
                                        gravity = Gravity.CENTER_HORIZONTAL
                                    }
                                    setTextAppearance(R.style.ButtonTextStyle)
                                    setOnClickListener {
                                        val intent = Intent(
                                            this@DictionaryBookActivity,
                                            WordInfoActivity::class.java
                                        )
                                        intent.putExtra("BOOK_ID", bookId)
                                        intent.putExtra("WORD_ID", wordId)
                                        startActivity(intent)
                                    }
                                }
                                wordButton.setPadding((20 * density).toInt(),(5 * density).toInt(),(20 * density).toInt(),(5 * density).toInt())
                                if (isNightMode) wordButton.setBackgroundResource(R.drawable.rect_base_dark)
                                else wordButton.setBackgroundResource(R.drawable.rect_base)
                                wordsListLayout.addView(wordButton)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DictionaryBookActivity, "Failed to load words.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}