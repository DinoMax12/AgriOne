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
        val myOrder = allData.entries.toTypedArray()[position].value as HashMap<String, Any>
        val viewModel = EcommViewModel()

        viewModel.getSpecificItem(myOrder["productId"].toString())
            .observe(context, androidx.lifecycle.Observer {
                Log.d("MyOrdersAdapter", it.toString())

                holder.itemName.text = it?.getString("title") ?: "N/A"
                holder.itemPrice.text = "₹" + (
                        myOrder["quantity"].toString().toInt() *
                                myOrder["itemCost"].toString().toInt() +
                                myOrder["deliveryCost"].toString().toInt()
                        ).toString()
                holder.pinCode.text = "Pin Code: ${myOrder["pincode"]}"
                holder.deliveryCharge.text = myOrder["deliveryCost"].toString()
                holder.deliveryStatus.text = myOrder["deliveryStatus"].toString()

                val allImages = it?.get("imageUrl") as List<String>
                Glide.with(context).load(allImages[0]).into(holder.itemImage)

                val date = myOrder["time"].toString().split(" ")
                holder.timeStamp.text = date[0]
            })

        holder.purchaseAgain.setOnClickListener {
            cartItemBuy.addToOrders(
                myOrder["productId"].toString(),
                myOrder["quantity"].toString().toInt(),
                myOrder["itemCost"].toString().toInt(),
                myOrder["deliveryCost"].toString().toInt()
            )
        }

        holder.itemView.setOnClickListener {
            cellClickListener.onCellClickListener(myOrder["productId"].toString())
        }
    }
}
