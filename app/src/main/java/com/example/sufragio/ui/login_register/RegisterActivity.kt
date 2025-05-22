package com.example.sufragio.ui.login_register

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
import com.example.sufragio.R
import com.example.sufragio.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_register)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val idInput = findViewById<EditText>(R.id.idInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val nicknameInput = findViewById<EditText>(R.id.nicknameInput)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        registerButton.setOnClickListener {
            val id = idInput.text.toString()
            val password = passwordInput.text.toString()
            val nickname = nicknameInput.text.toString()

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.registerUser(id, password, nickname)
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "비밀번호 입력 조건을 확인해 주세요!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
