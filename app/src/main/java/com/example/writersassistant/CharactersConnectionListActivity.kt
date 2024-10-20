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
import com.example.writersassistant.databinding.ActivityCharactersConnectionListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CharactersConnectionListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharactersConnectionListBinding
    private lateinit var addConnectionButton: ImageView
    private lateinit var bookId: String
    private lateinit var characterId: String
    private lateinit var database: DatabaseReference
    private lateinit var connectionsListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityCharactersConnectionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val mainLayout: ConstraintLayout = findViewById(R.id.main)

        addConnectionButton = findViewById(R.id.plusConnectionButton)
        bookId = intent.getStringExtra("BOOK_ID").toString()
        characterId = intent.getStringExtra("CHARACTER_ID").toString()
        connectionsListLayout = findViewById(R.id.connectionsListLayout)
        database = FirebaseDatabase.getInstance().reference.child("books")
        loadConnections()

        addConnectionButton.setOnClickListener {
            showCharacterSelectionDialog()
        }
        if (!isNightMode) {
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        } else {
            addConnectionButton.setImageResource(R.drawable.circle_plus_button_base_dark)
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.profilePage -> {
                    startActivity(Intent(this@CharactersConnectionListActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@CharactersConnectionListActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@CharactersConnectionListActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadConnections() {
        database.child(bookId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                connectionsListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (characterSnapshot in snapshot.child("characters").children) {
                    val charId = characterSnapshot.key ?: continue
                    if (charId == characterId) {
                        for (connectionSnapshot in characterSnapshot.child("connections").children) {
                            val connectionId = connectionSnapshot.key ?: continue
                            val secondCharacterId = connectionSnapshot.child("secondCharacterId").value?.toString()

                            if (secondCharacterId != null) {
                                val secondCharacterName = snapshot.child("characters").child(secondCharacterId).child("characterName").value?.toString()

                                if (secondCharacterName != null) {
                                    val connectionButton = Button(this@CharactersConnectionListActivity).apply {
                                        text = secondCharacterName
                                        layoutParams = LinearLayout.LayoutParams((350 * density).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                            setMargins(0, (10 * density).toInt(), 0, (10 * density).toInt())
                                            gravity = Gravity.CENTER_HORIZONTAL
                                        }
                                        setTextAppearance(R.style.ButtonTextStyle)
                                        setOnClickListener {
                                            openConnectionInfoActivity(bookId, characterId, connectionId)
                                        }
                                    }
                                    connectionButton.setPadding((20 * density).toInt(), (5 * density).toInt(), (20 * density).toInt(), (5 * density).toInt())
                                    if (isNightMode) connectionButton.setBackgroundResource(R.drawable.rect_base_dark)
                                    else connectionButton.setBackgroundResource(R.drawable.rect_base)
                                    connectionButton.setOnLongClickListener {
                                        showDeleteConnectionDialog(secondCharacterId, connectionId)
                                        true
                                    }
                                    connectionsListLayout.addView(connectionButton)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CharactersConnectionListActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openConnectionInfoActivity(bookId: String, mainCharacterId: String, connectionId: String) {
        val intent = Intent(this@CharactersConnectionListActivity, CharacterConnectionInfoActivity::class.java)
        intent.putExtra("BOOK_ID", bookId)
        intent.putExtra("MAIN_CHARACTER_ID", mainCharacterId)
        intent.putExtra("CONNECTION_ID", connectionId)
        startActivity(intent)
    }

    private fun showCharacterSelectionDialog() {
        database.child(bookId).child("characters").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val charactersList = mutableListOf<String>()
                val characterIds = mutableListOf<String>()
                val connectedCharacters = mutableSetOf<String>()

                database.child(bookId).child("characters").child(characterId).child("connections")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(connectionsSnapshot: DataSnapshot) {
                            for (connectionSnapshot in connectionsSnapshot.children) {
                                connectedCharacters.add(connectionSnapshot.child("secondCharacterId").value.toString())
                            }
                            for (characterSnapshot in snapshot.children) {
                                val charId = characterSnapshot.key ?: continue
                                val characterName = characterSnapshot.child("characterName").value.toString()

                                if (charId != characterId && !connectedCharacters.contains(charId)) {
                                    charactersList.add(characterName)
                                    characterIds.add(charId)
                                }
                            }

                            MaterialAlertDialogBuilder(this@CharactersConnectionListActivity)
                                .setTitle(R.string.charactersButton).setItems(charactersList.toTypedArray()) { _, which ->
                                    val selectedCharacterId = characterIds[which]
                                    openCreateConnectionActivity(bookId, characterId, selectedCharacterId)
                                }.show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@CharactersConnectionListActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CharactersConnectionListActivity, R.string.loadedError, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openCreateConnectionActivity(bookId: String, mainCharacterId: String, selectedCharacterId: String) {
        val intent = Intent(this@CharactersConnectionListActivity, CharacterConnectionInfoActivity::class.java)
        intent.putExtra("BOOK_ID", bookId)
        intent.putExtra("MAIN_CHARACTER_ID", mainCharacterId)
        intent.putExtra("SELECTED_CHARACTER_ID", selectedCharacterId)
        startActivity(intent)
    }

    private fun showDeleteConnectionDialog(secondCharacterId: String, connectionId: String) {
        MaterialAlertDialogBuilder(this@CharactersConnectionListActivity)
            .setTitle(R.string.deletionConfirmation).setMessage(R.string.deleteConnection).setPositiveButton(R.string.answerYes) { _, _ ->
                deleteConnection(secondCharacterId, connectionId) }.setNegativeButton(R.string.answerNo, null).show()
    }

    private fun deleteConnection(secondCharacterId: String, connectionId: String) {
        val firstCharacterPath = database.child(bookId).child("characters").child(characterId).child("connections").child(connectionId)
        val secondCharacterPath = database.child(bookId).child("characters").child(secondCharacterId).child("connections").child(connectionId)

        var deleteCount = 0

        firstCharacterPath.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                logDeleteStatus("Связь у персонажа $characterId удалена")
                deleteCount++
            } else {
                logDeleteStatus("Ошибка при удалении связи у персонажа $characterId: ${task.exception?.message}")
            }

            secondCharacterPath.removeValue().addOnCompleteListener { secondTask ->
                if (secondTask.isSuccessful) {
                    logDeleteStatus("Связь у персонажа $secondCharacterId удалена")
                    deleteCount++
                } else {
                    logDeleteStatus("Ошибка при удалении связи у персонажа $secondCharacterId: ${secondTask.exception?.message}")
                }

                checkAndReload(deleteCount)
            }
        }
    }

    private fun logDeleteStatus(message: String) {
        android.util.Log.e("DeleteConnection", message)
    }

    private fun checkAndReload(deleteCount: Int) {
        if (deleteCount >= 2) {
            loadConnections()
            Toast.makeText(this@CharactersConnectionListActivity, R.string.deletedSuccessfully, Toast.LENGTH_SHORT).show()
        }
    }
}
