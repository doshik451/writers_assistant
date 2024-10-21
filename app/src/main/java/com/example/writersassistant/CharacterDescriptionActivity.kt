package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityCharacterDescriptionBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CharacterDescriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharacterDescriptionBinding
    private lateinit var appearanceDescriptionET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var goalET: EditText
    private lateinit var motivationET: EditText
    private lateinit var prehistoryET: EditText
    private lateinit var database: DatabaseReference
    private var characterId: String? = null
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCharacterDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        val isNightMode = LoadSettings.loadTheme(this)
        appearanceDescriptionET = findViewById(R.id.characterAppearanceDescriptionEditText)
        descriptionET = findViewById(R.id.descriptionEditText)
        goalET = findViewById(R.id.characterGoalEditText)
        motivationET = findViewById(R.id.characterMotivationEditText)
        prehistoryET = findViewById(R.id.characterPrehistoryEditText)

        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        characterId = intent.getStringExtra("CHARACTER_ID")

        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else{
            appearanceDescriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            descriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            goalET.setBackgroundResource(R.drawable.rect_base_dark)
            motivationET.setBackgroundResource(R.drawable.rect_base_dark)
            prehistoryET.setBackgroundResource(R.drawable.rect_base_dark)
        }

        if (characterId != null) loadCharacterData(bookId!!, characterId!!)
        setupTextChangeListeners()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@CharacterDescriptionActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@CharacterDescriptionActivity, MainActivity::class.java))
                    finish()
                }
                R.id.searchPage -> {
                    startActivity(Intent(this@CharacterDescriptionActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@CharacterDescriptionActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadCharacterData(bookId: String, characterId: String) {
        database.child("books").child(bookId).child("characters").child(characterId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                appearanceDescriptionET.setText(snapshot.child("characterAppearanceDescription").value?.toString() ?: "")
                descriptionET.setText(snapshot.child("description").value?.toString() ?: "")
                goalET.setText(snapshot.child("characterGoal").value?.toString() ?: "")
                motivationET.setText(snapshot.child("characterMotivation").value?.toString() ?: "")
                prehistoryET.setText(snapshot.child("characterPrehistory").value?.toString() ?: "")
            }
        }
    }

    private fun setupTextChangeListeners() {
        appearanceDescriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveCharacterData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveCharacterData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        goalET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveCharacterData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        motivationET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveCharacterData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        prehistoryET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveCharacterData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveCharacterData() {
        val characterData = mapOf(
            "characterAppearanceDescription" to appearanceDescriptionET.text.toString(),
            "description" to descriptionET.text.toString(),
            "characterGoal" to goalET.text.toString(),
            "characterMotivation" to motivationET.text.toString(),
            "characterPrehistory" to prehistoryET.text.toString()
        )
        characterId?.let {
            database.child("books").child(bookId!!).child("characters").child(it).updateChildren(characterData)
        }
    }
}