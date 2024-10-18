package com.example.writersassistant

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityIdeaInfoBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class IdeaInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdeaInfoBinding
    private lateinit var ideaTitleET: EditText
    private lateinit var ideaDescriptionET: EditText
    private lateinit var deleteIdeaButton: Button
    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference
    private var ideaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIdeaInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        val isNightMode = LoadSettings.loadTheme(this)
        ideaTitleET = findViewById(R.id.ideaTitleEditText)
        ideaDescriptionET = findViewById(R.id.ideaDescriptionEditText)
        deleteIdeaButton = findViewById(R.id.deleteIdeaButton)
        user = FirebaseAuth.getInstance().currentUser!!
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            ideaDescriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            deleteIdeaButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        database = FirebaseDatabase.getInstance().reference
        ideaId = intent.getStringExtra("IDEA_ID") ?: database.child("ideas").push().key
        if (ideaId != null) loadIdeaData(ideaId!!)
        setupTextChangeListeners()
        deleteIdeaButton.setOnClickListener { deleteIdea() }

        binding.bottomNavigationView.selectedItemId = R.id.ideasPage
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@IdeaInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@IdeaInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@IdeaInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> false
            }
            true
        }
    }
    private fun loadIdeaData(ideaId: String) {
        database.child("ideas").child(ideaId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                ideaTitleET.setText(snapshot.child("title").value.toString())
                ideaDescriptionET.setText(snapshot.child("description").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        ideaTitleET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveIdeaData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ideaDescriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveIdeaData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveIdeaData() {
        val ideaTitle: String = ideaTitleET.text.toString()

        if(ideaTitle.isNullOrEmpty()) return

        val ideaData = mapOf(
            "title" to ideaTitle,
            "description" to ideaDescriptionET.text.toString(),
            "authorId" to user.uid
        )
        ideaId?.let {
            database.child("ideas").child(it).updateChildren(ideaData)
        }
    }

    private fun deleteIdea() {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingIdeaText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                ideaId?.let {
                    database.child("ideas").child(it).removeValue().addOnSuccessListener {
                        Toast.makeText(this, R.string.ideaDeleted, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@IdeaInfoActivity, IdeasListActivity::class.java))
                        finish()
                    }.addOnFailureListener { Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()}
                }
            }.setNegativeButton(R.string.answerNo, null).show()
    }
}