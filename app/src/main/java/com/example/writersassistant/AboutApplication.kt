package com.example.writersassistant

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.databinding.ActivityAboutApplicationBinding
import com.example.writersassistant.databinding.ActivityProfileBinding
import com.example.writersassistant.utils.LoadSettings

class AboutApplication : AppCompatActivity() {
    private lateinit var binding: ActivityAboutApplicationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityAboutApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        binding.bottomNavigationView.selectedItemId = R.id.profilePage
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.mainPage -> {
                    startActivity(Intent(this@AboutApplication, MainActivity::class.java))
                    finish()
                }
                R.id.profilePage -> {
                    startActivity(Intent(this@AboutApplication, ProfileActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@AboutApplication, IdeasListActivity::class.java))
                    finish()
                }
                else -> {startActivity(Intent(this@AboutApplication, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }
}