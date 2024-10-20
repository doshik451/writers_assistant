package com.example.writersassistant

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.indices
import androidx.viewpager2.widget.ViewPager2
import com.example.writersassistant.adapters.ImageAdapter
import com.example.writersassistant.databinding.ActivityCharacterReferencesBinding
import com.example.writersassistant.models.ImageItem
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CharacterReferencesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCharacterReferencesBinding
    private lateinit var addAppearanceReferenceButton: ImageView
    private lateinit var appearanceImagesContainer: ViewPager2
    private lateinit var appearanceSlideDots: LinearLayout
    private lateinit var addClothReferenceButton: ImageView
    private lateinit var clothImagesContainer: ViewPager2
    private lateinit var clothSlideDots: LinearLayout
    private lateinit var addAtmosphereReferenceButton: ImageView
    private lateinit var atmosphereImagesContainer: ViewPager2
    private lateinit var atmosphereSlideDots: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var currentCategory: String
    private lateinit var bookId: String
    private lateinit var characterId: String
    private val PICK_IMAGES_REQUEST = 1
    private val MAX_IMAGES = 7

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val clipData = data.clipData
            val uris = mutableListOf<Uri>()
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    if (uris.size < MAX_IMAGES) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                }
            } else {
                data.data?.let { uris.add(it) }
            }

            when (currentCategory) {
                "appearance" -> uploadImagesToFirebaseStorage(uris, "appearance")
                "clothing" -> uploadImagesToFirebaseStorage(uris, "clothing")
                "atmosphere" -> uploadImagesToFirebaseStorage(uris, "atmosphere")
            }
        }
    }

    private fun uploadImagesToFirebaseStorage(uris: List<Uri>, category: String) {
        val storageRef = storage.reference
        val bookId = bookId ?: return
        val characterId = characterId ?: return

        database.child("characters_references").child(bookId).child(characterId).child(category).get().addOnSuccessListener { snapshot ->
            val currentImageCount = snapshot.childrenCount
            val availableSlots = MAX_IMAGES - currentImageCount

            if (availableSlots <= 0) {
                Toast.makeText(this, R.string.youCanOnly + MAX_IMAGES + R.string.images, Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val imagesToUpload = uris.take(availableSlots.toInt())

            imagesToUpload.forEach { uri ->
                val imageRef = storageRef.child("characters_references/$bookId/$characterId/$category/${UUID.randomUUID()}.jpg")
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val imageId = database.child("characters_references").child(bookId).child(characterId).child(category).push().key
                            imageId?.let {
                                database.child("characters_references").child(bookId).child(characterId).child(category).child(it)
                                    .setValue(downloadUri.toString()).addOnCompleteListener {
                                        loadImagesIntoSlider(category)
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadImagesIntoSlider(category: String) {
        val bookId = bookId ?: return
        val characterId = characterId ?: return
        database.child("characters_references").child(bookId).child(characterId).child(category).get().addOnSuccessListener { snapshot ->
            val images = mutableListOf<ImageItem>()
            for (child in snapshot.children) {
                val imageUrl = child.getValue(String::class.java)
                val imageId = child.key
                if (imageUrl != null && imageId != null) {
                    images.add(ImageItem(imageId, imageUrl))
                }
            }

            val adapter = ImageAdapter { imageItem ->
                showDeleteImageDialog(imageItem, category)
            }

            adapter.submitList(images)

            when (category) {
                "appearance" -> {
                    appearanceImagesContainer.adapter = adapter
                    setupSliderDots(images.size, appearanceSlideDots)
                }
                "clothing" -> {
                    clothImagesContainer.adapter = adapter
                    setupSliderDots(images.size, clothSlideDots)
                }
                "atmosphere" -> {
                    atmosphereImagesContainer.adapter = adapter
                    setupSliderDots(images.size, atmosphereSlideDots)
                }
            }
        }
    }

    private fun showDeleteImageDialog(imageItem: ImageItem, category: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingImageText)
            .setPositiveButton(R.string.answerYes) { dialog, _ ->
                val bookId = bookId ?: return@setPositiveButton
                val characterId = characterId ?: return@setPositiveButton
                database.child("characters_references").child(bookId).child(characterId).child(category).child(imageItem.id).removeValue().addOnCompleteListener {
                    loadImagesIntoSlider(category)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.answerNo) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupSliderDots(count: Int, slideDots: LinearLayout) {
        slideDots.removeAllViews()
        val dots = arrayOfNulls<ImageView>(count)
        for (i in dots.indices) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this, R.drawable.non_active_dot
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            slideDots.addView(dots[i], params)
        }

        if (dots.isNotEmpty()) {
            dots[0]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.active_dot
                )
            )
        }

        appearanceImagesContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@CharacterReferencesActivity,
                            if (i == position) R.drawable.active_dot else R.drawable.non_active_dot
                        )
                    )
                }
            }
        })

        clothImagesContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@CharacterReferencesActivity,
                            if (i == position) R.drawable.active_dot else R.drawable.non_active_dot
                        )
                    )
                }
            }
        })

        atmosphereImagesContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@CharacterReferencesActivity,
                            if (i == position) R.drawable.active_dot else R.drawable.non_active_dot
                        )
                    )
                }
            }
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityCharacterReferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        characterId = intent.getStringExtra("CHARACTER_ID").toString()

        addAppearanceReferenceButton = findViewById(R.id.plusAppearanceReferenceButton)
        addClothReferenceButton = findViewById(R.id.plusClothReferenceButton)
        addAtmosphereReferenceButton = findViewById(R.id.plusAtmosphereReferenceButton)

        appearanceImagesContainer = findViewById(R.id.appearanceImagesSliderContainer)
        clothImagesContainer = findViewById(R.id.clothImagesSliderContainer)
        atmosphereImagesContainer = findViewById(R.id.atmosphereImagesSliderContainer)

        appearanceSlideDots = findViewById(R.id.appearanceSlidersDot)
        clothSlideDots = findViewById(R.id.clothSlidersDot)
        atmosphereSlideDots = findViewById(R.id.atmosphereSlidersDot)

        loadImagesIntoSlider("appearance")
        loadImagesIntoSlider("clothing")
        loadImagesIntoSlider("atmosphere")

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            addAppearanceReferenceButton.setImageResource(R.drawable.circle_plus_button_base_dark)
            addClothReferenceButton.setImageResource(R.drawable.circle_plus_button_base_dark)
            addAtmosphereReferenceButton.setImageResource(R.drawable.circle_plus_button_base_dark)
        }


        addAppearanceReferenceButton.setOnClickListener {
            currentCategory = "appearance"
            openImagePicker()
        }

        addClothReferenceButton.setOnClickListener {
            currentCategory = "clothing"
            openImagePicker()
        }

        addAtmosphereReferenceButton.setOnClickListener {
            currentCategory = "atmosphere"
            openImagePicker()
        }

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@CharacterReferencesActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@CharacterReferencesActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@CharacterReferencesActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }
}