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
import com.example.writersassistant.databinding.ActivityGroupsListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupsListBinding
    private lateinit var addGroupButton: ImageView
    private lateinit var bookId: String
    private lateinit var database: DatabaseReference
    private lateinit var groupsListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGroupsListBinding.inflate(layoutInflater)
        isNightMode = LoadSettings.loadTheme(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addGroupButton = findViewById(R.id.plusGroupButton)
        bookId = intent.getStringExtra("BOOK_ID").toString()
        groupsListLayout = findViewById(R.id.groupsListLayout)
        database = FirebaseDatabase.getInstance().reference.child("books")
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addGroupButton.setImageResource(R.drawable.circle_plus_button_base_dark)

        loadGroups()

        addGroupButton.setOnClickListener {
            val intent = Intent(this@GroupsListActivity, GroupInfoActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@GroupsListActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@GroupsListActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@GroupsListActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadGroups() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val currentBook = intent.getStringExtra("BOOK_ID")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupsListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (bookSnapshot in snapshot.children) {
                    val bookId = bookSnapshot.key ?: continue
                    val authorId = bookSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        if(bookId == currentBook) {
                            for (groupSnapshot in bookSnapshot.child("groups").children) {
                                val groupId = groupSnapshot.key ?: continue
                                val groupName = groupSnapshot.child("groupTitle").value.toString()
                                val groupButton = Button(this@GroupsListActivity).apply {
                                    text = groupName
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
                                            this@GroupsListActivity,
                                            GroupInfoActivity::class.java
                                        )
                                        intent.putExtra("BOOK_ID", bookId)
                                        intent.putExtra("GROUP_ID", groupId)
                                        startActivity(intent)
                                    }
                                }
                                groupButton.setPadding((20 * density).toInt(),(5 * density).toInt(),(20 * density).toInt(),(5 * density).toInt())
                                if (isNightMode) groupButton.setBackgroundResource(R.drawable.rect_base_dark)
                                else groupButton.setBackgroundResource(R.drawable.rect_base)
                                groupsListLayout.addView(groupButton)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GroupsListActivity, "Failed to load groups.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}