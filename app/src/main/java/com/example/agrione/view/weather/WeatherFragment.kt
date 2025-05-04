package com.example.agrione.view.weather

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
import com.example.agrione.R
import com.example.agrione.adapter.CurrentWeatherAdapter
import com.example.agrione.adapter.WeatherAdapter
import com.example.agrione.databinding.FragmentWeatherBinding
import com.example.agrione.model.data.WeatherList
import com.example.agrione.viewmodel.WeatherListener
import com.example.agrione.viewmodel.WeatherViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class WeatherFragment : Fragment(), WeatherListener {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: WeatherAdapter
    private lateinit var adapter2: CurrentWeatherAdapter
    private lateinit var viewModel: WeatherViewModel
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()

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
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Weather Forecast"

        val city = viewModel.getCoordinates().value
        binding.weatherCity.text = city?.get(2).toString()
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

        binding.rcylrWeather.adapter = adapter
        binding.rcylrWeather.layoutManager = LinearLayoutManager(requireContext())

        binding.currentWeatherRcycl.adapter = adapter2
        binding.currentWeatherRcycl.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        coroutineScope.launch {
            try {
                viewModel.updateNewData()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching weather data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.newDataTrial.observe(viewLifecycleOwner) { weatherData ->
            if (weatherData != null) {
                val updatedList = mutableListOf<WeatherList>()
                for (a in 0..39) {
                    if (weatherData.list[a].dt_txt.slice(11..12) == "12") {
                        updatedList.add(weatherData.list[a])
                    }
                }
                adapter = WeatherAdapter(requireContext(), updatedList)
                binding.rcylrWeather.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
