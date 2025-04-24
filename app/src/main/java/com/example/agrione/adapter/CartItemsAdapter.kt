package com.example.agrione.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.R
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.viewmodel.EcommViewModel
import kotlinx.android.synthetic.main.single_cart_item.view.*

class CartItemsAdapter(
    val context: CartFragment,
    val allData: HashMap<String, Object>,
    val cartItemBuy: CartItemBuy
) : RecyclerView.Adapter<CartItemsAdapter.CartItemsViewHolder>() {

    class CartItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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
            .getReference(firebaseAuth.currentUser!!.uid)
            .child("cart")
            .child(currentData.key)
            .child("quantity")

        val itemRef = firebaseDatabase
            .getReference(firebaseAuth.currentUser!!.uid)
            .child("cart")
            .child(currentData.key)

        val curr = currentData.value as Map<String, Object>
        val quantity = curr["quantity"].toString().toInt()

        val ecommViewModel = EcommViewModel()
        ecommViewModel.getSpecificItem(currentData.key).observe(context, Observer { item ->
            val itemCost = item["price"].toString().toInt()
            val deliveryCharge = item["delCharge"].toString().toInt()

            holder.itemView.itemNameCart.text = item["title"].toString()
            holder.itemView.itemPriceCart.text = "₹$itemCost"
            holder.itemView.quantityCountEcomm.text = quantity.toString()
            holder.itemView.deliveryChargeCart.text = deliveryCharge.toString()
            holder.itemView.cartItemFirm.text = item["retailer"].toString()
            holder.itemView.cartItemAvailability.text = item["availability"].toString()

            val imageUrl = item["imageUrl"] as ArrayList<String>
            Glide.with(context).load(imageUrl[0]).into(holder.itemView.cartItemImage)

            val total = itemCost * quantity + deliveryCharge
            holder.itemView.cartItemBuyBtn.text = "Buy Now: ₹$total"

            holder.itemView.cartItemBuyBtn.setOnClickListener {
                Log.d("totalPrice", "Qty: $quantity, Price: $itemCost, Delivery: $deliveryCharge")
                cartItemBuy.addToOrders(currentData.key, quantity, itemCost, deliveryCharge)
            }
        })

        holder.itemView.increaseQtyBtn.setOnClickListener {
            val newQty = holder.itemView.quantityCountEcomm.text.toString().toInt() + 1
            holder.itemView.quantityCountEcomm.text = newQty.toString()
            itemQtyRef.setValue(newQty)
        }

        holder.itemView.decreaseQtyBtn.setOnClickListener {
            val currentQty = holder.itemView.quantityCountEcomm.text.toString().toInt()
            if (currentQty > 1) {
                val newQty = currentQty - 1
                holder.itemView.quantityCountEcomm.text = newQty.toString()
                itemQtyRef.setValue(newQty)
            }
        }

        holder.itemView.removeCartBtn.setOnClickListener {
            itemRef.removeValue()
        }
    }
}
