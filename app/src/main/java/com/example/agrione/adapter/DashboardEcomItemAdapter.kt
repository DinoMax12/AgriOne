package com.example.agrione.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.single_dashboard_ecomm_item.view.*

class DashboardEcomItemAdapter(
    private val context: Context,
    private val allData: List<DocumentSnapshot>,
    private val itemsToShow: List<Int>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<DashboardEcomItemAdapter.DashboardEcomItemViewHolder>() {

    class DashboardEcomItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardEcomItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_dashboard_ecomm_item, parent, false)
        return DashboardEcomItemViewHolder(view)
    }

    override fun getItemCount(): Int = itemsToShow.size

    override fun onBindViewHolder(holder: DashboardEcomItemViewHolder, position: Int) {
        val currentData = allData[itemsToShow[position]]

        holder.itemView.itemTitle.text = currentData.get("title").toString()
        holder.itemView.itemPrice.text = "\u20B9${currentData.get("price").toString()}"

        val allImages = currentData.get("imageUrl") as? ArrayList<*> ?: arrayListOf<String>()
        if (allImages.isNotEmpty()) {
            Glide.with(context)
                .load(allImages[0])
                .into(holder.itemView.itemImage)
        }

        holder.itemView.setOnClickListener {
            cellClickListener.onCellClickListener(currentData.id)
        }
    }
}
