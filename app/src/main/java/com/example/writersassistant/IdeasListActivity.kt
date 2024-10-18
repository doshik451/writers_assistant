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
import com.example.writersassistant.databinding.ActivityChaptersListBinding
import com.example.writersassistant.databinding.ActivityIdeasListBinding
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IdeasListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdeasListBinding
    private lateinit var addIdeaButton: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var ideasListLayout: LinearLayout
    private var isNightMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityIdeasListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        addIdeaButton = findViewById(R.id.plusIdeaButton)
        database = FirebaseDatabase.getInstance().reference.child("ideas")
        ideasListLayout = findViewById(R.id.ideasListLayout)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else addIdeaButton.setImageResource(R.drawable.circle_plus_button_base_dark)
        loadIdeas()
        addIdeaButton.setOnClickListener { startActivity(Intent(this@IdeasListActivity, IdeaInfoActivity::class.java)) }

        binding.bottomNavigationView.selectedItemId = R.id.ideasPage
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@IdeasListActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@IdeasListActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> true
                else -> false
            }
            true
        }
    }

    private fun loadIdeas() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ideasListLayout.removeAllViews()
                val density = resources.displayMetrics.density

                for (ideaSnapshot in snapshot.children) {
                    val ideaId = ideaSnapshot.key ?: continue
                    val authorId = ideaSnapshot.child("authorId").value.toString()
                    if (authorId == currentUserId) {
                        val ideaTitle = ideaSnapshot.child("title").value.toString()
                        val ideaButton = Button(this@IdeasListActivity).apply {
                            text = ideaTitle
                            layoutParams = LinearLayout.LayoutParams(
                                (350 * density).toInt(),
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, (10 * density).toInt(), 0, (10 * density).toInt())
                                gravity = Gravity.CENTER_HORIZONTAL
                            }
                            setTextAppearance(R.style.ButtonTextStyle)
                            setOnClickListener {
                                val intent = Intent(this@IdeasListActivity, IdeaInfoActivity::class.java)
                                intent.putExtra("IDEA_ID", ideaId)
                                startActivity(intent)
                            }
                        }
                        ideaButton.setPadding((20 * density).toInt(), (5 * density).toInt(), (20 * density).toInt(), (5 * density).toInt())
                        if(isNightMode) ideaButton.setBackgroundResource(R.drawable.rect_base_dark)
                        else ideaButton.setBackgroundResource(R.drawable.rect_base)
                        ideasListLayout.addView(ideaButton)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@IdeasListActivity, "Failed to load ideas.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}