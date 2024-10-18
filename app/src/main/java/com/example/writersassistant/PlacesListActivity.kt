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
import com.example.writersassistant.databinding.ActivityPlacesListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlacesListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesListBinding
    private lateinit var addPlaceButton: ImageView
    private lateinit var bookId: String
    private lateinit var database: DatabaseReference
    private lateinit var placesListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityPlacesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addPlaceButton = findViewById(R.id.plusPlaceButton)
        bookId = intent.getStringExtra("BOOK_ID").toString()
        placesListLayout = findViewById(R.id.placesListLayout)
        database = FirebaseDatabase.getInstance().reference.child("books")
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addPlaceButton.setImageResource(R.drawable.circle_plus_button_base_dark)

        loadPlaces()

        addPlaceButton.setOnClickListener {
            val intent = Intent(this@PlacesListActivity, PlaceInfoActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@PlacesListActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@PlacesListActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@PlacesListActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }
    private fun loadPlaces() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val currentBook = intent.getStringExtra("BOOK_ID")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placesListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val authorId = bookSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        if(bookId == currentBook) {
                            for (placeSnapshot in bookSnapshot.child("places").children) {
                                val placeId = placeSnapshot.key ?: continue
                                val placeName = placeSnapshot.child("placeTitle").value.toString()
                                val placeButton = Button(this@PlacesListActivity).apply {
                                    text = placeName
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
                                            this@PlacesListActivity,
                                            PlaceInfoActivity::class.java
                                        )
                                        intent.putExtra("BOOK_ID", bookId)
                                        intent.putExtra("PLACE_ID", placeId)
                                        startActivity(intent)
                                    }
                                }
                                placeButton.setPadding((20 * density).toInt(),(5 * density).toInt(),(20 * density).toInt(),(5 * density).toInt())
                                if (isNightMode) placeButton.setBackgroundResource(R.drawable.rect_base_dark)
                                else placeButton.setBackgroundResource(R.drawable.rect_base)
                                placesListLayout.addView(placeButton)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PlacesListActivity, "Failed to load places.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}