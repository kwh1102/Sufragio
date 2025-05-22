package com.example.sufragio.ui.login_register

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sufragio.MainActivity
import com.example.sufragio.R
import com.example.sufragio.SplashActivity
import com.example.sufragio.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_login)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val idInput = findViewById<EditText>(R.id.idInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val goToRegister = findViewById<TextView>(R.id.goToRegister)

        val loginInfoTextView = findViewById<TextView>(R.id.loginInfo)

        val showLoginInfo = intent.getBooleanExtra(SplashActivity.EXTRA_SHOW_LOGIN_INFO, false)

        if (showLoginInfo) {
            loginInfoTextView.visibility = View.VISIBLE
        }

        loginButton.setOnClickListener {
            val id = idInput.text.toString()
            val password = passwordInput.text.toString()

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.loginUser(id, password)
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val accessToken = responseBody?.access_token
                        val refreshToken = responseBody?.refresh_token

                        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("access_token", accessToken)
                            putString("refresh_token", refreshToken)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "아이디 또는 비밀번호가 일치하지 않습니다..",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }



        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}