package com.example.agrione.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.R
import com.example.agrione.model.data.CartItem
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.view.ecommerce.CartFragment
import com.example.agrione.viewmodel.EcommViewModel

class CartItemsAdapter(
    val context: CartFragment,
    val allData: HashMap<String, Any>,
    val cartItemBuy: CartItemBuy
) : RecyclerView.Adapter<CartItemsAdapter.CartItemsViewHolder>() {

    // Add a map to store product totals
    private val productTotals = mutableMapOf<String, Double>()

    // Add a callback interface
    interface TotalUpdateListener {
        fun onTotalUpdated(totals: Map<String, Double>)
    }

    private var totalUpdateListener: TotalUpdateListener? = null

    fun setTotalUpdateListener(listener: TotalUpdateListener) {
        totalUpdateListener = listener
    }

    class CartItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemNameCart)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPriceCart)
        val quantityCount: TextView = itemView.findViewById(R.id.quantityCountEcomm)
        val deliveryCharge: TextView = itemView.findViewById(R.id.deliveryChargeCart)
        val firmName: TextView = itemView.findViewById(R.id.cartItemFirm)
        val availability: TextView = itemView.findViewById(R.id.cartItemAvailability)
        val itemImage: ImageView = itemView.findViewById(R.id.cartItemImage)
        val buyButton: Button = itemView.findViewById(R.id.cartItemBuyBtn)
        val increaseButton: ImageButton = itemView.findViewById(R.id.increaseQtyBtn)
        val decreaseButton: ImageButton = itemView.findViewById(R.id.decreaseQtyBtn)
        val removeButton: ImageView = itemView.findViewById(R.id.removeCartBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemsViewHolder {
        val view = LayoutInflater.from(context.context)
            .inflate(R.layout.single_cart_item, parent, false)
        return CartItemsViewHolder(view)
    }

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: CartItemsViewHolder, position: Int) {
        val currentData = allData.entries.toTypedArray()[position]
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseDatabase = FirebaseDatabase.getInstance()

        val itemQtyRef = firebaseDatabase
            .getReference("users")
            .child(firebaseAuth.currentUser?.uid ?: "")
            .child("cart")
            .child(currentData.key)
            .child("quantity")

        val itemRef = firebaseDatabase
            .getReference("users")
            .child(firebaseAuth.currentUser?.uid ?: "")
            .child("cart")
            .child(currentData.key)

        val cartItem = currentData.value as CartItem
        val quantity = cartItem.quantity

        val ecommViewModel = EcommViewModel()
        ecommViewModel.getSpecificItem(currentData.key).observe(context, Observer { item ->
            val itemCost = item["price"].toString().toDouble()
            val deliveryCharge = item["delCharge"].toString().toDouble()

            holder.itemName.text = item["title"].toString()
            holder.itemPrice.text = "₹" + String.format("%.2f", itemCost)
            holder.quantityCount.text = quantity.toString()
            holder.deliveryCharge.text = String.format("%.2f", deliveryCharge)
            
            // Set retailer name with marquee
            holder.firmName.apply {
                text = item["retailer"].toString()
                isSelected = true  // This enables the marquee effect
                isFocusable = true
                isFocusableInTouchMode = true
            }
            
            holder.availability.text = item["availability"].toString()

            val imageUrl = item["imageUrl"] as? String
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(context).load(imageUrl).into(holder.itemImage)
            }

            val total = itemCost * quantity + deliveryCharge
            holder.buyButton.text = "Buy Now: ₹" + String.format("%.2f", total)

            // Update product total and notify listener
            productTotals[currentData.key] = total
            totalUpdateListener?.onTotalUpdated(productTotals)

            holder.buyButton.setOnClickListener {
                Log.d("totalPrice", "Qty: $quantity, Price: $itemCost, Delivery: $deliveryCharge")
                cartItemBuy.addToOrders(currentData.key, quantity, itemCost.toInt(), deliveryCharge.toInt())
            }
        })

        holder.increaseButton.setOnClickListener {
            val newQty = holder.quantityCount.text.toString().toInt() + 1
            holder.quantityCount.text = newQty.toString()
            itemQtyRef.setValue(newQty)
        }

        holder.decreaseButton.setOnClickListener {
            val currentQty = holder.quantityCount.text.toString().toInt()
            if (currentQty > 1) {
                val newQty = currentQty - 1
                holder.quantityCount.text = newQty.toString()
                itemQtyRef.setValue(newQty)
            }
        }

        holder.removeButton.setOnClickListener {
            // Remove from product totals when item is removed
            productTotals.remove(currentData.key)
            totalUpdateListener?.onTotalUpdated(productTotals)
            itemRef.removeValue()
        }
    }
}
