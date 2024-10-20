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
import com.example.writersassistant.databinding.ActivityCharactersListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CharactersListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharactersListBinding
    private lateinit var addCharacterButton: ImageView
    private lateinit var bookId: String
    private lateinit var database: DatabaseReference
    private lateinit var charactersListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityCharactersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addCharacterButton = findViewById(R.id.plusCharacterButton)
        bookId = intent.getStringExtra("BOOK_ID").toString()
        charactersListLayout = findViewById(R.id.charactersListLayout)
        database = FirebaseDatabase.getInstance().reference.child("books")
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addCharacterButton.setImageResource(R.drawable.circle_plus_button_base_dark)

        loadCharacters()

        addCharacterButton.setOnClickListener {
            val intent = Intent(this@CharactersListActivity, CharacterInfoActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@CharactersListActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@CharactersListActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@CharactersListActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }
    private fun loadCharacters() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val currentBook = intent.getStringExtra("BOOK_ID")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                charactersListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val authorId = bookSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        if(bookId == currentBook) {
                            for (characterSnapshot in bookSnapshot.child("characters").children) {
                                val characterId = characterSnapshot.key ?: continue
                                val characterName = characterSnapshot.child("characterName").value.toString()
                                val characterButton = Button(this@CharactersListActivity).apply {
                                    text = characterName
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
                                            this@CharactersListActivity,
                                            CharacterInfoActivity::class.java
                                        )
                                        intent.putExtra("BOOK_ID", bookId)
                                        intent.putExtra("CHARACTER_ID", characterId)
                                        startActivity(intent)
                                    }
                                }
                                characterButton.setPadding((20 * density).toInt(),(5 * density).toInt(),(20 * density).toInt(),(5 * density).toInt())
                                if (isNightMode) characterButton.setBackgroundResource(R.drawable.rect_base_dark)
                                else characterButton.setBackgroundResource(R.drawable.rect_base)
                                charactersListLayout.addView(characterButton)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CharactersListActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }
}