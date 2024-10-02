package com.example.writersassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.writersassistant.databinding.ActivityMainBinding
import com.example.writersassistant.utils.LoadSettings

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadSettings.applyLocale(this)
        LoadSettings.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        RegisterPermissionListener()
        CheckStoragePermission()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    finish()
                }
                else -> {startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                    finish()}
            }
            true
        }
    }

    private fun CheckStoragePermission(){
        when{
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> {}
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    private fun RegisterPermissionListener(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it) {Toast.makeText(this, R.string.thxxx, Toast.LENGTH_SHORT).show()}
            else {Toast.makeText(this, R.string.giveAsYourPhoto, Toast.LENGTH_LONG).show()}
        }
    }
}