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
import com.example.writersassistant.databinding.ActivityChaptersListBinding
import com.example.writersassistant.databinding.ActivityCharactersListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChaptersListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChaptersListBinding
    private lateinit var addChapterButton: ImageView
    private lateinit var bookId: String
    private lateinit var database: DatabaseReference
    private lateinit var chaptersListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityChaptersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addChapterButton = findViewById(R.id.plusChapterButton)
        bookId = intent.getStringExtra("BOOK_ID").toString()
        chaptersListLayout = findViewById(R.id.chaptersListLayout)
        database = FirebaseDatabase.getInstance().reference.child("books")
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addChapterButton.setImageResource(R.drawable.circle_plus_button_base_dark)

        loadChapters()
        addChapterButton.setOnClickListener {
            val intent = Intent(this@ChaptersListActivity, ChaptersInfoActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@ChaptersListActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@ChaptersListActivity, MainActivity::class.java))
                    finish()
                }
                R.id.searchPage -> {
                    startActivity(Intent(this@ChaptersListActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@ChaptersListActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadChapters() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val currentBook = intent.getStringExtra("BOOK_ID")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chaptersListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val authorId = bookSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        if(bookId == currentBook) {
                            for (chapterSnapshot in bookSnapshot.child("chapters").children) {
                                val chapterId = chapterSnapshot.key ?: continue
                                val chapterName = chapterSnapshot.child("chapterName").value.toString()
                                val chapterButton = Button(this@ChaptersListActivity).apply {
                                    text = chapterName
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
                                            this@ChaptersListActivity,
                                            ChaptersInfoActivity::class.java
                                        )
                                        intent.putExtra("BOOK_ID", bookId)
                                        intent.putExtra("CHAPTER_ID", chapterId)
                                        startActivity(intent)
                                    }
                                }
                                chapterButton.setPadding((20 * density).toInt(),(5 * density).toInt(),(20 * density).toInt(),(5 * density).toInt())
                                if (isNightMode) chapterButton.setBackgroundResource(R.drawable.rect_base_dark)
                                else chapterButton.setBackgroundResource(R.drawable.rect_base)
                                chaptersListLayout.addView(chapterButton)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChaptersListActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }
}