package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityBookInfoBinding
import com.example.writersassistant.databinding.ActivityCharactersListBinding
import com.example.writersassistant.databinding.ActivityMainBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth

class CharactersListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharactersListBinding
    private lateinit var addCharacterButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityCharactersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addCharacterButton = findViewById(R.id.plusCharacterButton)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addCharacterButton.setImageResource(R.drawable.circle_plus_button_base_dark)

        addCharacterButton.setOnClickListener {
            startActivity(Intent(this@CharactersListActivity, CharacterInfoActivity::class.java))
            finish()
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
                else -> {startActivity(Intent(this@CharactersListActivity, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }
}