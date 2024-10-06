package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityBookInfoBinding
import com.example.writersassistant.databinding.ActivityMainBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class BookInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookInfoBinding
    private lateinit var authorNameInput: EditText
    private lateinit var user: FirebaseUser
    private lateinit var bookDescription: EditText
    private lateinit var charactersButton: Button
    private lateinit var chaptersButton: Button
    private lateinit var ideasButton: Button
    private lateinit var dictionaryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        ideasButton = findViewById(R.id.ideasListButton)
        dictionaryButton = findViewById(R.id.dictionaryListButton)

        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else{
            bookDescription.setBackgroundResource(R.drawable.rect_base_dark)
            charactersButton.setBackgroundResource(R.drawable.rect_base_dark)
            chaptersButton.setBackgroundResource(R.drawable.rect_base_dark)
            ideasButton.setBackgroundResource(R.drawable.rect_base_dark)
            dictionaryButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        authorNameInput = findViewById(R.id.authorNameEditText)
        user = FirebaseAuth.getInstance().currentUser!!
        authorNameInput.setText(user.displayName)

        charactersButton.setOnClickListener {
            startActivity(Intent(this@BookInfoActivity, CharactersListActivity::class.java))
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
                else -> {startActivity(Intent(this@BookInfoActivity, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }
}