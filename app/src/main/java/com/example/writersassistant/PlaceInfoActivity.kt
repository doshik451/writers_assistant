package com.example.writersassistant

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.writersassistant.adapters.ImageAdapter
import com.example.writersassistant.databinding.ActivityPlaceInfoBinding
import com.example.writersassistant.models.ImageItem
import com.example.writersassistant.utils.LoadSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PlaceInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceInfoBinding
    private lateinit var placeTitleET: EditText
    private lateinit var descriptionET: EditText
    private lateinit var addReferenceButton: ImageView
    private lateinit var deletePlaceButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var imagesContainer: ViewPager2
    private lateinit var storage: FirebaseStorage
    private lateinit var slideDots: LinearLayout
    private lateinit var pageChangeListener: ViewPager2.OnPageChangeCallback
    private lateinit var bookId: String
    private var placeId: String? = null
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
            uploadImagesToFirebaseStorage(uris)
        }
    }

    private fun uploadImagesToFirebaseStorage(uris: List<Uri>) {
        val storageRef = storage.reference
        val bookId = bookId ?: return
        val placeId = placeId ?: return

        database.child("places_references").child(bookId).child(placeId).get().addOnSuccessListener { snapshot ->
            val currentImageCount = snapshot.childrenCount
            val availableSlots = MAX_IMAGES - currentImageCount

            if (availableSlots <= 0) {
                Toast.makeText(this, R.string.youCanOnly + MAX_IMAGES + R.string.images, Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val imagesToUpload = uris.take(availableSlots.toInt())

            imagesToUpload.forEach { uri ->
                val imageRef = storageRef.child("places_references/$bookId/$placeId/${UUID.randomUUID()}.jpg")
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val imageId = database.child("places_references").child(bookId).child(placeId).push().key
                            imageId?.let {
                                database.child("places_references").child(bookId).child(placeId).child(it)
                                    .setValue(downloadUri.toString()).addOnCompleteListener { loadImagesIntoSlider() }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadImagesIntoSlider() {
        val bookId = bookId ?: return
        val placeId = placeId ?: return
        database.child("places_references").child(bookId).child(placeId).get().addOnSuccessListener { snapshot ->
            val images = mutableListOf<ImageItem>()
            for (child in snapshot.children) {
                val imageUrl = child.getValue(String::class.java)
                val imageId = child.key
                if (imageUrl != null && imageId != null) {
                    images.add(ImageItem(imageId, imageUrl))
                }
            }

            val adapter = ImageAdapter { imageItem ->
                showDeleteImageDialog(imageItem)
            }

            adapter.submitList(images)
            imagesContainer.adapter = adapter
            setupSliderDots(images.size)
        }
    }

    private fun showDeleteImageDialog(imageItem: ImageItem) {
        AlertDialog.Builder(this)
            .setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingImageText)
            .setPositiveButton(R.string.answerYes) { dialog, _ ->
                val bookId = bookId ?: return@setPositiveButton
                val placeId = placeId ?: return@setPositiveButton
                database.child("places_references").child(bookId).child(placeId).child(imageItem.id).removeValue().addOnCompleteListener {
                    loadImagesIntoSlider()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.answerNo) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupSliderDots(count: Int) {
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

        imagesContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@PlaceInfoActivity,
                            if (i == position) R.drawable.active_dot else R.drawable.non_active_dot
                        )
                    )
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNightMode = LoadSettings.loadTheme(this)
        binding = ActivityPlaceInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bookId = intent.getStringExtra("BOOK_ID").toString()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        placeId = intent.getStringExtra("PLACE_ID") ?: database.child("books").child(bookId).push().key
        placeTitleET = findViewById(R.id.placeTitleEditText)
        descriptionET = findViewById(R.id.placeDescriptionEditText)
        deletePlaceButton = findViewById(R.id.deletePlaceButton)
        addReferenceButton = findViewById(R.id.plusReferenceButton)
        imagesContainer = findViewById(R.id.imagesSliderContainer)
        slideDots = findViewById(R.id.slidersDot)
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        if(!isNightMode) mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.LightBackground))
        else {
            descriptionET.setBackgroundResource(R.drawable.rect_base_dark)
            deletePlaceButton.setBackgroundResource(R.drawable.rect_base_dark)
            addReferenceButton.setImageResource(R.drawable.circle_plus_button_base_dark)
        }
        addReferenceButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select up to $MAX_IMAGES images"), PICK_IMAGES_REQUEST)
        }
        loadImagesIntoSlider()
        if (placeId != null) loadPlaceData(bookId, placeId!!)
        deletePlaceButton.setOnClickListener { deletePlace() }
        setupTextChangeListeners()

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.profilePage -> {
                    startActivity(Intent(this@PlaceInfoActivity, ProfileActivity::class.java))
                    finish()
                }
                R.id.mainPage -> {
                    startActivity(Intent(this@PlaceInfoActivity, MainActivity::class.java))
                    finish()
                }
                R.id.ideasPage -> {
                    startActivity(Intent(this@PlaceInfoActivity, IdeasListActivity::class.java))
                    finish()
                }
                else -> true
            }
            true
        }
    }

    private fun loadPlaceData(bookId: String, placeId: String) {
        database.child("books").child(bookId).child("places").child(placeId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                placeTitleET.setText(snapshot.child("placeTitle").value.toString())
                descriptionET.setText(snapshot.child("description").value.toString())
            }
        }
    }

    private fun setupTextChangeListeners() {
        placeTitleET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                savePlaceData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        descriptionET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                savePlaceData()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun savePlaceData() {
        val placeTitleText: String = placeTitleET.text.toString()

        if(placeTitleText.isNullOrEmpty()) return

        val placeData = mapOf(
            "placeTitle" to placeTitleText,
            "description" to descriptionET.text.toString()
        )
        placeId?.let {
            database.child("books").child(bookId).child("places").child(it).updateChildren(placeData)
        }
    }

    private fun deletePlace() {
        AlertDialog.Builder(this).setTitle(R.string.deletionConfirmation)
            .setMessage(R.string.deletingPlaceText)
            .setPositiveButton(R.string.answerYes) { dialog, which ->
                val currentPlaceId = placeId ?: return@setPositiveButton
                val currentBookId = bookId ?: return@setPositiveButton
                database.child("books").child(currentBookId).child("places").child(currentPlaceId).removeValue()
                    .addOnSuccessListener {
                        deleteFolderFromStorage(currentBookId, currentPlaceId)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(R.string.answerNo, null)
            .show()
    }

    private fun deleteFolderFromStorage(bookId: String, placeId: String) {
        val storageRef = storage.reference.child("places_references/$bookId/$placeId")
        storageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { fileRef ->
                fileRef.delete().addOnSuccessListener {
                }.addOnFailureListener {
                    Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
                }
            }

            Toast.makeText(this, R.string.placeDeleted, Toast.LENGTH_SHORT).show()
            val intent = Intent(this@PlaceInfoActivity, PlacesListActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, R.string.deletionError, Toast.LENGTH_SHORT).show()
        }
    }
}