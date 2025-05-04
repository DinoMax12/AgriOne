package com.example.agrione.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserDataViewModel : ViewModel() {

    val userliveData = MutableLiveData<DocumentSnapshot>()

    fun getUserData(userId: String) {
        val firebaseFireStore = FirebaseFirestore.getInstance()

        firebaseFireStore.collection("users").document(userId)
            .get()
            .addOnCompleteListener {
                userliveData.value = it.result
            }
    }

    suspend fun updateUserField(context: Context, userID: String, about: String?, city: String?) {
        withContext(Dispatchers.IO) {
            val firebaseFireStore = FirebaseFirestore.getInstance()

            about?.let {
                firebaseFireStore.collection("users").document(userID)
                    .update(mapOf("about" to it))
                    .addOnSuccessListener {
                        Log.d("UserDataViewModel", "User About Data Updated")
                        getUserData(userID)
                    }
                    .addOnFailureListener {
                        Log.d("UserDataViewModel", "Failed to Update About User Data")
                        showToast(context, "Failed to Update About. Try Again!")
                    }
            }

            city?.let {
                firebaseFireStore.collection("users").document(userID)
                    .update(mapOf("city" to it))
                    .addOnSuccessListener {
                        Log.d("UserDataViewModel", "User City Data Updated")
                        getUserData(userID)
                    }
                    .addOnFailureListener {
                        Log.d("UserDataViewModel", "Failed to Update City User Data")
                        showToast(context, "Failed to Update City. Try Again!")
                    }
            }
            showToast(context, "Profile Updated")
        }
    }

    suspend fun deleteUserPost(userId: String, postId: String) {
        withContext(Dispatchers.IO) {
            val firebaseFirestore = FirebaseFirestore.getInstance()

            firebaseFirestore.collection("posts").document(postId)
                .delete()
                .addOnSuccessListener {
                    Log.d("UserDataViewModel", "Post Deleted")
                    UserProfilePostsViewModel().getAllPosts(userId)
                    firebaseFirestore.collection("users").document(userId)
                        .update("posts", FieldValue.arrayRemove(postId))
                        .addOnSuccessListener {
                            Log.d("UserDataViewModel", "Successfully Deleted User Doc Post")
                            getUserData(userId)
                        }
                        .addOnFailureListener {
                            Log.e("UserDataViewModel", "Failed to delete post from User Doc")
                        }
                }
                .addOnFailureListener {
                    Log.d("UserDataViewModel", "Failed to delete post")
                }
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
