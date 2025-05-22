package com.example.sufragio.ui.mypage

data class VoteResponse(
    val poll_id: String,
    val votes: List<String>
)