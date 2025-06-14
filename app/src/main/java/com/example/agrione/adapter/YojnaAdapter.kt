package com.example.agrione.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.example.agrione.R
import com.example.agrione.utilities.CellClickListener

class YojnaAdapter(
    val context: Context,
    val yojnaData: List<DocumentSnapshot>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<YojnaAdapter.YojnaListviewHolder>() {

    class YojnaListviewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var yojnaName = itemView.findViewById<TextView>(R.id.yojnaTitleYojnaList)
        var yojnaImage = itemView.findViewById<ImageView>(R.id.yojnaImageYojnaList)
        var yojnaDate = itemView.findViewById<TextView>(R.id.yojnaDateYojnaList)
        var yojnaCard = itemView.findViewById<CardView>(R.id.singlelistyojnacard)
        var yojnaStatus = itemView.findViewById<TextView>(R.id.yojnaStatusYojnaList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YojnaListviewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_yojna_list, parent, false)
        return YojnaListviewHolder(view)
    }

    override fun getItemCount(): Int {
        return yojnaData.size
    }

    override fun onBindViewHolder(holder: YojnaListviewHolder, position: Int) {
        val singleYojna = yojnaData[position]

        holder.yojnaName.text = singleYojna.data!!.get("title").toString()
        holder.yojnaStatus.text = singleYojna.data!!.get("status").toString()
        holder.yojnaDate.text = singleYojna.data!!.get("launch").toString()
        val url = singleYojna.data!!.get("image").toString()

        Glide.with(holder.itemView.context)
            .load(url)
            .into(holder.yojnaImage)

        holder.yojnaCard.setOnClickListener {
            cellClickListener.onCellClickListener(singleYojna.data!!.get("title").toString())
        }
    }
}
