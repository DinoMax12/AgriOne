package com.project.agrione.view.apmc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.agrione.R
import com.project.agrione.adapter.ApmcAdapter
import com.project.agrione.databinding.FragmentApmcBinding
import java.text.SimpleDateFormat
import java.util.*

class ApmcFragment : Fragment() {

    private var _binding: FragmentApmcBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ApmcAdapter
    private var someMap: Map<Any, Array<String>>? = null
    private var states: Array<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApmcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressApmc.visibility = View.GONE
        binding.loadingTextApmc.visibility = View.GONE

        (activity as AppCompatActivity).supportActionBar?.title = "APMC"

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateValueTextApmc.text = sdf.format(Date())

        states = arrayOf(
            "None", "Andhra Pradesh", "Chandigarh", "Chattisgarh", "Gujarat", "Hariyana", "Himachal Pradesh",
            "Jammu & Kashmir", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Odisha",
            "Pudu Cherry", "Punjab", "Rajasthan", "Tamil Nadu", "Telangana", "Uttar Pradesh", "Uttarakhand", "West Bengal"
        )

        val districtInGujarat = arrayOf("None", "Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha", "Bharuch", "Bhavnagar", "Botad", "Chhota Udepur", "Dahod", "Dangs", "Devbhoomi Dwarka", "Gandhinagar", "Gir Somnath", "Jamnagar", "Junagadh", "Kachchh", "Kheda", "Mahisagar", "Mehsana", "Morbi", "Narmada", "Navsari", "Panchmahal", "Patan", "Porbandar", "Rajkot", "Sabarkantha", "Surat", "Surendranagar", "Tapi", "Vadodara", "Valsad")
        val districtInMaha = arrayOf("None", "Ahmednagar", "Akola", "Amravati", "Aurangabad", "Beed", "Bhandara", "Buldhana", "Chandrapur", "Dhule", "Gadchiroli", "Gondia", "Hingoli", "Jalgaon", "Jalna", "Kolhapur", "Latur", "Mumbai City", "Mumbai Suburban", "Nagpur", "Nanded", "Nandurbar", "Nashik", "Osmanabad", "Palghar", "Parbhani", "Pune", "Raigad", "Ratnagiri", "Sangli", "Satara", "Sindhudurg", "Solapur", "Thane", "Wardha", "Washim", "Yavatmal")
        val districtInRajasthan = arrayOf("None", "Ajmer", "Alwar", "Banswara", "Baran", "Barmer", "Bharatpur", "Bhilwara", "Bikaner", "Bundi", "Chittorgarh", "Churu", "Dausa", "Dholpur", "Dungarpur", "Hanumangarh", "Jaipur", "Jaisalmer", "Jalore", "Jhalawar", "Jhunjhunu", "Jodhpur", "Karauli", "Kota", "Nagaur", "Pali", "Pratapgarh", "Rajsamand", "Sawai Madhopur", "Sikar", "Sirohi", "Sri Ganganagar", "Tonk", "Udaipur")
        val districtInUttarPradesh = arrayOf("None", "Agra", "Aligarh", "Allahabad", "Ambedkar Nagar", "Amethi", "Amroha", "Auraiya", "Azamgarh", "Baghpat", "Bahraich", "Ballia", "Balrampur", "Banda", "Barabanki", "Bareilly", "Basti", "Bhadohi", "Bijnor", "Budaun", "Bulandshahr", "Chandauli", "Chitrakoot", "Deoria", "Etah", "Etawah", "Faizabad", "Farrukhabad", "Fatehpur", "Firozabad", "Gautam Buddha Nagar", "Ghaziabad", "Ghazipur", "Gonda", "Gorakhpur", "Hamirpur", "Hapur", "Hardoi", "Hathras", "Jalaun", "Jaunpur", "Jhansi", "Kannauj", "Kanpur Dehat", "Kanpur Nagar", "Kanshiram Nagar", "Kaushambi", "Kushinagar", "Lakhimpur - Kheri", "Lalitpur", "Lucknow", "Maharajganj", "Mahoba", "Mainpuri", "Mathura", "Mau", "Meerut", "Mirzapur", "Moradabad", "Muzaffarnagar", "Pilibhit", "Pratapgarh", "RaeBareli", "Rampur", "Saharanpur", "Sambhal", "Sant Kabir Nagar", "Shahjahanpur", "Shamali", "Shravasti", "Siddharth Nagar", "Sitapur", "Sonbhadra", "Sultanpur", "Unnao", "Varanasi")
        val districtInWestBengal = arrayOf("None", "Alipurduar", "Bankura", "Birbhum", "Cooch Behar", "Dakshin Dinajpur", "Darjeeling", "Hooghly", "Howrah", "Jalpaiguri", "Jhargram", "Kalimpong", "Kolkata", "Malda", "Murshidabad", "Nadia", "North 24 Parganas", "Paschim Medinipur", "Paschim Burdwan", "Purba Burdwan", "Purba Medinipur", "Purulia", "South 24 Parganas", "Uttar Dinajpur")
        val districtInKerala = arrayOf("None", "Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod", "Kollam", "Kottayam", "Kozhikode", "Malappuram", "Palakkad", "Pathanamthitta", "Thiruvananthapuram", "Thrissur", "Wayanad")
        val districtInAndhraPradesh = arrayOf("None", "Anantapur", "Chittoor", "East Godavari", "Guntur", "Krishna", "Kurnool", "Prakasam", "Srikakulam", "Sri Potti Sriramulu Nellore", "Visakhapatnam", "Vizianagaram", "West Godavari", "Kadapa")

        val emptyDistricts = arrayOf("None")

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            states!!
        )
        binding.spinner1.adapter = aa

        someMap = mapOf(
            "Andhra Pradesh" to districtInAndhraPradesh,
            "Gujarat" to districtInGujarat,
            "Kerala" to districtInKerala,
            "Maharashtra" to districtInMaha,
            "Rajasthan" to districtInRajasthan,
            "Uttar Pradesh" to districtInUttarPradesh,
            "West Bengal" to districtInWestBengal,
            "Chandigarh" to emptyDistricts,
            "Chattisgarh" to emptyDistricts,
            "Hariyana" to emptyDistricts,
            "Himachal Pradesh" to emptyDistricts,
            "Jammu & Kashmir" to emptyDistricts,
            "Jharkhand" to emptyDistricts,
            "Karnataka" to emptyDistricts,
            "Madhya Pradesh" to emptyDistricts,
            "Odisha" to emptyDistricts,
            "Pudu Cherry" to emptyDistricts,
            "Punjab" to emptyDistricts,
            "Tamil Nadu" to emptyDistricts,
            "Telangana" to emptyDistricts,
            "Uttarakhand" to emptyDistricts
            // Add rest here if needed
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
