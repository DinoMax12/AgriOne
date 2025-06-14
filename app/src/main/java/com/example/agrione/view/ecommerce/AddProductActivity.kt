package com.example.agrione.view.ecommerce

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.databinding.ActivityAddProductBinding
import com.example.agrione.model.data.EcommItem
import com.example.agrione.viewmodel.EcommViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import androidx.appcompat.widget.Toolbar

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var viewModel: EcommViewModel
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val productTypes = arrayOf("Fertilizer", "Pesticide", "Machine", "Seed", "Agrione", "Other")
    private val availabilityOptions = arrayOf("Yes", "No")

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("mytag", "Image selected: $uri")
                selectedImageUri = uri
                binding.productImage.setImageURI(uri)
                binding.productImageError.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set up back button click listener
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel = ViewModelProvider(this)[EcommViewModel::class.java]

        setupUI()
        setupSpinners()
        setupClickListeners()
    }

    private fun setupUI() {
        // Show content and hide progress bar initially
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.VISIBLE
    }

    private fun setupSpinners() {
        // Setup Product Type spinner
        val productTypeAdapter = ArrayAdapter(this, R.layout.item_dropdown, productTypes)
        (binding.productTypeInput as? AutoCompleteTextView)?.setAdapter(productTypeAdapter)

        // Setup Availability spinner
        val availabilityAdapter = ArrayAdapter(this, R.layout.item_dropdown, availabilityOptions)
        (binding.availabilityInput as? AutoCompleteTextView)?.setAdapter(availabilityAdapter)
    }

    private fun setupClickListeners() {
        binding.productImage.setOnClickListener {
            Log.d("mytag", "Opening image picker")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        binding.addProductButton.setOnClickListener {
            Log.d("mytag", "Add product button clicked")
            if (validateInputs()) {
                uploadProduct()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (selectedImageUri == null) {
            Log.d("mytag", "No image selected")
            binding.productImageError.visibility = View.VISIBLE
            isValid = false
        }

        with(binding) {
            if (productTitleInput.text.isNullOrEmpty()) {
                productTitleInput.error = "Title is required"
                isValid = false
            }
            if (productPriceInput.text.isNullOrEmpty()) {
                productPriceInput.error = "Price is required"
                isValid = false
            }
            if (shortDescInput.text.isNullOrEmpty()) {
                shortDescInput.error = "Short description is required"
                isValid = false
            }
            if (longDescInput.text.isNullOrEmpty()) {
                longDescInput.error = "Long description is required"
                isValid = false
            }
            if (howToUseInput.text.isNullOrEmpty()) {
                howToUseInput.error = "How to use is required"
                isValid = false
            }
            if (deliveryChargeInput.text.isNullOrEmpty()) {
                deliveryChargeInput.error = "Delivery charge is required"
                isValid = false
            }
            if (retailerInput.text.isNullOrEmpty()) {
                retailerInput.error = "Retailer name is required"
                isValid = false
            }
            if (productTypeInput.text.isNullOrEmpty()) {
                productTypeInput.error = "Product type is required"
                isValid = false
            }
            if (availabilityInput.text.isNullOrEmpty()) {
                availabilityInput.error = "Availability is required"
                isValid = false
            }
        }

        Log.d("mytag", "Input validation result: $isValid")
        return isValid
    }

    private fun uploadProduct() {
        Log.d("mytag", "Starting product upload")
        binding.progressBar.visibility = View.VISIBLE
        binding.addProductButton.isEnabled = false

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("mytag", "No current user found")
            showError("User not logged in")
            return
        }

        // Update the path to match the storage rules
        val imageRef = storage.reference.child("product_images/${UUID.randomUUID()}")
        Log.d("mytag", "Image reference path: ${imageRef.path}")

        selectedImageUri?.let { uri ->
            Log.d("mytag", "Starting image upload for URI: $uri")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    Log.d("mytag", "Image upload successful")
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        Log.d("mytag", "Got download URL: $downloadUri")
                        saveProductToFirestore(downloadUri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("mytag", "Failed to get download URL", e)
                        showError("Failed to get image URL: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("mytag", "Failed to upload image", e)
                    showError("Failed to upload image: ${e.message}")
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    Log.d("mytag", "Upload progress: $progress%")
                }
        }
    }

    private fun saveProductToFirestore(imageUrl: String) {
        Log.d("mytag", "Starting to save product to Firestore")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("mytag", "No current user found for Firestore save")
            showError("User not logged in")
            return
        }

        with(binding) {
            val product = EcommItem(
                id = UUID.randomUUID().toString(),
                name = productTitleInput.text.toString(), // Using title as name
                price = productPriceInput.text.toString().toDoubleOrNull() ?: 0.0,
                description = longDescInput.text.toString(),
                imageUrl = imageUrl,
                sellerId = currentUser.uid,
                sellerName = currentUser.displayName ?: "Unknown Seller",
                title = productTitleInput.text.toString(),
                shortDesc = shortDescInput.text.toString(),
                longDesc = longDescInput.text.toString(),
                howtouse = howToUseInput.text.toString(),
                delCharge = deliveryChargeInput.text.toString(),
                rating = 0f,
                retailer = retailerInput.text.toString(),
                availability = availabilityInput.text.toString(),
                type = productTypeInput.text.toString(),
                attributes = mapOf()
            )

            Log.d("mytag", "Saving product with ID: ${product.id}")
            db.collection("products")
                .document(product.id)
                .set(product)
                .addOnSuccessListener {
                    Log.d("mytag", "Product saved successfully")
                    Toast.makeText(this@AddProductActivity, "Product added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("mytag", "Failed to save product", e)
                    showError("Failed to save product: ${e.message}")
                }
        }
    }

    private fun showError(message: String) {
        Log.e("mytag", "Error: $message")
        binding.progressBar.visibility = View.GONE
        binding.addProductButton.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 