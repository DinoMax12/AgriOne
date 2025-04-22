package com.example.agrione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agrione.R
import com.example.agrione.model.data.IntroData

class IntroAdapter(private val introSlides: List<IntroData>) : RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {

    inner class IntroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle: TextView = view.findViewById(R.id.sliderTitle)
        private val textDescription: TextView = view.findViewById(R.id.sliderDescription)
        private val imageIcon: ImageView = view.findViewById(R.id.imageSlider)

        fun bind(introSlider: IntroData) {
            textTitle.text = introSlider.title
            textDescription.text = introSlider.description
            imageIcon.setImageResource(introSlider.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_slider_screen, parent, false)
        return IntroViewHolder(view)
    }

    override fun getItemCount(): Int = introSlides.size

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.bind(introSlides[position])
    }
}
