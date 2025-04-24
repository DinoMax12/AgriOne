package com.agrione.app.viewmodel

import androidx.lifecycle.LiveData

interface WeatherListener {
    fun onSuccess(weatherData: LiveData<String>)
}
