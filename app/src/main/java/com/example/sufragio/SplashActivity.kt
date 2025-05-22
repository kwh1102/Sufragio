package com.example.sufragio

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sufragio.network.TokenManager
import com.example.sufragio.ui.login_register.LoginActivity
import com.example.sufragio.ui.onboarding.OnboardingActivity
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SHOW_LOGIN_INFO = "com.example.sufragio.SHOW_LOGIN_INFO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val onboardingShown = prefs.getBoolean("onboardingShown", false)
            val accessToken = prefs.getString("access_token", null)

            if (!onboardingShown) {
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
                return@postDelayed
            }

            lifecycleScope.launch {
                if (accessToken.isNullOrEmpty()) {
                    moveToLogin()
                } else {
                    val newToken = TokenManager.refreshAccessTokenIfNeeded(this@SplashActivity)
                    if (newToken.isNullOrEmpty()) {
                        moveToLogin()
                    } else {
                        moveToMain()
                    }
                }
            }
        }, 1500)
    }

    private fun moveToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(EXTRA_SHOW_LOGIN_INFO, true)
        }
        startActivity(intent)
        finish()
    }

    private fun moveToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
