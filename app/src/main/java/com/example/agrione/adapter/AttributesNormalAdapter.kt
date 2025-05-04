package com.example.agrione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrione.R

class AttributesNormalAdapter(
    private val allData: List<Map<String, Any>>
) : RecyclerView.Adapter<AttributesNormalAdapter.AttributesNormalViewHolder>() {

    class AttributesNormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attributeTitle: TextView = itemView.findViewById(R.id.normalAttributeTitle)
        val attributeValue: TextView = itemView.findViewById(R.id.normalAttributeValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributesNormalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_normal_attributes_ecomm, parent, false)
        return AttributesNormalViewHolder(view)
    }

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: AttributesNormalViewHolder, position: Int) {
        val currentData = allData[position]
        for ((key, value) in currentData) {
            holder.attributeTitle.text = "$key - "
            holder.attributeValue.text = value.toString()
        }
    }
}
