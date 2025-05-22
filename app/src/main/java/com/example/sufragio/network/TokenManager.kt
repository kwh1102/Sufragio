package com.example.sufragio.network

import android.content.Context
import androidx.core.content.edit

object TokenManager {
    suspend fun refreshAccessTokenIfNeeded(context: Context): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val refreshToken = prefs.getString("refresh_token", null) ?: return null

        return try {
            val response = RetrofitClient.instance.refreshAccessToken(refreshToken)
            if (response.isSuccessful) {
                val newAccessToken = response.body()?.access_token
                prefs.edit { putString("access_token", newAccessToken) }
                newAccessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

