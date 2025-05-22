package com.example.sufragio.ui.detail

data class PollOptionResponse(
    val option_text: String,
    val id: String,
    val user_id: String,
    val poll_id: String,
    val created_at: String
)
