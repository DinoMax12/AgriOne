package com.example.agrione.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agrione.R
import com.example.agrione.model.data.WeatherList
import kotlinx.android.synthetic.main.single_currentweather.view.*

class CurrentWeatherAdapter(val context: Context, private val weatherData: List<WeatherList>) :
    RecyclerView.Adapter<CurrentWeatherAdapter.CurrentWeatherViewHolder>() {

    class CurrentWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val temp: TextView = itemView.findViewById(R.id.temp)
        val desc: TextView = itemView.findViewById(R.id.desc)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val minTemp: TextView = itemView.findViewById(R.id.minTemp)
        val maxTemp: TextView = itemView.findViewById(R.id.maxTemp)
        val humidity: TextView = itemView.findViewById(R.id.humidity)
        val todayTitle: TextView = itemView.findViewById(R.id.todayTitle)
        val container: ConstraintLayout = itemView.findViewById(R.id.currentWeatherContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentWeatherViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_currentweather, parent, false)
        return CurrentWeatherViewHolder(view)
    }

    override fun getItemCount(): Int = weatherData.size

    override fun onBindViewHolder(holder: CurrentWeatherViewHolder, position: Int) {
        val weatherItem = weatherData[position]

        holder.temp.text = "${(weatherItem.main.temp - 273.15).toInt()}\u2103"
        holder.desc.text = weatherItem.weather[0].description.capitalize()
        holder.todayTitle.text = "Today, ${weatherItem.dt_txt.slice(10..15)}"

        Log.d("mytag", "Time: ${weatherItem.dt_txt.slice(10..weatherItem.dt_txt.length - 1)}")

        holder.minTemp.text = "${(weatherItem.main.temp_min.toDouble() - 273.1).toInt()}\u2103"
        holder.maxTemp.text = "${(weatherItem.main.temp_max.toDouble() - 273.1).toInt()}\u2103"
        holder.humidity.text = "${weatherItem.main.humidity}%"

        val iconCode = weatherItem.weather[0].icon
        val iconUrl = "https://openweathermap.org/img/w/$iconCode.png"

        Glide.with(holder.itemView.context)
            .load(iconUrl)
            .into(holder.icon)

        holder.container.animation = AnimationUtils.loadAnimation(context, R.anim.fade_scale)
    }
}
