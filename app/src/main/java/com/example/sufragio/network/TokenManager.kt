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
                val body = response.body()
                val newAccessToken = body?.access_token
                val newRefreshToken = body?.refresh_token

                if (newAccessToken != null && newRefreshToken != null) {
                    prefs.edit {
                        putString("access_token", newAccessToken)
                        putString("refresh_token", newRefreshToken)
                    }
                }

                newAccessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
