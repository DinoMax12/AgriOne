package com.example.agrione.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.view.ecommerce.MyOrdersFragment
import com.example.agrione.viewmodel.EcommViewModel
import androidx.lifecycle.Observer
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks

class MyOrdersAdapter(
    val context: MyOrdersFragment,
    val allData: HashMap<String, Any>,
    val cellClickListener: CellClickListener,
    val cartItemBuy: CartItemBuy
) : RecyclerView.Adapter<MyOrdersAdapter.MyOrdersViewHolder>() {

    class MyOrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.myOrderItemName)
        val itemPrice: TextView = itemView.findViewById(R.id.myOrderItemPrice)
        val pinCode: TextView = itemView.findViewById(R.id.myOrderPinCode)
        val deliveryCharge: TextView = itemView.findViewById(R.id.myOderDeliveryCharge)
        val deliveryStatus: TextView = itemView.findViewById(R.id.myOrderDeliveryStatus)
        val itemImage: ImageView = itemView.findViewById(R.id.myOderItemImage)
        val timeStamp: TextView = itemView.findViewById(R.id.myOrderTimeStamp)
        val purchaseAgain: Button = itemView.findViewById(R.id.myOrderPurchaseAgain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersViewHolder {
        val view = LayoutInflater.from(context.context)
            .inflate(R.layout.single_myorder_item, parent, false)
        return MyOrdersViewHolder(view)
    }

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: MyOrdersViewHolder, position: Int) {
        val currentData = allData.entries.toTypedArray()[position]
        val ecommViewModel = EcommViewModel()
        
        // Get the order data
        val orderData = currentData.value as? Map<String, Any>
        if (orderData != null) {
            // Get product details from the order data
            val productId = orderData["productId"] as? String
            val quantity = (orderData["quantity"] as? Number)?.toInt() ?: 1
            val timestamp = orderData["timestamp"] as? Long ?: System.currentTimeMillis()
            val pinCode = orderData["pincode"] as? String ?: "N/A"
            
            // Get or set delivery date
            val database = FirebaseDatabase.getInstance()
            database.getReference("orders").child(currentData.key)
                .get()
                .addOnSuccessListener { orderSnapshot ->
                    val deliveryDate = if (orderSnapshot.child("deliveryDate").exists()) {
                        orderSnapshot.child("deliveryDate").value as String
                    } else {
                        // Calculate and store new delivery date
                        val randomDays = (3..7).random()
                        val calculatedDate = calculateDeliveryDate(timestamp, randomDays)
                        
                        // Store in database
                        database.getReference("orders")
                            .child(currentData.key)
                            .child("deliveryDate")
                            .setValue(calculatedDate)
                        
                        calculatedDate
                    }
                    
                    if (productId != null) {
                        ecommViewModel.getSpecificItem(productId).observe(context, Observer { item ->
                            // Safely get price and delivery charge with default values
                            val itemCost = item["price"]?.toString()?.toDoubleOrNull() ?: 0.0
                            val deliveryCharge = item["delCharge"]?.toString()?.toDoubleOrNull() ?: 0.0

                            holder.itemName.text = item["title"]?.toString() ?: "N/A"
                            holder.itemPrice.text = "₹" + String.format("%.2f", itemCost)
                            holder.pinCode.text = "Pin Code: $pinCode"
                            holder.deliveryCharge.text = String.format("%.2f", deliveryCharge)
                            holder.deliveryStatus.text = "Arriving by: $deliveryDate"
                            holder.timeStamp.text = formatOrderDate(timestamp)

                            // Handle imageUrl that could be either String or List<String>
                            val imageUrl = when (val imageUrlValue = item["imageUrl"]) {
                                is String -> imageUrlValue
                                is List<*> -> if (imageUrlValue.isNotEmpty()) imageUrlValue[0].toString() else ""
                                else -> ""
                            }
                            
                            if (imageUrl.isNotEmpty()) {
                                Glide.with(context)
                                    .load(imageUrl)
                                    .into(holder.itemImage)
                            }

                            val total = itemCost * quantity + deliveryCharge
                            holder.purchaseAgain.text = "Buy Again: ₹" + String.format("%.2f", total)

                            holder.purchaseAgain.setOnClickListener {
                                Log.d("totalPrice", "Qty: $quantity, Price: $itemCost, Delivery: $deliveryCharge")
                                cartItemBuy.addToOrders(productId, quantity, itemCost.toInt(), deliveryCharge.toInt())
                            }

                            holder.itemView.setOnClickListener {
                                cellClickListener.onCellClickListener(productId)
                            }
                        })
                    }
                }
        }
    }

    private fun calculateDeliveryDate(timestamp: Long, daysToAdd: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(java.util.Calendar.DAY_OF_MONTH, daysToAdd)
        
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun formatOrderDate(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }
}
