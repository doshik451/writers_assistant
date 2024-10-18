package com.example.writersassistant

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityPlaceInfoBinding
import com.example.writersassistant.databinding.ActivityWordInfoBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PlaceInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceInfoBinding
    private lateinit var placeTitleET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var addReferenceButton: ImageView
    private lateinit var deletePlaceButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var bookId: String
    private var placeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityPlaceInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        placeId = intent.getStringExtra("PLACE_ID") ?: database.child("books").child(bookId).push().key
        placeTitleET = findViewById(R.id.placeTitleEditText)
        descriptionET = findViewById(R.id.placeDescriptionEditText)
        deletePlaceButton = findViewById(R.id.deletePlaceButton)
        addReferenceButton = findViewById(R.id.plusReferenceButton)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            descriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            deletePlaceButton.setBackgroundResource(R.drawable.rect_base_dark)
            addReferenceButton.setImageResource(R.drawable.circle_plus_button_base_dark)
        }
        if (placeId != null) loadPlaceData(bookId, placeId!!)
        deletePlaceButton.setOnClickListener { deletePlace() }
        setupTextChangeListeners()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@PlaceInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@PlaceInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@PlaceInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadPlaceData(bookId: String, placeId: String) {
        database.child("books").child(bookId).child("places").child(placeId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                placeTitleET.setText(snapshot.child("placeTitle").value.toString())
                descriptionET.setText(snapshot.child("description").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        placeTitleET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                savePlaceData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                savePlaceData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun savePlaceData() {
        val placeTitleText: String = placeTitleET.text.toString()

        if(placeTitleText.isNullOrEmpty()) return

        val placeData = mapOf(
            "placeTitle" to placeTitleText,
            "description" to descriptionET.text.toString()
        )
        placeId?.let {
            database.child("books").child(bookId).child("places").child(it).updateChildren(placeData)
        }
    }

    private fun deletePlace() {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingPlaceText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                placeId?.let {
                    database.child("books").child(bookId).child("places").child(it).removeValue().addOnSuccessListener {
                        Toast.makeText(this, R.string.placeDeleted, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PlaceInfoActivity, PlacesListActivity::class.java)
                        intent.putExtra("BOOK_ID", bookId)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()}
                }
            }.setNegativeButton(R.string.answerNo, null).show()
    }
}