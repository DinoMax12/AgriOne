package com.example.agrione.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EcommerceAdapter(
    private val context: Context,
    private val ecommListData: List<DocumentSnapshot>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<EcommerceAdapter.EcommerceViewHolder>() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    class EcommerceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ecommtitle: TextView = itemView.findViewById(R.id.ecommtitle)
        val ecommPrice: TextView = itemView.findViewById(R.id.ecommPrice)
        val ecommretailer: TextView = itemView.findViewById(R.id.ecommretailer)
        val ecommItemAvailability: TextView = itemView.findViewById(R.id.ecommItemAvailability)
        val ecommImage: ImageView = itemView.findViewById(R.id.ecommImage)
        val ecommRating: RatingBar = itemView.findViewById(R.id.ecommRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EcommerceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_ecomm_item, parent, false)
        return EcommerceViewHolder(view)
    }

    override fun getItemCount(): Int = ecommListData.size

    override fun onBindViewHolder(holder: EcommerceViewHolder, position: Int) {
        val currentItem = ecommListData[position]
        val imageUrl = currentItem.get("imageUrl") as? String
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(imageUrl).into(holder.ecommImage)
        }
        holder.ecommtitle.text = currentItem.getString("title") ?: "N/A"
        holder.ecommPrice.text = "\u20B9 ${currentItem.get("price").toString()}"
        holder.ecommretailer.text = currentItem.getString("retailer") ?: "Unknown"
        holder.ecommItemAvailability.text = currentItem.getString("availability") ?: "Unavailable"
        holder.ecommRating.rating = currentItem.get("rating")?.toString()?.toFloatOrNull() ?: 0f

        holder.itemView.setOnClickListener {
            cellClickListener.onCellClickListener(currentItem.id)
        }
    }
}
