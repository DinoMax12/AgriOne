package com.example.agrione.view.dashboard

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.example.agrione.R
import com.example.agrione.adapter.DashboardEcomItemAdapter
import com.example.agrione.model.WeatherApi
import com.example.agrione.model.data.WeatherRootList
import com.example.agrione.utilities.CellClickListener
import com.example.agrione.view.articles.ArticleListFragment
import com.example.agrione.view.articles.FruitsFragment
import com.example.agrione.view.ecommerce.EcommerceItemFragment
import com.example.agrione.view.weather.WeatherFragment
import com.example.agrione.view.yojna.YojnaListFragment
import com.example.agrione.viewmodel.ArticleViewModel
import com.example.agrione.viewmodel.EcommViewModel
import com.example.agrione.viewmodel.WeatherViewModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class DashboardFragment : Fragment(), CellClickListener {

    private var param1: String? = null
    private var param2: String? = null
    lateinit var weatherFragment: WeatherFragment
    lateinit var fruitsFragment: FruitsFragment
    lateinit var yojnaListFragment: YojnaListFragment
    lateinit var articleListFragment: ArticleListFragment
    private lateinit var viewModel: WeatherViewModel
    private lateinit var viewModel2: EcommViewModel
    var data: WeatherRootList? = null
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var weathTempTextWeathFrag: TextView
    private lateinit var humidityTextWeathFrag: TextView
    private lateinit var windTextWeathFrag: TextView
    private lateinit var weatherCityTitle: TextView
    private lateinit var weathIconImageWeathFrag: ImageView
    private lateinit var weatherCard: CardView
    private lateinit var cat4: CardView
    private lateinit var cat5: CardView
    private lateinit var dashboardEcommRecycler: RecyclerView
    private lateinit var weatherProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(requireActivity())
            .get(WeatherViewModel::class.java)

        viewModel2 = ViewModelProvider(requireActivity())
            .get(EcommViewModel::class.java)

        viewModel2.loadAllEcommItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        
        // Initialize views
        weathTempTextWeathFrag = view.findViewById(R.id.weathTempTextWeathFrag)
        humidityTextWeathFrag = view.findViewById(R.id.humidityTextWeathFrag)
        windTextWeathFrag = view.findViewById(R.id.windTextWeathFrag)
        weatherCityTitle = view.findViewById(R.id.weatherCityTitle)
        weathIconImageWeathFrag = view.findViewById(R.id.weathIconImageWeathFrag)
        weatherCard = view.findViewById(R.id.weatherCard)
        cat4 = view.findViewById(R.id.cat4)
        cat5 = view.findViewById(R.id.cat5)
        dashboardEcommRecycler = view.findViewById(R.id.dashboardEcommRecycler)
        weatherProgressBar = view.findViewById(R.id.weatherProgressBar)

        // Show progress bar initially
        weatherProgressBar.visibility = View.VISIBLE
        weathTempTextWeathFrag.visibility = View.GONE
        humidityTextWeathFrag.visibility = View.GONE
        windTextWeathFrag.visibility = View.GONE
        weatherCityTitle.visibility = View.GONE
        weathIconImageWeathFrag.visibility = View.GONE

        viewModel.getCoordinates().observe(viewLifecycleOwner, Observer {
            Log.d("DashFrag", "Coordinates: $it")
            viewModel.updateNewData()
            val city = it.get(2) as String
            viewModel.newDataTrial.observe(viewLifecycleOwner, Observer {
                Log.d("WeatherData", "Raw data: ${it.list[0]}")
                
                // Hide progress bar and show weather data
                weatherProgressBar.visibility = View.GONE
                weathTempTextWeathFrag.visibility = View.VISIBLE
                humidityTextWeathFrag.visibility = View.VISIBLE
                windTextWeathFrag.visibility = View.VISIBLE
                weatherCityTitle.visibility = View.VISIBLE
                weathIconImageWeathFrag.visibility = View.VISIBLE

                // Convert temperature from Kelvin to Celsius
                val tempCelsius = (it.list[0].main.temp - 273.15).toInt()
                weathTempTextWeathFrag.text = "$tempCelsius\u2103"
                Log.d("WeatherData", "Temperature: $tempCelsius°C")

                // Humidity is already in percentage
                val humidity = it.list[0].main.humidity
                humidityTextWeathFrag.text = "Humidity: $humidity%"
                Log.d("WeatherData", "Humidity: $humidity%")

                // Convert wind speed from m/s to km/hr (1 m/s = 3.6 km/hr)
                val windSpeedMs = it.list[0].wind.speed
                val windSpeedKmh = (windSpeedMs * 3.6).toInt()
                windTextWeathFrag.text = "Wind: $windSpeedKmh km/hr"
                Log.d("WeatherData", "Wind speed: $windSpeedMs m/s = $windSpeedKmh km/hr")

                weatherCityTitle.text = city
                var iconcode = it.list[0].weather[0].icon
                var iconurl = "https://openweathermap.org/img/w/" + iconcode + ".png"
                Glide.with(requireContext()).load(iconurl)
                    .into(weathIconImageWeathFrag)
            })
        })

        viewModel2.ecommLiveData.observe(viewLifecycleOwner, Observer {
            var itemsToShow = (0..it.size - 1).shuffled().take(4) as List<Int>
            val adapterEcomm =
                DashboardEcomItemAdapter(requireContext(), it, itemsToShow, this)
            dashboardEcommRecycler.adapter = adapterEcomm
            dashboardEcommRecycler.layoutManager =
                GridLayoutManager(requireContext(), 2)
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Agri India"

        weatherCard.setOnClickListener {
            weatherFragment = WeatherFragment()

            val transaction = requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, weatherFragment, "name2")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("name")
                .commit()
            data?.let { it1 -> viewModel.messageToB(it1) }
        }

        cat4.setOnClickListener {
            yojnaListFragment = YojnaListFragment()
            val transaction = requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, yojnaListFragment, "yojnaListFrag")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("yojnaListFrag")
                .commit()
        }

        cat5.setOnClickListener {
            articleListFragment = ArticleListFragment()
            val transaction = requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, articleListFragment, "articlesListFrag")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("articlesListFrag")
                .commit()
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCellClickListener(name: String) {
        val ecommerceItemFragment = EcommerceItemFragment()

        val transaction = requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, ecommerceItemFragment, name)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .addToBackStack("name")
            .commit()
    }
}
