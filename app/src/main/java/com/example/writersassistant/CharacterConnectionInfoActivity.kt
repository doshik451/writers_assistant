package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.writersassistant.databinding.ActivityCharacterConnectionInfoBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class CharacterConnectionInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharacterConnectionInfoBinding
    private lateinit var storageReference: StorageReference
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var mainCharacterIcon: CircleImageView
    private lateinit var secondCharacterIcon: CircleImageView
    private lateinit var firstToSecondConnection: EditText
    private lateinit var secondToFirstConnection: EditText
    private lateinit var mainCharacterName: TextView
    private lateinit var secondCharacterName: TextView
    private lateinit var bookId: String
    private lateinit var mainCharacterId: String
    private var secondCharacterId: String? = null
    private var connectionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityCharacterConnectionInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        mainCharacterId = intent.getStringExtra("MAIN_CHARACTER_ID").toString()
        mainCharacterIcon = findViewById(R.id.mainCharacterImage)
        secondCharacterIcon = findViewById(R.id.secondCharacterImage)
        firstToSecondConnection = findViewById(R.id.firstToSecondConnectionDescriptionEditText)
        secondToFirstConnection = findViewById(R.id.secondToFirstConnectionDescriptionEditText)
        mainCharacterName = findViewById(R.id.mainCharacterName)
        secondCharacterName = findViewById(R.id.secondCharacterName)
        secondCharacterId = intent.getStringExtra("SELECTED_CHARACTER_ID")
        connectionId = intent.getStringExtra("CONNECTION_ID")

        storageReference = FirebaseStorage.getInstance().reference
        storage = FirebaseStorage.getInstance()

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if (!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            firstToSecondConnection.setBackgroundResource(R.drawable.rect_base_dark)
            secondToFirstConnection.setBackgroundResource(R.drawable.rect_base_dark)
        }

        loadUserImage(mainCharacterIcon, mainCharacterId)

        if (connectionId != null) {
            loadConnectionData(connectionId!!)
        } else if (secondCharacterId != null) {
            loadUserImage(secondCharacterIcon, secondCharacterId!!)
        } else {
            Toast.makeText(this, R.string.loadedError, Toast.LENGTH_SHORT).show()
        }

        loadCharacterNamesFromCharacters()
        setupAutoSave()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@CharacterConnectionInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@CharacterConnectionInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@CharacterConnectionInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadUserImage(characterImage: CircleImageView, characterId: String) {
        val filePath = storageReference.child("characterMainImage/$bookId/$characterId.jpg")
        filePath.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(characterImage)
        }.addOnFailureListener {
            characterImage.setImageResource(R.drawable.ic_profile_page_icon)
        }
    }

    private fun loadConnectionData(connectionId: String) {
        val connectionRef = database.child("books").child(bookId)
            .child("characters").child(mainCharacterId)
            .child("connections").child(connectionId)

        connectionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                secondCharacterId = snapshot.child("secondCharacterId").value.toString()
                firstToSecondConnection.setText(snapshot.child("firstToSecond").value.toString())
                secondToFirstConnection.setText(snapshot.child("secondToFirst").value.toString())

                secondCharacterId?.let { loadUserImage(secondCharacterIcon, it) }

                loadCharacterNamesFromCharacters()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CharacterConnectionInfoActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCharacterNamesFromCharacters() {
        database.child("books").child(bookId).child("characters").child(mainCharacterId)
            .child("characterName").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mainCharacterName.text = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CharacterConnectionInfoActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
                }
            })

        secondCharacterId?.let {
            database.child("books").child(bookId).child("characters").child(it)
                .child("characterName").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        secondCharacterName.text = snapshot.value.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@CharacterConnectionInfoActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun saveConnectionData() {
        val firstToSecondText = firstToSecondConnection.text.toString()
        val secondToFirstText = secondToFirstConnection.text.toString()

        if (secondCharacterId.isNullOrEmpty()) {
            Toast.makeText(this, "Персонаж для связи не выбран", Toast.LENGTH_SHORT).show()
            return
        }

        if (connectionId.isNullOrEmpty()) {
            connectionId = database.child("books").child(bookId).child(mainCharacterId).child("connections").push().key
        }

        val connectionRef = database.child("books").child(bookId)
            .child("characters").child(mainCharacterId)
            .child("connections").child(connectionId!!)

        val connectionData = mapOf(
            "secondCharacterId" to secondCharacterId,
            "firstToSecond" to firstToSecondText,
            "secondToFirst" to secondToFirstText,
        )

        connectionRef.updateChildren(connectionData).addOnCompleteListener {
            if (it.isSuccessful) {
                saveSecondCharacterConnection(firstToSecondText, secondToFirstText)
            } else {
                Toast.makeText(this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveSecondCharacterConnection(firstToSecondText: String, secondToFirstText: String) {
        val mirroredConnectionRef = database.child("books").child(bookId)
            .child("characters").child(secondCharacterId!!)
            .child("connections").child(connectionId!!)

        val mirroredConnectionData = mapOf(
            "secondCharacterId" to mainCharacterId,
            "firstToSecond" to secondToFirstText,
            "secondToFirst" to firstToSecondText
        )

        mirroredConnectionRef.updateChildren(mirroredConnectionData).addOnCompleteListener {
            if (it.isSuccessful) {
            } else {
                Toast.makeText(this, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupAutoSave() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveConnectionData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        }

        firstToSecondConnection.addTextChangedListener(textWatcher)
        secondToFirstConnection.addTextChangedListener(textWatcher)
    }
}
