package com.project.agrione.view.introscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.project.agrione.R
import com.project.agrione.adapter.IntroAdapter
import com.project.agrione.model.data.IntroData
import com.project.agrione.view.auth.LoginActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    private val introSliderAdapter = IntroAdapter(
        listOf(
            IntroData(
                "Welcome to the\n\bAgrione App\b",
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
        setContentView(R.layout.activity_intro)

        sliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)

        sliderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        if (sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount) {
            nextBtn.text = "Get Started"
        } else {
            nextBtn.text = "Next"
        }

        nextBtn.setOnClickListener {
            if (sliderViewPager.currentItem + 1 < introSliderAdapter.itemCount) {
                sliderViewPager.currentItem += 1
                nextBtn.text = "Next"
                if (sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount) {
                    nextBtn.text = "Get Started"
                }
            } else {
                Intent(this, LoginActivity::class.java).also {
                    startActivity(it)
                }
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("firstTime", false)
                editor.apply()
                finish()
            }
        }

        skipIntro.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
            }
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("firstTime", false)
            editor.apply()
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

            sliderballs_container.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = sliderballs_container.childCount
        for (i in 0 until childCount) {
            val imageView = sliderballs_container.get(i) as ImageView
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
            nextBtn.text = "Get Started"
        } else {
            nextBtn.text = "Next"
        }
    }
}
