package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivitySearchingBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchingBinding
    private lateinit var searchingInput: EditText
    private lateinit var searchingResult: LinearLayout
    private var isNightMode: Boolean = false
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivitySearchingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        searchingInput = findViewById(R.id.searchingInputET)
        searchingResult = findViewById(R.id.searchingResultListLayout)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))

        searchingInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    searchingResult.removeAllViews() // Очистка результатов
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.bottomNavigationView.selectedItemId = R.id.searchPage
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.mainPage -> {
                    startActivity(Intent(this@SearchingActivity, MainActivity::class.java))
                    finish()
                }
                R.id.profilePage -> {
                    startActivity(Intent(this@SearchingActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.searchPage -> true
                R.id.ideasPage -> {
                    startActivity(Intent(this@SearchingActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun performSearch(query: String) {
        searchingResult.removeAllViews()

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        database.child("books").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (bookSnapshot in snapshot.children) {
                    val authorId = bookSnapshot.child("authorId").value.toString()

                    if (authorId == currentUserId) {
                        val bookTitle = bookSnapshot.child("title").value.toString()
                        val authorName = bookSnapshot.child("authorName").value.toString()

                        if (bookTitle.contains(query, true) || authorName.contains(query, true)) {
                            addButtonToResults(getString(R.string.book) + ": " + bookTitle, bookSnapshot.key.toString(), bookSnapshot.key.toString(), "book")
                        }

                        val charactersRef = bookSnapshot.child("characters")
                        for (characterSnapshot in charactersRef.children) {
                            val characterName = characterSnapshot.child("characterName").value.toString()
                            if (characterName.contains(query, true)) {
                                addButtonToResults(getString(R.string.book) + ": " + bookTitle + " " + getString(R.string.character) + ": " + characterName, characterSnapshot.key.toString(), bookSnapshot.key.toString(), "character")
                            }
                        }

                        val chaptersRef = bookSnapshot.child("chapters")
                        for (chapterSnapshot in chaptersRef.children) {
                            val chapterName = chapterSnapshot.child("chapterName").value.toString()
                            if (chapterName.contains(query, true)) {
                                addButtonToResults(getString(R.string.book) + ": " + bookTitle + " " + getString(R.string.chapter) + ": " + chapterName, chapterSnapshot.key.toString(), bookSnapshot.key.toString(), "chapter")
                            }
                        }

                        val dictionaryRef = bookSnapshot.child("dictionary")
                        for (wordSnapshot in dictionaryRef.children) {
                            val wordTitle = wordSnapshot.child("wordTitle").value.toString()
                            if (wordTitle.contains(query, true)) {
                                addButtonToResults(getString(R.string.book) + ": " + bookTitle + " " + getString(R.string.word) + ": " + wordTitle, wordSnapshot.key.toString(), bookSnapshot.key.toString(), "dictionary")
                            }
                        }

                        val placesRef = bookSnapshot.child("places")
                        for (placeSnapshot in placesRef.children) {
                            val placeTitle = placeSnapshot.child("placeTitle").value.toString()
                            if (placeTitle.contains(query, true)) {
                                addButtonToResults(getString(R.string.book) + ": " + bookTitle + " " + getString(R.string.place) + ": " + placeTitle, placeSnapshot.key.toString(), bookSnapshot.key.toString(), "place")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchingActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })

        database.child("ideas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ideaSnapshot in snapshot.children) {
                    val authorId = ideaSnapshot.child("authorId").value.toString()

                    if (authorId == currentUserId) {
                        val ideaTitle = ideaSnapshot.child("title").value.toString()
                        if (ideaTitle.contains(query, true)) {
                            addButtonToResults(getString(R.string.idea) + ": " + ideaTitle, ideaSnapshot.key.toString(), "", "idea")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchingActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addButtonToResults(buttonText: String, itemId: String, bookId: String, itemType: String) {
        val density = resources.displayMetrics.density
        val button = Button(this@SearchingActivity).apply {
            text = buttonText
            layoutParams = LinearLayout.LayoutParams(
                (350 * density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, (10 * density).toInt(), 0, (10 * density).toInt())
                gravity = Gravity.CENTER_HORIZONTAL
            }
            setTextAppearance(R.style.ButtonTextStyle)
            setOnClickListener {
                when (itemType) {
                    "book" -> {
                        val intent = Intent(this@SearchingActivity, BookInfoActivity::class.java)
                        intent.putExtra("BOOK_ID", itemId)
                        startActivity(intent)
                    }

                    "character" -> {
                        val intent = Intent(this@SearchingActivity, CharacterInfoActivity::class.java)
                        intent.putExtra("CHARACTER_ID", itemId)
                        intent.putExtra("BOOK_ID", bookId)
                        startActivity(intent)
                    }

                    "chapter" -> {
                        val intent = Intent(this@SearchingActivity, ChaptersInfoActivity::class.java)
                        intent.putExtra("BOOK_ID", bookId)
                        intent.putExtra("CHAPTER_ID", itemId)
                        startActivity(intent)
                    }

                    "dictionary" -> {
                        val intent = Intent(this@SearchingActivity, WordInfoActivity::class.java)
                        intent.putExtra("BOOK_ID", bookId)
                        intent.putExtra("WORD_ID", itemId)
                        startActivity(intent)
                    }

                    "place" -> {
                        val intent = Intent(this@SearchingActivity, PlaceInfoActivity::class.java)
                        intent.putExtra("PLACE_ID", itemId)
                        intent.putExtra("BOOK_ID", bookId)
                        startActivity(intent)
                    }

                    "idea" -> {
                        val intent = Intent(this@SearchingActivity, IdeaInfoActivity::class.java)
                        intent.putExtra("IDEA_ID", itemId)
                        startActivity(intent)
                    }
                }
            }

        }
        button.setPadding((20 * density).toInt(), (5 * density).toInt(), (20 * density).toInt(), (5 * density).toInt())
        if(isNightMode) button.setBackgroundResource(R.drawable.rect_base_dark)
        else button.setBackgroundResource(R.drawable.rect_base)
        searchingResult.addView(button)
    }
}
