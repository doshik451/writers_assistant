package com.example.writersassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.writersassistant.databinding.ActivityCharacterInfoBinding
import com.example.writersassistant.databinding.ActivityCharactersListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class CharacterInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharacterInfoBinding
    private lateinit var characterImage: CircleImageView
    private lateinit var characterAppearanceDescription: EditText
    private lateinit var roleInTheBook: EditText
    private lateinit var description: EditText
    private lateinit var connectionButton: Button
    private lateinit var groupsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityCharacterInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        characterImage = findViewById(R.id.characterImage)
        characterAppearanceDescription = findViewById(R.id.characterAppearanceDescriptionEditText)
        roleInTheBook = findViewById(R.id.roleInTheBookEditText)
        description = findViewById(R.id.descriptionEditText)
        connectionButton = findViewById(R.id.connectionButton)
        groupsButton = findViewById(R.id.groupsButton)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else{
            characterAppearanceDescription.setBackgroundResource(R.drawable.rect_base_dark)
            roleInTheBook.setBackgroundResource(R.drawable.rect_base_dark)
            description.setBackgroundResource(R.drawable.rect_base_dark)
            connectionButton.setBackgroundResource(R.drawable.rect_base_dark)
            groupsButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@CharacterInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@CharacterInfoActivity, MainActivity::class.java))
                    finish()
                }
                else -> {startActivity(Intent(this@CharacterInfoActivity, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }
}