package com.example.agrione.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.agrione.model.data.APMCMain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APMCRepository {
    private val apmcService = APMCApi.apmcInstances

    // LiveData to observe the API response
    private val _apmcData = MutableLiveData<APMCMain>()
    val apmcData: LiveData<APMCMain> get() = _apmcData

    // LiveData for handling errors
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Function to fetch APMC data based on district filter
    fun getAPMCDataByDistrict(district: String) {
        apmcService.getSomeData(district).enqueue(object : Callback<APMCMain> {
            override fun onResponse(call: Call<APMCMain>, response: Response<APMCMain>) {
                if (response.isSuccessful) {
                    _apmcData.postValue(response.body())
                } else {
                    _errorMessage.postValue("Failed to fetch data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<APMCMain>, t: Throwable) {
                _errorMessage.postValue("Error: ${t.message}")
            }
        })
    }
}
