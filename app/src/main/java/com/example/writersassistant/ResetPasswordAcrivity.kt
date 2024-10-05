package com.example.writersassistant

import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordAcrivity : AppCompatActivity() {
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var emailToReset: EditText
    private lateinit var buttonTooSendLetter: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password_acrivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainLayout = findViewById(R.id.main)
        val isNightMode = LoadSettings.loadTheme(this)
        if (!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        emailToReset = findViewById(R.id.editTextTextEmailAddress)
        buttonTooSendLetter = findViewById(R.id.resetPasswordButton)
        auth = FirebaseAuth.getInstance()
        buttonTooSendLetter.setOnClickListener {
            val email = emailToReset.text.toString()
            if(TextUtils.isEmpty(email)) {
                Toast.makeText(this@ResetPasswordAcrivity, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                Toast.makeText(this@ResetPasswordAcrivity, R.string.letterIsSended, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this@ResetPasswordAcrivity, R.string.sentError, Toast.LENGTH_SHORT).show()
            }
        }
    }
}