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
import com.example.writersassistant.databinding.ActivityGroupInfoBinding
import com.example.writersassistant.databinding.ActivityPlaceInfoBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GroupInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupInfoBinding
    private lateinit var groupTitleET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var deleteGroupButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var bookId: String
    private var groupId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityGroupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        groupId = intent.getStringExtra("GROUP_ID") ?: database.child("books").child(bookId).push().key
        groupTitleET = findViewById(R.id.groupTitleEditText)
        descriptionET = findViewById(R.id.groupDescriptionEditText)
        deleteGroupButton = findViewById(R.id.deleteGroupButton)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            descriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            deleteGroupButton.setBackgroundResource(R.drawable.rect_base_dark)
        }
        if (groupId != null) loadGroupData(bookId, groupId!!)
        deleteGroupButton.setOnClickListener { deleteGroup() }
        setupTextChangeListeners()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@GroupInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@GroupInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@GroupInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadGroupData(bookId: String, groupId: String) {
        database.child("books").child(bookId).child("groups").child(groupId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                groupTitleET.setText(snapshot.child("groupTitle").value.toString())
                descriptionET.setText(snapshot.child("description").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        groupTitleET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveGroupData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveGroupData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun saveGroupData() {
        val groupTitleText: String = groupTitleET.text.toString()

        if(groupTitleText.isNullOrEmpty()) return

        val groupData = mapOf(
            "groupTitle" to groupTitleText,
            "description" to descriptionET.text.toString()
        )
        groupId?.let {
            database.child("books").child(bookId).child("groups").child(it).updateChildren(groupData)
        }
    }

    private fun deleteGroup() {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingGroupText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                groupId?.let {
                    database.child("books").child(bookId).child("groups").child(it).removeValue().addOnSuccessListener {
                        Toast.makeText(this, R.string.groupDeleted, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@GroupInfoActivity, GroupsListActivity::class.java)
                        intent.putExtra("BOOK_ID", bookId)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()}
                }
            }.setNegativeButton(R.string.answerNo, null).show()
    }
}