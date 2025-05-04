package com.example.agrione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrione.R
import com.example.agrione.model.data.APMCCustomRecords

class ApmcAdapter(
    private val data: List<APMCCustomRecords>
) : RecyclerView.Adapter<ApmcAdapter.ApmcViewHolder>() {

    class ApmcViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val market: TextView = itemView.findViewById(R.id.apmcNameValue)
        val location: TextView = itemView.findViewById(R.id.apmcLocationValue)
        val commodity: TextView = itemView.findViewById(R.id.comodityname)
        val min: TextView = itemView.findViewById(R.id.minvalue)
        val max: TextView = itemView.findViewById(R.id.maxvalue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApmcViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.apmc_single_list, parent, false)
        return ApmcViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ApmcViewHolder, position: Int) {
        val mainData = data[position]

        holder.market.text = mainData.market
        holder.location.text = "${mainData.district}, ${mainData.state}"

        holder.commodity.text = mainData.commodity.joinToString("\n")
        holder.min.text = mainData.min_price.joinToString("\n")
        holder.max.text = mainData.max_price.joinToString("\n")
    }
}
