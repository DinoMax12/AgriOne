package com.example.agrione.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EcommViewModel : ViewModel() {

    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    val ecommLiveData = MutableLiveData<List<DocumentSnapshot>>()
    val specificCategoryItems = MutableLiveData<List<DocumentSnapshot>>()
    val specificItem = MutableLiveData<DocumentSnapshot>()

    fun loadAllEcommItems(): MutableLiveData<List<DocumentSnapshot>> {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFireStore = FirebaseFirestore.getInstance()

        firebaseFireStore.collection("products").get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    Log.d("EcommViewModel", it.documents[0].getString("title").toString())
                }
                ecommLiveData.value = it.documents
            }
            .addOnFailureListener { exception ->
                Log.e("EcommViewModel", "Error loading all items: ${exception.message}")
            }

        return ecommLiveData
    }

    fun loadSpecificTypeEcomItem(itemType: String) {
        firebaseFireStore = FirebaseFirestore.getInstance()

        firebaseFireStore.collection("products")
            .whereEqualTo("type", itemType)
            .get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    Log.d("EcommViewModel", it.documents[0].getString("title").toString())
                }
                ecommLiveData.value = it.documents
            }
            .addOnFailureListener {
                Log.e("EcommViewModel", "Error loading specific type: ${it.message}")
            }
    }

    fun getSpecificCategoryItems(category: String): MutableLiveData<List<DocumentSnapshot>> {
        Log.d("EcommViewModel", "Querying for type: ${category.lowercase()}")
        firebaseFireStore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val filteredDocs = documents.documents.filter { doc ->
                    val type = doc.getString("type")?.lowercase()
                    Log.d("EcommViewModel", "Document type: $type, comparing with: ${category.lowercase()}")
                    type == category.lowercase()
                }
                Log.d("EcommViewModel", "Found ${filteredDocs.size} matching documents")
                specificCategoryItems.value = filteredDocs
            }
            .addOnFailureListener { exception ->
                Log.e("EcommViewModel", "Error loading type items: ${exception.message}")
                specificCategoryItems.value = emptyList()
            }
        return specificCategoryItems
    }

    fun getSpecificItem(itemID: String): MutableLiveData<DocumentSnapshot> {
        firebaseFireStore.collection("products")
            .document(itemID)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    specificItem.value = it.result
                    Log.d("EcommViewModel", "Loaded Item: ${it.result?.data}")
                } else {
                    Log.e("EcommViewModel", "Failed to get specific item.")
                }
            }
            .addOnFailureListener {
                Log.e("EcommViewModel", "Error loading specific item: ${it.message}")
            }
        return specificItem
    }
}
