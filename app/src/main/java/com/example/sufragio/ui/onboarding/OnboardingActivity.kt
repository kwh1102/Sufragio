package com.example.sufragio.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.sufragio.MainActivity
import com.example.sufragio.R
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import androidx.core.content.edit
import com.example.sufragio.ui.login_register.LoginActivity
import com.example.sufragio.ui.login_register.RegisterActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        dotsIndicator = findViewById(R.id.dotsIndicator)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)

        val layouts = listOf(
            R.layout.onboarding_page_1,
            R.layout.onboarding_page_2,
            R.layout.onboarding_page_3
        )

        val adapter = OnboardingAdapter(this, layouts)
        viewPager.adapter = adapter
        dotsIndicator.setViewPager(viewPager)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                loginButton.visibility = if (position == layouts.size - 1) Button.VISIBLE else Button.GONE
                signupButton.visibility = if (position == layouts.size - 1) Button.VISIBLE else Button.GONE
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        loginButton.setOnClickListener {
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                putBoolean("onboardingShown", true)
            }
            startActivity(Intent(this, LoginActivity::class.java))
        }
        signupButton.setOnClickListener {
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                putBoolean("onboardingShown", true)
            }
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
