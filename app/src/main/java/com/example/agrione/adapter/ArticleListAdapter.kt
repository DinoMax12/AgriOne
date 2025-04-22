package com.example.agrione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.utilities.CellClickListener
import com.google.firebase.firestore.DocumentSnapshot

class ArticleListAdapter(
    private val articleListData: List<DocumentSnapshot>,
    private val cellClickListener: CellClickListener
) : RecyclerView.Adapter<ArticleListAdapter.ArticleListViewholder>() {

    class ArticleListViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val articleName: TextView = itemView.findViewById(R.id.descTextxArticleListFrag)
        val articleImage: ImageView = itemView.findViewById(R.id.imageArticleListFrag)
        val articleCard: CardView = itemView.findViewById(R.id.articleListCardArtListFrag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleListViewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_list_single, parent, false)
        return ArticleListViewholder(view)
    }

    override fun getItemCount(): Int = articleListData.size

    override fun onBindViewHolder(holder: ArticleListViewholder, position: Int) {
        val singleArticle = articleListData[position]

        holder.articleName.text = singleArticle.getString("title")

        holder.articleCard.setOnClickListener {
            cellClickListener.onCellClickListener(singleArticle.getString("title").toString())
        }

        val images: List<String> = singleArticle.get("images") as List<String>
        Glide.with(holder.itemView.context)
            .load(images.firstOrNull())
            .into(holder.articleImage)
    }
}
