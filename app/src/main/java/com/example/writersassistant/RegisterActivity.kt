package com.example.writersassistant

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
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
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var login: String

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth
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
        val registrationButton: Button = findViewById(R.id.button)
        val emailTextInput: EditText = findViewById(R.id.editTextTextEmailAddress)
        val passwordInput: EditText = findViewById(R.id.editTextTextPassword)
        val confirmPassword: EditText = findViewById(R.id.editTextTextPasswordConfirm)
        val loginEditText: EditText = findViewById(R.id.editTextLogin)
        registerTextView.paint?.isUnderlineText = true
        if (isNightMode) Glide.with(this).load(R.drawable.white_cat_washing).into(imageCatRunning)
        else {
            Glide.with(this).load(R.drawable.black_cat_washing).into(imageCatRunning)
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        }

        registrationButton.setOnClickListener {
            email = emailTextInput.text.toString()
            password = passwordInput.text.toString()
            val connfirmatedPassword: String = confirmPassword.text.toString()
            login = loginEditText.text.toString()
            if (password != connfirmatedPassword) {
                Toast.makeText(this@RegisterActivity, getString(R.string.passwords_don_t_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@RegisterActivity, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password) or TextUtils.isEmpty(connfirmatedPassword)) {
                Toast.makeText(this@RegisterActivity, getString(R.string.enter_passwords), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(login)) {
                Toast.makeText(this@RegisterActivity, getString(R.string.enter_login), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(login).build()
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else Toast.makeText(this@RegisterActivity, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
                    }
                } else Toast.makeText(this@RegisterActivity,getString(R.string.authentication_failed),Toast.LENGTH_SHORT).show()
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}