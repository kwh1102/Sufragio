package com.example.sufragio.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sufragio.MainActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingShown = prefs.getBoolean("onboardingShown", false)

        if (onboardingShown) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        finish()
    }
}