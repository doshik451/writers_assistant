package com.example.writersassistant

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainLayout = findViewById(R.id.main)
        val imageCatRunning: ImageView = findViewById(R.id.CatImageView)
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val registerTextView: TextView = findViewById(R.id.tvRegister)
        val loginButton: Button = findViewById(R.id.LoginButton)
        val emailTextInput: EditText = findViewById(R.id.editTextTextEmailAddress)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword)

        registerTextView.paint?.isUnderlineText = true
        if (isNightMode)Glide.with(this).load(R.drawable.white_cat_running).into(imageCatRunning)
        else {
            Glide.with(this).load(R.drawable.dark_cat_running).into(imageCatRunning)
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        }

        loginButton.setOnClickListener {
            email = emailTextInput.text.toString()
            password = passwordEditText.text.toString()

            if(TextUtils.isEmpty(email)) {
                Toast.makeText(this@LoginActivity, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(password)) {
                Toast.makeText(this@LoginActivity, getString(R.string.enter_passwords), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.authentication_failed),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}