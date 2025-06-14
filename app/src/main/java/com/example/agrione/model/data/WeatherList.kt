package com.example.agrione.model.data

data class WeatherList(
    val main: WeatherMain,
    val weather: List<Weather>,
    val dt_txt: String,
    val wind: WeatherWind
)
