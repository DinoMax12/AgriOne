package com.example.agrione.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener

class PostListUserProfileAdapter(
    val context: Context,
    var listData: ArrayList<DocumentSnapshot>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<PostListUserProfileAdapter.PostListUserProfileViewHolder>() {

    class PostListUserProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userPostTitle: TextView = itemView.findViewById(R.id.userPostTitleUserProfileFrag)
        val userPostUploadTime: TextView = itemView.findViewById(R.id.userPostUploadTimeUserProfileFrag)
        val userPostImage: ImageView = itemView.findViewById(R.id.userPostImageUserProfileFrag)
        val userPostProfileCard: CardView = itemView.findViewById(R.id.userPostProfileCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListUserProfileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_profile_posts_single, parent, false)
        return PostListUserProfileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: PostListUserProfileViewHolder, position: Int) {
        val currentData = listData[position]

        holder.userPostTitle.text = currentData.get("title")?.toString() ?: ""
        
        // Safely handle timestamp
        val timestamp = currentData.get("timeStamp")
        holder.userPostUploadTime.text = when (timestamp) {
            is Long -> DateUtils.getRelativeTimeSpanString(timestamp)
            is Number -> DateUtils.getRelativeTimeSpanString(timestamp.toLong())
            else -> "Just now"
        }

        holder.userPostProfileCard.setOnClickListener {
            cellClickListener.onCellClickListener(currentData.id)
        }

        // Safely handle image URL
        val imageUrl = currentData.getString("imageUrl")
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_user_profile)
                .error(R.drawable.ic_user_profile)
                .into(holder.userPostImage)
        } else {
            holder.userPostImage.setImageResource(R.drawable.ic_user_profile)
        }
    }
}
