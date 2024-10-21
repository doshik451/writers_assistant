package com.example.writersassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.writersassistant.databinding.ActivityMainBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.example.writersassistant.BookInfoActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var addBookButton: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var bookListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerPermissionListener()
        checkStoragePermission()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addBookButton = findViewById(R.id.plusBookButton)
        database = FirebaseDatabase.getInstance().reference.child("books")
        bookListLayout = findViewById(R.id.bookListLayout)
        loadBooks()
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addBookButton.setImageResource(R.drawable.circle_plus_button_base_dark)
        addBookButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, BookInfoActivity::class.java))
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> true
                R.id.searchPage -> {
                    startActivity(Intent(this@MainActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@MainActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun checkStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun registerPermissionListener() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, R.string.thxxx, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.giveAsYourPhoto, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun loadBooks() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val authorId = bookSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        val bookTitle = bookSnapshot.child("title").value.toString()
                        val bookButton = Button(this@MainActivity).apply {
                            text = bookTitle
                            layoutParams = LinearLayout.LayoutParams(
                                (350 * density).toInt(),
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, (10 * density).toInt(), 0, (10 * density).toInt())
                                gravity = Gravity.CENTER_HORIZONTAL
                            }
                            setTextAppearance(R.style.ButtonTextStyle)
                            setOnClickListener {
                                val intent = Intent(this@MainActivity, BookInfoActivity::class.java)
                                intent.putExtra("BOOK_ID", bookId)
                                startActivity(intent)
                            }
                        }
                        bookButton.setPadding((20 * density).toInt(), (5 * density).toInt(), (20 * density).toInt(), (5 * density).toInt())
                        if(isNightMode) bookButton.setBackgroundResource(R.drawable.rect_base_dark)
                        else bookButton.setBackgroundResource(R.drawable.rect_base)
                        bookListLayout.addView(bookButton)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }
}