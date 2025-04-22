package com.example.agrione.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.single_ecomm_item.view.*

class EcommerceAdapter(
    private val context: Context,
    private val ecommListData: List<DocumentSnapshot>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<EcommerceAdapter.EcommerceViewHolder>() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    class EcommerceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EcommerceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_ecomm_item, parent, false)
        return EcommerceViewHolder(view)
    }

    override fun getItemCount(): Int = ecommListData.size

    override fun onBindViewHolder(holder: EcommerceViewHolder, position: Int) {
        val currentItem = ecommListData[position]

        holder.itemView.ecommtitle.text = currentItem.getString("title") ?: "N/A"
        holder.itemView.ecommPrice.text = "\u20B9 ${currentItem.get("price").toString()}"
        holder.itemView.ecommretailer.text = currentItem.getString("retailer") ?: "Unknown"
        holder.itemView.ecommItemAvailability.text = currentItem.getString("availability") ?: "Unavailable"

        val allImages = currentItem.get("imageUrl") as? List<*> ?: emptyList<String>()
        if (allImages.isNotEmpty()) {
            Glide.with(context)
                .load(allImages[0].toString())
                .into(holder.itemView.ecommImage)
        }

        holder.itemView.ecommRating.rating = currentItem.get("rating")?.toString()?.toFloatOrNull() ?: 0f

        holder.itemView.setOnClickListener {
            cellClickListener.onCellClickListener(currentItem.id)
        }
    }
}
