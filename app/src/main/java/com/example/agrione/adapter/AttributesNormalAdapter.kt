package com.example.agrione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agrione.R
import kotlinx.android.synthetic.main.single_normal_attributes_ecomm.view.*

class AttributesNormalAdapter(
    private val allData: List<Map<String, Any>>
) : RecyclerView.Adapter<AttributesNormalAdapter.AttributesNormalViewHolder>() {

    class AttributesNormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributesNormalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_normal_attributes_ecomm, parent, false)
        return AttributesNormalViewHolder(view)
    }

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: AttributesNormalViewHolder, position: Int) {
        val currentData = allData[position]
        for ((key, value) in currentData) {
            holder.itemView.normalAttributeTitle.text = "$key - "
            holder.itemView.normalAttributeValue.text = value.toString()
        }
    }
}
