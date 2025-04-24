package com.agrione.app.view.weather

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrione.app.R
import com.agrione.app.adapter.CurrentWeatherAdapter
import com.agrione.app.adapter.WeatherAdapter
import com.agrione.app.databinding.FragmentWeatherBinding
import com.agrione.app.model.data.WeatherList
import com.agrione.app.viewmodel.WeatherListener
import com.agrione.app.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_weather.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class WeatherFragment : Fragment(), WeatherListener {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: WeatherAdapter
    private lateinit var adapter2: CurrentWeatherAdapter
    private lateinit var viewModel: WeatherViewModel
    private var fragmentWeatherBinding: FragmentWeatherBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(requireActivity())[WeatherViewModel::class.java]

        val bundle = this.arguments
        bundle?.getString("key")?.let {
            Log.d("WeatherFrag Bundle", it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Weather Forecast"

        val city = viewModel.getCoordinates().value
        weatherCity.text = city?.get(2).toString()
        val newWeatherData = viewModel.newDataTrial.value
        Log.d("New Data Weather Trial", newWeatherData.toString())

        val firstDate = newWeatherData!!.list[0].dt_txt.slice(8..9)
        var otherDates = firstDate
        var i = 1
        val data2 = mutableListOf<WeatherList>()

        while (otherDates == firstDate) {
            data2.add(newWeatherData.list[i - 1])
            otherDates = newWeatherData.list[i].dt_txt.slice(8..9)
            i++
        }

        val data3 = mutableListOf<WeatherList>()
        for (a in i - 1..39) {
            if (newWeatherData.list[a].dt_txt.slice(11..12) == "12") {
                Log.d("Something date", newWeatherData.list[a].dt_txt)
                data3.add(newWeatherData.list[a])
            }
        }

        adapter = WeatherAdapter(requireContext(), data3)
        adapter2 = CurrentWeatherAdapter(requireContext(), data2)

        rcylr_weather.adapter = adapter
        rcylr_weather.layoutManager = LinearLayoutManager(requireContext())

        currentWeather_rcycl.adapter = adapter2
        currentWeather_rcycl.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeatherFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onSuccess(authRepo: LiveData<String>) {
        authRepo.observe(this, Observer {
            Log.d("Frag", it)
        })
    }
}
