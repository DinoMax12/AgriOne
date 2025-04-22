package com.example.agrione.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener
import kotlinx.android.synthetic.main.single_selection_attributes_ecomm.view.*

class AttributesSelectionAdapter(
    private val context: Context,
    private val allData: List<Map<String, Any>>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<AttributesSelectionAdapter.AttributesSelectionViewHolder>() {

    class AttributesSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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

            holder.itemView.attributeTitle.text = key
            var allValues = values as ArrayList<String>
            var currentValue = allValues[0].split(" ")
            holder.itemView.attribute1.text = currentValue[0]
            holder.itemView.attribute1Price.text = currentValue[1]

            currentValue = allValues[1].split(" ")
            holder.itemView.attribute2.text = currentValue[0]
            holder.itemView.attribute2Price.text = currentValue[1]

            currentValue = allValues[2].split(" ")
            holder.itemView.attribute3.text = currentValue[0]
            holder.itemView.attribute3Price.text = currentValue[1]

            // Card Size 1 Click Listener
            holder.itemView.cardSize1.setOnClickListener {
                handleCardSelection(holder, 1, key)
            }

            // Card Size 2 Click Listener
            holder.itemView.cardSize2.setOnClickListener {
                handleCardSelection(holder, 2, key)
            }

            // Card Size 3 Click Listener
            holder.itemView.cardSize3.setOnClickListener {
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
        listOf(holder.itemView.cardSize1, holder.itemView.cardSize2, holder.itemView.cardSize3).forEach {
            it.backgroundTintList = context.getResources().getColorStateList(R.color.secondary)
        }
        listOf(holder.itemView.attribute1, holder.itemView.attribute2, holder.itemView.attribute3).forEach {
            it.setTextColor(Color.parseColor("#FF404A3A"))
            it.setTypeface(null, Typeface.NORMAL)
        }
        listOf(holder.itemView.attribute1Price, holder.itemView.attribute2Price, holder.itemView.attribute3Price).forEach {
            it.setTextColor(Color.parseColor("#FF404A3A"))
            it.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun updateCardStyles(holder: AttributesSelectionViewHolder, selectedCard: Int) {
        // Update the selected card's background and text style
        when (selectedCard) {
            1 -> {
                holder.itemView.cardSize1.backgroundTintList = context.getResources().getColorStateList(R.color.colorPrimary)
                holder.itemView.attribute1.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.attribute1Price.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.attribute1.setTypeface(null, Typeface.BOLD)
                holder.itemView.attribute1Price.setTypeface(null, Typeface.BOLD)
            }
            2 -> {
                holder.itemView.cardSize2.backgroundTintList = context.getResources().getColorStateList(R.color.colorPrimary)
                holder.itemView.attribute2.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.attribute2Price.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.attribute2.setTypeface(null, Typeface.BOLD)
                holder.itemView.attribute2Price.setTypeface(null, Typeface.BOLD)
            }
            3 -> {
                holder.itemView.cardSize3.backgroundTintList = context.getResources().getColorStateList(R.color.colorPrimary)
                holder.itemView.attribute3.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.attribute3Price.setTextColor(Color.parseColor("#FFFFFF"))
                holder.itemView.attribute3.setTypeface(null, Typeface.BOLD)
                holder.itemView.attribute3Price.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}
