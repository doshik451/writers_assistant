package com.example.writersassistant

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.writersassistant.utils.LoadSettings

class RegisterActivity : AppCompatActivity() {
    private lateinit var mainLayout: ConstraintLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainLayout = findViewById(R.id.main)
        val imageCatRunning: ImageView = findViewById(R.id.CatImageView)
        val isNightMode = LoadSettings.loadTheme(this)
        val registerTextView: TextView = findViewById(R.id.tvLogin)
        registerTextView.paint?.isUnderlineText = true
        if (isNightMode) Glide.with(this).load(R.drawable.white_cat_washing).into(imageCatRunning)
        else {
            Glide.with(this).load(R.drawable.black_cat_washing).into(imageCatRunning)
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}