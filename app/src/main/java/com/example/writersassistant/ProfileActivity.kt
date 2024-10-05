package com.example.writersassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.writersassistant.databinding.ActivityMainBinding
import com.example.writersassistant.databinding.ActivityProfileBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: CircleImageView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var user: FirebaseUser
    private lateinit var userId: String
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            imageUri?.let {
                uploadImageToFirebase(it)
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val filePath = storageReference.child("profileImages/${user.uid}.jpg")

        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val data = baos.toByteArray()

        filePath.putBytes(data)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.profile_image_updated, Toast.LENGTH_SHORT).show()
                loadUserImage()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.image_loading_error, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserImage() {
        val filePath = storageReference.child("profileImages/${user.uid}.jpg")
        filePath.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(profileImageView)
        }.addOnFailureListener {
            profileImageView.setImageResource(R.drawable.ic_profile_page_icon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        user = FirebaseAuth.getInstance().currentUser!!
        userId = user.uid

        storageReference = FirebaseStorage.getInstance().reference

        mainLayout = findViewById(R.id.main)
        FirebaseApp.initializeApp(this)
        val isNightMode = LoadSettings.loadTheme(this)
        profileImageView = findViewById(R.id.profile_image)
        profileImageView.setOnClickListener {
            pickImage.launch(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }
        loadUserImage()
        val userLoginText: EditText = findViewById(R.id.editTextLogin)
        val userEmailText: EditText = findViewById(R.id.editTextEmail)
        userLoginText.setText(user.displayName)
        userEmailText.setText(user.email)
        val changeThemeButton: Button = findViewById(R.id.changeThemeButton)
        val changeAppLanguage: Button = findViewById(R.id.changeLanguage)
        val aboutProgramButton: Button = findViewById(R.id.aboutProgramButton)
        val logOutAccountButton: Button = findViewById(R.id.logOutOfAccountButton)
        auth = Firebase.auth
        changeAppLanguage.setOnClickListener {
            if (Locale.getDefault().language == "en") LoadSettings.setLocale(this, "ru")
            else LoadSettings.setLocale(this, "en")
            recreate()
        }
        profileImageView.setOnClickListener {
            pickImage.launch(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        if(isNightMode) {
            changeThemeButton.setBackgroundResource(R.drawable.rect_base_dark)
            changeAppLanguage.setBackgroundResource(R.drawable.rect_base_dark)
            aboutProgramButton.setBackgroundResource(R.drawable.rect_base_dark)
            logOutAccountButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        else {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
            changeThemeButton.setBackgroundResource(R.drawable.rect_base)
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