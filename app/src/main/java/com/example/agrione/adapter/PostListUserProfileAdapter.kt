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

        holder.userPostTitle.text = currentData.get("title").toString()
        holder.userPostUploadTime.text =
            DateUtils.getRelativeTimeSpanString(currentData.get("timeStamp") as Long)
        holder.userPostProfileCard.setOnClickListener {
            cellClickListener.onCellClickListener(currentData.id)
        }
        if (!currentData.get("imageUrl").toString().isNullOrEmpty()) {
            Glide.with(context).load(currentData.getString("imageUrl")).into(holder.userPostImage)
        }
    }
}
