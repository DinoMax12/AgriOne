package com.example.agrione.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class YojnaViewModel: ViewModel() {

    lateinit var firebaseDb: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    var msg = MutableLiveData<HashMap<String, Any>>()
    var message3 = MutableLiveData<List<DocumentSnapshot>>()

    fun getYojna(name: String) {

        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDb = FirebaseFirestore.getInstance()

        firebaseDb.collection("yojnas").document(name)
            .get()
            .addOnSuccessListener {
                msg.value = it.data as HashMap<String, Any>?
                Log.d("YojnaViewModel", msg.value.toString())
            }
            .addOnFailureListener {
                Log.d("YojnaViewModel", "Error fetching Yojna")
            }
    }

    fun getAllYojna(name: String) {
        if (message3.value.isNullOrEmpty()) {
            firebaseDb = FirebaseFirestore.getInstance()
            firebaseDb.collection(name).get().addOnSuccessListener {
                message3.value = it.documents
                Log.d("YojnaViewModel", "Yojnas fetched successfully")
            }
        }
    }

    fun updateArticle(data: HashMap<String, Any>) {
        Log.d("YojnaViewModel", data.toString())
        msg.value = data
    }
}
