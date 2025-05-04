package com.example.agrione.adapter

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.example.agrione.R
import com.google.firebase.firestore.FirebaseFirestore

class SMPostListAdapter(val context: Context, val postListData: List<DocumentSnapshot>) : RecyclerView.Adapter<SMPostListAdapter.SMPostListViewModel>() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore

    class SMPostListViewModel(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNamePost: TextView = itemView.findViewById(R.id.userNamePostSM)
        val userPostTitleValue: TextView = itemView.findViewById(R.id.userPostTitleValue)
        val userPostDescValue: TextView = itemView.findViewById(R.id.userPostDescValue)
        val userPostUploadTime: TextView = itemView.findViewById(R.id.userPostUploadTime)
        val postVideo: WebView = itemView.findViewById(R.id.postVideoSM)
        val postImage: ImageView = itemView.findViewById(R.id.postImageSM)
        val userProfileImageCard: CardView = itemView.findViewById(R.id.userProfileImageCard)
        val postContainer: ConstraintLayout = itemView.findViewById(R.id.post_container)
        val userProfileImagePost: ImageView = itemView.findViewById(R.id.userProfileImagePost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SMPostListViewModel {
        val view = LayoutInflater.from(context).inflate(R.layout.post_with_image_sm, parent, false)
        return SMPostListViewModel(view)
    }

    override fun getItemCount(): Int {
        return postListData.size
    }

    override fun onBindViewHolder(holder: SMPostListViewModel, position: Int) {
        val currentPost = postListData[position]

        holder.userNamePost.text = currentPost.get("name").toString()
        holder.userPostTitleValue.text = currentPost.get("title").toString()
        holder.userPostDescValue.text = currentPost.get("description").toString()
        holder.userPostUploadTime.text = DateUtils.getRelativeTimeSpanString(currentPost.get("timeStamp") as Long)

        val imageUrl = currentPost.get("imageUrl")
        Log.d("Post without Image1", imageUrl.toString())

        val uploadType = currentPost.get("uploadType").toString()
        if (uploadType == "video") {
            val webSet: WebSettings = holder.postVideo.settings
            webSet.javaScriptEnabled = true
            webSet.loadWithOverviewMode = true
            webSet.useWideViewPort = true

            holder.postVideo.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // Uncomment the line below if autoplay is required for the video
                    // holder.postVideo.loadUrl("javascript:(function() { document.getElementsByTagName('video')[0].play(); })()")
                }
            })

            holder.postVideo.loadUrl(currentPost.get("imageUrl").toString())
            holder.postImage.visibility = View.GONE
            holder.postVideo.visibility = View.VISIBLE

        } else if (uploadType == "image") {
            Glide.with(context).load(currentPost.get("imageUrl")).into(holder.postImage)
            holder.postVideo.visibility = View.GONE
            holder.postImage.visibility = View.VISIBLE
            Log.d("Upload Type 2", uploadType)

        } else if (uploadType.isEmpty()) {
            Log.d("Post without Image2", imageUrl.toString())
            holder.postImage.visibility = View.GONE
            holder.postVideo.visibility = View.GONE
            Log.d("Upload Type 3", uploadType)
        }

        firebaseAuth = FirebaseAuth.getInstance()

        holder.userProfileImageCard.animation = AnimationUtils.loadAnimation(context, R.anim.fade_transition)
        holder.postContainer.animation = AnimationUtils.loadAnimation(context, R.anim.fade_transition)

        holder.userPostDescValue.setOnClickListener {
            holder.userPostDescValue.maxLines = Int.MAX_VALUE
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection("users").document("${currentPost.get("userID")}").get()
            .addOnSuccessListener {
                val profileImage = it.get("profileImage").toString()
                if (!profileImage.isNullOrEmpty()) {
                    Glide.with(context).load(profileImage).into(holder.userProfileImagePost)
                }
            }
    }
}
