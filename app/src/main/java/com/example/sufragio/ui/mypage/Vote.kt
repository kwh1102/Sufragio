package com.example.sufragio.ui.mypage

data class Vote(
    val title: String,
    val description: String,
    val participants: Int,
    val deadline: String,
    val id: String,
    val userId: String,
    val is_anonymous: Boolean,
    val is_multiple_choice: Boolean,
    val is_option_add_allowed: Boolean,
    val created_at: String
)
