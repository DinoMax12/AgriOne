package com.example.agrione.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.utilities.CartItemBuy
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.viewmodel.EcommViewModel
import kotlinx.android.synthetic.main.single_myorder_item.view.*

class MyOrdersAdapter(
    val context: MyOrdersFragment,
    val allData: HashMap<String, Object>,
    val cellClickListener: CellClickListener,
    val cartItemBuy: CartItemBuy
) : RecyclerView.Adapter<MyOrdersAdapter.MyOrdersViewHolder>() {

    class MyOrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersViewHolder {
        val view = LayoutInflater.from(context.context)
            .inflate(R.layout.single_myorder_item, parent, false)
        return MyOrdersViewHolder(view)
    }

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: MyOrdersViewHolder, position: Int) {
        val myOrder = allData.entries.toTypedArray()[position].value as HashMap<String, Object>
        val viewModel = EcommViewModel()

        viewModel.getSpecificItem(myOrder["productId"].toString())
            .observe(context, androidx.lifecycle.Observer {
                Log.d("MyOrdersAdapter", it.toString())

                holder.itemView.apply {
                    myOrderItemName.text = it?.getString("title") ?: "N/A"
                    myOrderItemPrice.text = "₹" + (
                            myOrder["quantity"].toString().toInt() *
                                    myOrder["itemCost"].toString().toInt() +
                                    myOrder["deliveryCost"].toString().toInt()
                            ).toString()
                    myOrderPinCode.text = "Pin Code: ${myOrder["pincode"]}"
                    myOderDeliveryCharge.text = myOrder["deliveryCost"].toString()
                    myOrderDeliveryStatus.text = myOrder["deliveryStatus"].toString()

                    val allImages = it?.get("imageUrl") as List<String>
                    Glide.with(context).load(allImages[0]).into(myOderItemImage)

                    val date = myOrder["time"].toString().split(" ")
                    myOrderTimeStamp.text = date[0]
                }
            })

        holder.itemView.myOrderPurchaseAgain.setOnClickListener {
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
