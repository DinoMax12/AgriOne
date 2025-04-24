package com.project.agrione.view.dashboard

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.project.agrione.R
import com.project.agrione.adapter.DashboardEcomItemAdapter
import com.project.agrione.model.WeatherApi
import com.project.agrione.model.data.WeatherRootList
import com.project.agrione.utilities.CellClickListener
import com.project.agrione.view.articles.ArticleListFragment
import com.project.agrione.view.articles.FruitsFragment
import com.project.agrione.view.ecommerce.EcommerceItemFragment
import com.project.agrione.view.weather.WeatherFragment
import com.project.agrione.view.yojna.YojnaListFragment
import com.project.agrione.viewmodel.ArticleViewModel
import com.project.agrione.viewmodel.EcommViewModel
import com.project.agrione.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class dashboardFragment : Fragment(), CellClickListener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProviders.of(requireActivity())
            .get<WeatherViewModel>(WeatherViewModel::class.java)

        viewModel2 = ViewModelProviders.of(requireActivity())
            .get<EcommViewModel>(EcommViewModel::class.java)

        viewModel2.loadAllEcommItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel.getCoordinates().observe(viewLifecycleOwner, Observer {
            Log.d("DashFrag", it.toString())
            viewModel.updateNewData()
            val city = it.get(2) as String
            viewModel.newDataTrial.observe(viewLifecycleOwner, Observer {

                Log.d("Observed Here", "Yes")
                weathTempTextWeathFrag.text =
                    (it.list[0].main.temp - 273).toInt().toString() + "\u2103"
                humidityTextWeathFrag.text =
                    "Humidity: " + it!!.list[0].main.humidity.toString() + " %"
                windTextWeathFrag.text = "Wind: " + it!!.list[0].wind.speed.toString() + " km/hr"
                weatherCityTitle.text = city.toString()
                var iconcode = it!!.list[0].weather[0].icon
                var iconurl = "https://openweathermap.org/img/w/" + iconcode + ".png"
                Glide.with(activity!!.applicationContext).load(iconurl)
                    .into(weathIconImageWeathFrag)
            })
        })

        viewModel2.ecommLiveData.observe(viewLifecycleOwner, Observer {
            var itemsToShow = (0..it.size - 1).shuffled().take(4) as List<Int>
            val adapterEcomm =
                DashboardEcomItemAdapter(activity!!.applicationContext, it, itemsToShow, this)
            dashboardEcommRecycler.adapter = adapterEcomm
            dashboardEcommRecycler.layoutManager =
                GridLayoutManager(activity!!.applicationContext, 2)
        })

        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.title = "Agri India"

        weatherCard.setOnClickListener {
            weatherFragment = WeatherFragment()

            val transaction = activity!!.supportFragmentManager
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
            val transaction = activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, yojnaListFragment, "yojnaListFrag")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("yojnaListFrag")
                .commit()
        }

        cat5.setOnClickListener {
            articleListFragment = ArticleListFragment()
            val transaction = activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, articleListFragment, "articlesListFrag")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("articlesListFrag")
                .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            dashboardFragment().apply {
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

        val transaction = activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, ecommerceItemFragment, name)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .addToBackStack("name")
            .commit()
    }
}
