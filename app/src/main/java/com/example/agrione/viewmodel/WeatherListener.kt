package com.example.agrione.viewmodel

import androidx.lifecycle.LiveData

interface WeatherListener {
    fun onSuccess(weatherData: LiveData<String>)
}
