package com.agrione.app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ArticleViewModel : ViewModel() {

    var message1 = MutableLiveData<HashMap<String, Any>>()
    var message2 = MutableLiveData<String>()
    var message3 = MutableLiveData<List<DocumentSnapshot>>()
    var articleListener: ArticleListener? = null

    private var todoLiveData: LiveData<HashMap<String, Any>>? = null

    private lateinit var firebaseDb: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    fun getArticle(): MutableLiveData<HashMap<String, Any>> {
        Log.d("ArticleViewModelGet", message1.value.toString())
        return message1
    }

    fun getMyArticle(name: String) {
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDb = FirebaseFirestore.getInstance()

        Log.d("ArticleRepo1", "Fetching article for $name")

        firebaseDb.collection("article_fruits").document(name)
            .get()
            .addOnSuccessListener {
                message1.value = it.data as HashMap<String, Any>?
                Log.d("ArticleViewModelDirect", message1.value.toString())
            }
            .addOnFailureListener {
                Log.d("ArticleRepo3", "Failed to fetch article")
            }
    }

    fun getAllArticles(name: String) {
        if (message3.value.isNullOrEmpty()) {
            firebaseDb = FirebaseFirestore.getInstance()
            firebaseDb.collection(name).get().addOnSuccessListener {
                message3.value = it.documents
                Log.d("ArticleViewModel", "All articles fetched successfully")
            }
        }
    }

    fun updateArticle(data: HashMap<String, Any>) {
        Log.d("ArticleViewModel", "Updating article with data: $data")
        message1.value = data
    }
}
