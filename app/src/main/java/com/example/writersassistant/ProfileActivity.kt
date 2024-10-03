package com.example.writersassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.writersassistant.databinding.ActivityMainBinding
import com.example.writersassistant.databinding.ActivityProfileBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: CircleImageView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .into(profileImageView)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        mainLayout = findViewById(R.id.main)
        val isNightMode = LoadSettings.loadTheme(this)
        val profileImageView: CircleImageView = findViewById(R.id.profile_image)
        val userLoginText: EditText = findViewById(R.id.editTextLogin)
        val userEmailText: EditText = findViewById(R.id.editTextEmail)
        userLoginText.setText(user?.displayName)
        userEmailText.setText(user?.email)
        val changeThemeButton: Button = findViewById(R.id.changeThemeButton)
        val changePasswordButton: Button = findViewById(R.id.changeAccountPassword)
        val changeAppLanguage: Button = findViewById(R.id.changeLanguage)
        val aboutProgramButton: Button = findViewById(R.id.aboutProgramButton)
        val logOutAccountButton: Button = findViewById(R.id.logOutOfAccountButton)
        auth = Firebase.auth

        changeAppLanguage.setOnClickListener {
            if (Locale.getDefault().language == "en") LoadSettings.setLocale(this, "ru")
            else LoadSettings.setLocale(this, "en")
            recreate()
        }

        if(isNightMode) {
            changeThemeButton.setBackgroundResource(R.drawable.rect_base_dark)
            changePasswordButton.setBackgroundResource(R.drawable.rect_base_dark)
            changeAppLanguage.setBackgroundResource(R.drawable.rect_base_dark)
            aboutProgramButton.setBackgroundResource(R.drawable.rect_base_dark)
            logOutAccountButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        else {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
            changeThemeButton.setBackgroundResource(R.drawable.rect_base)
            changePasswordButton.setBackgroundResource(R.drawable.rect_base)
            changeAppLanguage.setBackgroundResource(R.drawable.rect_base)
            aboutProgramButton.setBackgroundResource(R.drawable.rect_base)
            logOutAccountButton.setBackgroundResource(R.drawable.rect_base)
        }

        changeThemeButton.setOnClickListener {
            LoadSettings.setTheme(this, !isNightMode)
            recreate()
        }

        aboutProgramButton.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, AboutApplication::class.java))
        }

        logOutAccountButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.bottomNavigationView.selectedItemId = R.id.profilePage
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.mainPage -> {
                    startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                    finish()
                }
                R.id.profilePage -> true
                else -> {startActivity(Intent(this@ProfileActivity, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }
}