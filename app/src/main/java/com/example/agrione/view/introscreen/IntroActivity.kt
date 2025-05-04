package com.example.agrione.view.introscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.agrione.R
import com.example.agrione.adapter.IntroAdapter
import com.example.agrione.model.data.IntroData
import com.example.agrione.view.auth.LoginActivity
import com.example.agrione.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding

    private val introSliderAdapter = IntroAdapter(
        listOf(
            IntroData(
                "Welcome to the\nAgrione App",
                "Best Guide and Helper for any Farmer. Provides various features at one place!",
                R.drawable.intro_first
            ),
            IntroData(
                "Read Articles",
                "Read Online articles related to Farming Concepts, Technologies, and other useful knowledge.",
                R.drawable.intro_read
            ),
            IntroData(
                "Share Knowledge",
                "Social Media lets you share knowledge with other farmers!\nCreate your own posts using Image/Video/Texts.",
                R.drawable.intro_share
            ),
            IntroData(
                "E-Commerce",
                "Buy / Sell Agriculture related products & Manage your Cart Online",
                R.drawable.intro_ecomm
            ),
            IntroData(
                "Weather Forecast",
                "Get Notified for Daily Weather Conditions. 24x7 Data",
                R.drawable.intro_weather
            ),
            IntroData(
                "APMC Statistics",
                "Get updates on APMC Pricing and Commodity details every day.",
                R.drawable.intro_statistics
            ),
            IntroData(
                "Let's Grow Together",
                "- Agrione App",
                R.drawable.intro_help
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check SharedPreferences if user has already completed the intro
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("firstTime", true)

        if (!isFirstTime) {
            // If the user has already seen the intro, go directly to the login screen
            navigateToLogin()
            return
        }

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)

        binding.sliderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        if (binding.sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount) {
            binding.nextBtn.text = "Get Started"
        } else {
            binding.nextBtn.text = "Next"
        }

        binding.nextBtn.setOnClickListener {
            if (binding.sliderViewPager.currentItem + 1 < introSliderAdapter.itemCount) {
                binding.sliderViewPager.currentItem += 1
                binding.nextBtn.text = "Next"
                if (binding.sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount) {
                    binding.nextBtn.text = "Get Started"
                }
            } else {
                // Mark firstTime as false and navigate to the login screen
                val editor = sharedPreferences.edit()
                editor.putBoolean("firstTime", false)
                editor.apply()

                navigateToLogin()
            }
        }

        binding.skipIntro.setOnClickListener {
            // Mark firstTime as false and navigate to the login screen
            val editor = sharedPreferences.edit()
            editor.putBoolean("firstTime", false)
            editor.apply()

            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.apply {
                this.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this.layoutParams = layoutParams
            }

            binding.sliderballsContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.sliderballsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = binding.sliderballsContainer.get(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }

        if (index == introSliderAdapter.itemCount - 1) {
            binding.nextBtn.text = "Get Started"
        } else {
            binding.nextBtn.text = "Next"
        }
    }
}
