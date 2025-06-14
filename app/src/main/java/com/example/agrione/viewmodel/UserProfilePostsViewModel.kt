package com.example.agrione.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.model.data.Post

class UserProfilePostsViewModel : ViewModel() {

    val userProfilePostsLiveData = MutableLiveData<ArrayList<HashMap<String, Any>>>()
    val userProfilePostsLiveData2 = MutableLiveData<List<String>?>()

    val liveData1 = MutableLiveData<List<String>>()
    val liveData2 = MutableLiveData<ArrayList<DocumentSnapshot>>()
    val liveData3 = MutableLiveData<ArrayList<DocumentSnapshot>>()

    fun getUserPosts(userID: String?) {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseFirestore2 = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("users").document("$userID")
            .get()
            .addOnSuccessListener { document ->
                val allPostsIDs: List<String>? = document.get("posts") as List<String>
                userProfilePostsLiveData2.value = allPostsIDs
                Log.d("User All Posts 3", userProfilePostsLiveData2.value.toString())

                val localPostList = ArrayList<HashMap<String, Any>>()

                allPostsIDs?.forEach { postId ->
                    firebaseFirestore2.collection("posts").document(postId)
                        .get()
                        .addOnSuccessListener { post ->
                            Log.d("User All Posts", post.data.toString())
                            val postData = post.data as HashMap<String, Any>
                            localPostList.add(postData)

                            Log.d("User All Posts 4", localPostList.size.toString())
                            userProfilePostsLiveData.value = localPostList
                        }
                        .addOnFailureListener {
                            Log.d("User All Posts", "Some Failure Occurred while fetching user posts")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("User All Posts 2", "Some Failure Occurred while fetching user posts")
            }
    }

    fun getUserPostsIDs(userID: String?) {
        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("users").document(userID!!)
            .get()
            .addOnSuccessListener { document ->
                liveData1.value = document.get("posts") as List<String>
            }
    }

    fun getAllPostsOfUser(listOfIDs: List<String>) {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val someList = ArrayList<DocumentSnapshot>()
        val totalPosts = listOfIDs.size

        listOfIDs.forEach { postId ->
            firebaseFirestore.collection("posts").document(postId)
                .get()
                .addOnSuccessListener { post ->
                    someList.add(post)
                    Log.d("LiveData2 - 2", post.toString())
                }
        }

        if (someList.size == totalPosts) {
            liveData2.value = someList
            Log.d("LiveData2 - 1", liveData2.value.toString())
        }
    }

    fun getAllPosts(userId: String?) {
        val firebaseFirestore = FirebaseFirestore.getInstance()

        // First get the user document to get the post IDs
        firebaseFirestore.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { userDoc ->
                val postIds = userDoc.get("posts") as? List<String> ?: emptyList()
                Log.d("UserPrlPostsViewModel", "Found ${postIds.size} posts for user")
                
                if (postIds.isEmpty()) {
                    liveData3.value = ArrayList()
                    return@addOnSuccessListener
                }

                // Then get all the posts using the IDs
                val postsList = ArrayList<DocumentSnapshot>()
                var completedCount = 0

                postIds.forEach { postId ->
                    firebaseFirestore.collection("posts").document(postId)
                        .get()
                        .addOnSuccessListener { postDoc ->
                            postsList.add(postDoc)
                            completedCount++
                            
                            // Only update when all posts are fetched
                            if (completedCount == postIds.size) {
                                liveData3.value = postsList
                                Log.d("UserPrlPostsViewModel", "Updated data with ${postsList.size} posts")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserPrlPostsViewModel", "Error fetching post $postId: ${e.message}")
                            completedCount++
                            
                            // Still update when all requests are complete, even if some failed
                            if (completedCount == postIds.size) {
                                liveData3.value = postsList
                                Log.d("UserPrlPostsViewModel", "Updated data with ${postsList.size} posts (some failed)")
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserPrlPostsViewModel", "Error fetching user document: ${e.message}")
                liveData3.value = ArrayList()
            }
    }
}
