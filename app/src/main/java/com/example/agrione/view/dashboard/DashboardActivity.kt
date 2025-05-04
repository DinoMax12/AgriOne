package com.example.agrione.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.agrione.R
import com.example.agrione.databinding.ActivityDashboardBinding
import com.example.agrione.view.apmc.ApmcFragment
import com.example.agrione.view.articles.ArticleListFragment
import com.example.agrione.view.auth.LoginActivity
import com.example.agrione.view.ecommerce.*
import com.example.agrione.view.introscreen.IntroActivity
import com.example.agrione.view.socialmedia.SMCreatePostFragment
import com.example.agrione.view.socialmedia.SocialMediaPostsFragment
import com.example.agrione.view.weather.WeatherFragment
import com.example.agrione.viewmodel.UserDataViewModel
import com.example.agrione.viewmodel.UserProfilePostsViewModel
import com.example.agrione.viewmodel.WeatherViewModel
import com.example.agrione.view.user.UserFragment
import com.example.agrione.view.ecommerce.CartFragment
import com.example.agrione.view.ecommerce.EcommerceFragment
import com.example.agrione.view.ecommerce.PaymentFragment
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, com.google.android.gms.location.LocationListener {
    private val TEMP_BYPASS_INTRO = false

    private lateinit var binding: ActivityDashboardBinding
    lateinit var cartFragment: CartFragment
    lateinit var ecommerceItemFragment: EcommerceItemFragment
    lateinit var paymentFragment: PaymentFragment
    lateinit var dashboardFragment: DashboardFragment
    lateinit var ecommerceFragment: EcommerceFragment
    lateinit var weatherFragment: WeatherFragment
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var apmcFragment: ApmcFragment
    lateinit var articleListFragment: ArticleListFragment
    lateinit var myOrdersFragment: MyOrdersFragment
    lateinit var userFragment: UserFragment
    lateinit var socialMediaPostFragment: SocialMediaPostsFragment
    lateinit var smCreatePostFragment: SMCreatePostFragment
    private lateinit var viewModel: UserDataViewModel
    private lateinit var viewModel2: UserProfilePostsViewModel
    private lateinit var weatherViewModel: WeatherViewModel
    lateinit var sharedPreferences: SharedPreferences

    val firebaseFireStore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    var userName = ""
    var firstTime: Boolean? = null

    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 2 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private var isLocationUpdateInProgress = false
    private var lastLocationUpdateTime = 0L
    private val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components first
        initializeUI()

        // Only perform navigation checks if this is a fresh start
        if (savedInstanceState == null) {
            val currentUser = firebaseAuth.currentUser
            sharedPreferences = getSharedPreferences("AgrionePrefs", Context.MODE_PRIVATE)
            firstTime = sharedPreferences.getBoolean("firstTime", true)

            if (firstTime == true) {
                // First time user, show intro
                Intent(this, IntroActivity::class.java).also {
                    startActivity(it)
                }
                finish()
                return
            }

            if (currentUser == null) {
                // No user logged in, show login
                Intent(this, LoginActivity::class.java).also {
                    it.putExtra("from_dashboard", true)
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
                finish()
                return
            }

            // User is logged in, load user data
            viewModel.getUserData(currentUser.email as String)
        }
        
        coroutineScope.launch {
            delay(1000)
            initializeLocationServices()
        }
    }

    private fun initializeUI() {
        viewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        binding.userDataViewModel = viewModel

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        weatherViewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)
        viewModel2 = ViewModelProvider(this).get(UserProfilePostsViewModel::class.java)

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build()

        mGoogleApiClient!!.connect()

        buildGoogleApiClient()

        binding.navView.setNavigationItemSelectedListener(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Agrione"

        ecommerceItemFragment = EcommerceItemFragment()
        dashboardFragment = DashboardFragment()
        weatherFragment = WeatherFragment()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, dashboardFragment, "dashFrag")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setReorderingAllowed(true)
            .commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNavHome

        val headerView = binding.navView.getHeaderView(0)

        if (dashboardFragment.isVisible) {
            bottomNav.selectedItemId = R.id.bottomNavHome
        }

        headerView.setOnClickListener {
            Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show()

            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }

            userFragment = UserFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.frame_layout, userFragment, "userFrag")
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                setReorderingAllowed(true)
                addToBackStack("userFrag")
                commit()
            }
        }

        apmcFragment = ApmcFragment()
        socialMediaPostFragment = SocialMediaPostsFragment()
        ecommerceFragment = EcommerceFragment()
        paymentFragment = PaymentFragment()
        cartFragment = CartFragment()
        myOrdersFragment = MyOrdersFragment()

        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottomNavAPMC -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frame_layout, apmcFragment, "apmcFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("apmcFrag")
                        commit()
                    }
                }
                R.id.bottomNavHome -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frame_layout, dashboardFragment, "dashFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("dashFrag")
                        commit()
                    }
                }
                R.id.bottomNavEcomm -> {
                    ecommerceFragment = EcommerceFragment()
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frame_layout, ecommerceFragment, "ecommItemFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("ecommItemFrag")
                        commit()
                    }
                }
                R.id.bottomNavPost -> {
                    socialMediaPostFragment = SocialMediaPostsFragment()
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frame_layout, socialMediaPostFragment, "socialFrag")
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        setReorderingAllowed(true)
                        addToBackStack("socialFrag")
                        commit()
                    }
                }
            }
            true
        }

        if (!TEMP_BYPASS_INTRO) {
            viewModel.userliveData.observe(this, Observer {
                val headerView = binding.navView.getHeaderView(0)
                val posts = it.get("posts") as List<String>
                val city = it.get("city")
                userName = it.get("name").toString()

                if(city == null) {
                    headerView.findViewById<TextView>(R.id.cityTextNavHeader).text = "City: "
                } else {
                    headerView.findViewById<TextView>(R.id.cityTextNavHeader).text = "City: " + it.get("city").toString()
                }

                headerView.findViewById<TextView>(R.id.navbarUserName).text = userName
                headerView.findViewById<TextView>(R.id.navbarUserEmail).text = firebaseAuth.currentUser!!.email
                Glide.with(this).load(it.get("profileImage")).into(headerView.findViewById(R.id.navbarUserImage))

                it.getString("name")?.let { it1 -> Log.d("User Data from VM", it1) }

                headerView.findViewById<TextView>(R.id.navBarUserPostCount).text = "Posts Count: " + posts.size.toString()
            })
        }
    }

    private fun initializeLocationServices() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build()

        mGoogleApiClient!!.connect()
        buildGoogleApiClient()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (isLocationUpdateInProgress) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLocationUpdateTime < LOCATION_UPDATE_INTERVAL) {
            return
        }

        isLocationUpdateInProgress = true
        lastLocationUpdateTime = currentTime

        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    mLocation = mGoogleApiClient?.let { LocationServices.FusedLocationApi.getLastLocation(it) }

                    if (mLocation == null) {
                        startLocationUpdates()
                    } else {
                        updateLocationData()
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error getting location: ${e.message}")
            } finally {
                isLocationUpdateInProgress = false
            }
        }
    }

    private fun updateLocationData() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val coords = mutableListOf<String>()
                    val geocoder = Geocoder(this@DashboardActivity, Locale.getDefault())
                    val addresses: MutableList<Address>? = geocoder.getFromLocation(mLocation!!.latitude, mLocation!!.longitude, 1)

                    coords.add(mLocation!!.latitude.toString())
                    coords.add(mLocation!!.longitude.toString())
                    coords.add(addresses?.get(0)?.locality.toString())
                    
                    withContext(Dispatchers.Main) {
                        weatherViewModel.updateCoordinates(coords)
                        if (!isLocationUpdateInProgress) {
                            Toast.makeText(this@DashboardActivity, "Location detected", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error updating location data: ${e.message}")
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        mLocation = location
        updateLocationData()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(LOCATION_UPDATE_INTERVAL)
            .setFastestInterval(LOCATION_UPDATE_INTERVAL / 2)

        mGoogleApiClient?.let { 
            LocationServices.FusedLocationApi.requestLocationUpdates(it, mLocationRequest!!, this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            setReorderingAllowed(true)
            addToBackStack("name")
            commit()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.bottomNavHome
        when (item.itemId) {
            R.id.miItem1 -> {
                ecommerceFragment = EcommerceFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, ecommerceFragment, "ecommListFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("ecommListFrag")
                    .commit()
            }
            R.id.miItem2 -> {
                apmcFragment = ApmcFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, apmcFragment, "apmcFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("apmcFrag")
                    .commit()
            }
            R.id.miItem3 -> {
                smCreatePostFragment = SMCreatePostFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, smCreatePostFragment, "createPostFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("createPostFrag")
                    .commit()
            }
            R.id.miItem4 -> {
                socialMediaPostFragment = SocialMediaPostsFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, socialMediaPostFragment, "socialFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("socialFrag")
                    .commit()
            }
            R.id.miItem5 -> {
                weatherFragment = WeatherFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, weatherFragment, "weatherFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("weatherFrag")
                    .commit()
            }
            R.id.miItem6 -> {
                articleListFragment = ArticleListFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, articleListFragment, "articleListFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("articleListFrag")
                    .commit()
            }
            R.id.miItem7 -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, myOrdersFragment, "myOrdersFrag")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .addToBackStack("myOrdersFrag")
                    .commit()
            }
            R.id.miItem8 -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Log Out")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton("Yes") { dialogInterface, i ->
                        firebaseAuth.signOut()
                        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                        Intent(this, LoginActivity::class.java).also {
                            startActivity(it)
                        }
                    }
                    .setNegativeButton("No") { dialogInterface, i ->
                    }
                    .show()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun automatedClick() {
        if (!checkGPSEnabled()) {
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                checkLocationPermission()
            }
        } else {
            getLocation()
        }
    }

    override fun onClick(v: View?) {
        if (!checkGPSEnabled()) {
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                checkLocationPermission()
            }
        } else {
            getLocation()
        }
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build()

        mGoogleApiClient!!.connect()
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to use this app!")
            .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location Permissions!\nPlease accept to use location functionality.")
                    .setPositiveButton("OK") { dialog, which ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                    }
                    .create()
                    .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        automatedClick()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
        coroutineScope.launch {
            delay(2000)
            automatedClick()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient?.isConnected == true) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient!!, this)
            mGoogleApiClient!!.disconnect()
        }
    }
}