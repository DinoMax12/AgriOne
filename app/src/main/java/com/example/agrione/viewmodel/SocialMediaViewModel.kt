package com.example.agrione.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SocialMediaViewModel : ViewModel() {

    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseFireStore: FirebaseFirestore? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    val postLiveData = MutableLiveData<List<DocumentSnapshot>>()

    fun loadPosts() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFireStore = FirebaseFirestore.getInstance()

        firebaseFireStore!!.collection("posts").get()
            .addOnSuccessListener { documents ->
                if (documents != null && documents.documents.isNotEmpty()) {
                    postLiveData.value = documents.documents
                    // You can access document data like this: documents[0].data["field_name"]
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure case
                Log.e("SocialMediaViewModel", "Error loading posts: ${exception.message}")
            }
    }
}
