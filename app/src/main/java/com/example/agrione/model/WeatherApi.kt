package com.example.agrione.model

import com.example.agrione.model.data.WeatherRootList
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.openweathermap.org/"
const val API_KEY = "63259e8886cbe4d575c24358fb860b1b"

interface WeatherInterface {
    @GET("data/2.5/forecast?appid=$API_KEY")
    fun getWeather(@Query("lat") lat: String, @Query("lon") lon: String): Call<WeatherRootList>
}

object WeatherApi {
    val weatherInstances: WeatherInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherInstances = retrofit.create(WeatherInterface::class.java)
    }
}
