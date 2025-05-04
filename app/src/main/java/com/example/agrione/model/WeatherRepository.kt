package com.example.agrione.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.agrione.model.data.WeatherRootList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository {

    val data = MutableLiveData<WeatherRootList>()

    fun getWeather(): LiveData<String> {
        val response: Call<WeatherRootList> =
            WeatherApi.weatherInstances.getWeather("23.0225", "72.5714")

        val weatherRes = MutableLiveData<String>()

        response.enqueue(object : Callback<WeatherRootList> {
            override fun onFailure(call: Call<WeatherRootList>, t: Throwable) {
                Log.d("WeatherRepository", "Error Occurred")
            }

            override fun onResponse(
                call: Call<WeatherRootList>,
                response: Response<WeatherRootList>
            ) {
                if (response.isSuccessful) {
                    data.value = response.body()
                    weatherRes.value = "DONE"
                } else {
                    weatherRes.value = "FAILED"
                }
            }
        })
        return weatherRes
    }
}
