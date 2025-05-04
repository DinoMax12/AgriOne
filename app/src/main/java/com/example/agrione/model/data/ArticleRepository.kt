package com.example.agrione.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.viewmodel.ArticleViewModel

class ArticleRepository(private val viewModel: ArticleViewModel) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val articleData = MutableLiveData<HashMap<String, Any>>()
    private val statusMessage = MutableLiveData<String>()

    fun getSpecificFruitArticle(name: String): LiveData<String> {

        firebaseDb.collection("article_fruits").document(name)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Updating ViewModel with the article data
                    articleData.value = documentSnapshot.data as HashMap<String, Any>
                    // Update status message on success
                    statusMessage.value = "Success"
                    // Optionally, you can also pass the article data to the ViewModel for further handling.
                    viewModel.updateArticle(documentSnapshot.data as HashMap<String, Any>)
                } else {
                    // Document doesn't exist
                    statusMessage.value = "Article not found"
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.e("ArticleRepo", "Error fetching article: ${exception.message}")
                statusMessage.value = "Failed to fetch article"
            }

        return statusMessage
    }

    // Return the LiveData for article data (if needed for UI display or other operations)
    fun getArticleData(): LiveData<HashMap<String, Any>> {
        return articleData
    }
}
