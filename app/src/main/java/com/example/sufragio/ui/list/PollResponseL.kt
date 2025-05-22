package com.example.sufragio.ui.list

data class PollResponseL(
    val title: String,
    val description: String,
    val is_anonymous: Boolean,
    val is_option_add_allowed: Boolean,
    val user_id: String,
    val is_multiple_choice: Boolean,
    val id: String,
    val is_revoting_allowed: Boolean,
    val created_at: String
)
