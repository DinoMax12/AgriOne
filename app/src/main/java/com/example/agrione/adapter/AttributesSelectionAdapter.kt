package com.example.agrione.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener

class AttributesSelectionAdapter(
    private val context: Context,
    private val allData: List<Map<String, Any>>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<AttributesSelectionAdapter.AttributesSelectionViewHolder>() {

    class AttributesSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val attributeTitle: TextView = itemView.findViewById(R.id.attributeTitle)
        val attribute1: TextView = itemView.findViewById(R.id.attribute1)
        val attribute1Price: TextView = itemView.findViewById(R.id.attribute1Price)
        val attribute2: TextView = itemView.findViewById(R.id.attribute2)
        val attribute2Price: TextView = itemView.findViewById(R.id.attribute2Price)
        val attribute3: TextView = itemView.findViewById(R.id.attribute3)
        val attribute3Price: TextView = itemView.findViewById(R.id.attribute3Price)
        val cardSize1: CardView = itemView.findViewById(R.id.cardSize1)
        val cardSize2: CardView = itemView.findViewById(R.id.cardSize2)
        val cardSize3: CardView = itemView.findViewById(R.id.cardSize3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributesSelectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_selection_attributes_ecomm, parent, false)
        return AttributesSelectionViewHolder(view)
    }

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: AttributesSelectionViewHolder, position: Int) {
        val currentData = allData[position]

        for ((key, values) in currentData) {
            cellClickListener.onCellClickListener("1 $key")

            holder.attributeTitle.text = key
            var allValues = values as ArrayList<String>
            var currentValue = allValues[0].split(" ")
            holder.attribute1.text = currentValue[0]
            holder.attribute1Price.text = currentValue[1]

            currentValue = allValues[1].split(" ")
            holder.attribute2.text = currentValue[0]
            holder.attribute2Price.text = currentValue[1]

            currentValue = allValues[2].split(" ")
            holder.attribute3.text = currentValue[0]
            holder.attribute3Price.text = currentValue[1]

            // Card Size 1 Click Listener
            holder.cardSize1.setOnClickListener {
                handleCardSelection(holder, 1, key)
            }

            // Card Size 2 Click Listener
            holder.cardSize2.setOnClickListener {
                handleCardSelection(holder, 2, key)
            }

            // Card Size 3 Click Listener
            holder.cardSize3.setOnClickListener {
                handleCardSelection(holder, 3, key)
            }
        }
    }

    private fun handleCardSelection(
        holder: AttributesSelectionViewHolder,
        selectedCard: Int,
        key: String
    ) {
        // Notify Cell Click Listener
        cellClickListener.onCellClickListener("$selectedCard $key")
        Toast.makeText(context, "You Clicked $selectedCard", Toast.LENGTH_SHORT).show()

        // Reset all card styles to default
        resetCardStyles(holder)

        // Set selected card's style
        when (selectedCard) {
            1 -> updateCardStyles(holder, 1)
            2 -> updateCardStyles(holder, 2)
            3 -> updateCardStyles(holder, 3)
        }
    }

    private fun resetCardStyles(holder: AttributesSelectionViewHolder) {
        // Reset all card backgrounds and text styles
        listOf(holder.cardSize1, holder.cardSize2, holder.cardSize3).forEach {
            it.backgroundTintList = context.getResources().getColorStateList(R.color.secondary)
        }
        listOf(holder.attribute1, holder.attribute2, holder.attribute3).forEach {
            it.setTextColor(Color.parseColor("#FF404A3A"))
            it.setTypeface(null, Typeface.NORMAL)
        }
        listOf(holder.attribute1Price, holder.attribute2Price, holder.attribute3Price).forEach {
            it.setTextColor(Color.parseColor("#FF404A3A"))
            it.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun updateCardStyles(holder: AttributesSelectionViewHolder, selectedCard: Int) {
        // Update the selected card's background and text style
        when (selectedCard) {
            1 -> {
                holder.cardSize1.backgroundTintList = context.getResources().getColorStateList(R.color.colorPrimary)
                holder.attribute1.setTextColor(Color.parseColor("#FFFFFF"))
                holder.attribute1Price.setTextColor(Color.parseColor("#FFFFFF"))
                holder.attribute1.setTypeface(null, Typeface.BOLD)
                holder.attribute1Price.setTypeface(null, Typeface.BOLD)
            }
            2 -> {
                holder.cardSize2.backgroundTintList = context.getResources().getColorStateList(R.color.colorPrimary)
                holder.attribute2.setTextColor(Color.parseColor("#FFFFFF"))
                holder.attribute2Price.setTextColor(Color.parseColor("#FFFFFF"))
                holder.attribute2.setTypeface(null, Typeface.BOLD)
                holder.attribute2Price.setTypeface(null, Typeface.BOLD)
            }
            3 -> {
                holder.cardSize3.backgroundTintList = context.getResources().getColorStateList(R.color.colorPrimary)
                holder.attribute3.setTextColor(Color.parseColor("#FFFFFF"))
                holder.attribute3Price.setTextColor(Color.parseColor("#FFFFFF"))
                holder.attribute3.setTypeface(null, Typeface.BOLD)
                holder.attribute3Price.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}
