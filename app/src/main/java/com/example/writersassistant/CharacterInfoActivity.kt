package com.example.writersassistant

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class CharacterInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharacterInfoBinding
    private var characterId: String? = null
    private lateinit var characterName: EditText
    private lateinit var characterAge: EditText
    private lateinit var characterImage: CircleImageView
    private lateinit var roleInTheBook: EditText
    private lateinit var connectionButton: Button
    private lateinit var aboutCharacterButton: Button
    private lateinit var storage: FirebaseStorage
    private lateinit var deleteCharacterButton: Button
    private lateinit var characterReferencesButton: Button
    private lateinit var storageReference: StorageReference
    private lateinit var database: DatabaseReference
    private var imageUri: Uri? = null
    private lateinit var bookId: String

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
        val filePath = storageReference.child("characterMainImage/${bookId}/${characterId}.jpg")

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
        val filePath = storageReference.child("characterMainImage/${bookId}/${characterId}.jpg")
        filePath.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(characterImage)
        }.addOnFailureListener {
            characterImage.setImageResource(R.drawable.ic_profile_page_icon)
        }
    }

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
        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        characterId = intent.getStringExtra("CHARACTER_ID") ?: database.child("books").child(bookId).push().key
        characterImage = findViewById(R.id.characterImage)
        roleInTheBook = findViewById(R.id.roleInTheBookEditText)
        characterName = findViewById(R.id.editTextCharacterName)
        characterAge = findViewById(R.id.editTextCharacterAge)
        connectionButton = findViewById(R.id.connectionButton)
        aboutCharacterButton = findViewById(R.id.aboutCharacterButton)
        characterReferencesButton = findViewById(R.id.referencesButton)
        storageReference = FirebaseStorage.getInstance().reference
        deleteCharacterButton = findViewById(R.id.deleteCharacterButton)
        storage = FirebaseStorage.getInstance()
        characterImage.setOnClickListener {
            pickImage.launch(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }
        loadUserImage()
        if (characterId != null) loadCharacterData(bookId!!, characterId!!)
        setupTextChangeListeners()
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else{
            roleInTheBook.setBackgroundResource(R.drawable.rect_base_dark)
            connectionButton.setBackgroundResource(R.drawable.rect_base_dark)
            deleteCharacterButton.setBackgroundResource(R.drawable.rect_base_dark)
            aboutCharacterButton.setBackgroundResource(R.drawable.rect_base_dark)
            characterReferencesButton.setBackgroundResource(R.drawable.rect_base_dark)
        }

        deleteCharacterButton.setOnClickListener { deleteCharacter(bookId) }

        aboutCharacterButton.setOnClickListener {
            val intent = Intent(this@CharacterInfoActivity, CharacterDescriptionActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            intent.putExtra("CHARACTER_ID", characterId)
            startActivity(intent)
        }

        characterReferencesButton.setOnClickListener {
            val intent = Intent(this@CharacterInfoActivity, CharacterReferencesActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            intent.putExtra("CHARACTER_ID", characterId)
            startActivity(intent)
        }

        connectionButton.setOnClickListener {
            val intent = Intent(this@CharacterInfoActivity, CharactersConnectionListActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            intent.putExtra("CHARACTER_ID", characterId)
            startActivity(intent)
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
                R.id.searchPage -> {
                    startActivity(Intent(this@CharacterInfoActivity, SearchingActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@CharacterInfoActivity, IdeasListActivity::class.java))
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
                characterName.setText(snapshot.child("characterName").value.toString())
                characterAge.setText(snapshot.child("characterAge").value.toString())
                roleInTheBook.setText(snapshot.child("roleInTheBook").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        characterName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveBookData(bookId.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        characterAge.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveBookData(bookId.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        roleInTheBook.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveBookData(bookId.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveBookData(bookId: String) {
        val characterNameText: String = characterName.text.toString()

        if(characterNameText.isNullOrEmpty()) return

        val characterData = mapOf(
            "characterName" to characterNameText,
            "characterAge" to characterAge.text.toString(),
            "roleInTheBook" to roleInTheBook.text.toString()
        )
        characterId?.let {
            database.child("books").child(bookId).child("characters").child(it).updateChildren(characterData)
        }
    }

    private fun deleteCharacter(bookId: String) {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingCharacterText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                characterId?.let {
                    database.child("books").child(bookId).child("characters").child(it).removeValue()
                        .addOnSuccessListener {
                            val storageRef = storage.reference.child("character_references/$bookId/$characterId")
                            storageRef.listAll().addOnSuccessListener { listResult ->
                                listResult.items.forEach { fileRef ->
                                    fileRef.delete().addOnSuccessListener {
                                    }.addOnFailureListener {
                                        Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
                            }

                            val mainImageRef = storage.reference.child("characterMainImage/$bookId/$characterId.jpg")
                            mainImageRef.delete().addOnSuccessListener {
                            }.addOnFailureListener {
                                Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
                            }
                            removeConnectionsForDeletedCharacter(bookId, characterId!!)

                            Toast.makeText(this, R.string.characterDeleted, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@CharacterInfoActivity, CharactersListActivity::class.java)
                            intent.putExtra("BOOK_ID", bookId)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton(R.string.answerNo, null)
            .show()
    }

    private fun removeConnectionsForDeletedCharacter(bookId: String, deletedCharacterId: String) {
        val charactersRef = database.child("books").child(bookId).child("characters")

        charactersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (characterSnapshot in snapshot.children) {
                    val currentCharacterId = characterSnapshot.key
                    characterSnapshot.child("connections").children.forEach { connectionSnapshot ->
                        val secondCharacterId = connectionSnapshot.child("secondCharacterId").value?.toString()
                        if (secondCharacterId == deletedCharacterId) {
                            connectionSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    logDeleteStatus("Удалена связь с персонажем $deletedCharacterId у персонажа $currentCharacterId")
                                } else {
                                    logDeleteStatus("Ошибка при удалении связи у персонажа $currentCharacterId")
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CharacterInfoActivity, R.string.deletionError, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logDeleteStatus(message: String) {
        android.util.Log.e("DeleteConnection", message)
    }
}