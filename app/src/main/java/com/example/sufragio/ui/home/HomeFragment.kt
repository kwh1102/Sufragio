package com.example.sufragio.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R
import com.example.sufragio.network.RetrofitClient
import com.example.sufragio.network.TokenManager
import com.example.sufragio.ui.detail.VoteDetailActivity
import com.example.sufragio.ui.login_register.LoginActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var popularRecycler: RecyclerView
    private lateinit var recentRecycler: RecyclerView
    private lateinit var dueRecycler: RecyclerView
    private lateinit var nicknameTextView: TextView

    private val voteDetailResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchAndDisplayContent(isInitialLoad = false)
        }
    }

    private val onItemClick: (VoteItem) -> Unit = { vote ->
        val intent = Intent(requireContext(), VoteDetailActivity::class.java).apply {
            putExtra("title", vote.title)
            putExtra("description", vote.description)
            putExtra("participants", vote.participants)
            putExtra("deadline", vote.deadline)
            putExtra("poll_id", vote.id)
            putExtra("poll_user_id", vote.userId)
            putExtra("is_anonymous", vote.is_anonymous)
            putExtra("is_multiple_choice", vote.is_multiple_choice)
            putExtra("is_option_add_allowed", vote.is_option_add_allowed)
            putExtra("created_at", vote.created_at)
        }
        voteDetailResultLauncher.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        popularRecycler = view.findViewById(R.id.popularVoteRecyclerView)
        recentRecycler = view.findViewById(R.id.recentVoteRecyclerView)
        dueRecycler = view.findViewById(R.id.dueVoteRecyclerView)
        nicknameTextView = view.findViewById(R.id.welcomeText)

        popularRecycler.layoutManager = LinearLayoutManager(requireContext())
        recentRecycler.layoutManager = LinearLayoutManager(requireContext())
        dueRecycler.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchAndDisplayContent(isInitialLoad = true)

    }

    private fun fetchAndDisplayContent(isInitialLoad: Boolean) {
        lifecycleScope.launch {
            try {
                val userInfoJob = async { handleUserInfo() }
                val voteLoadJob = async { loadVotes(onItemClick) }

                userInfoJob.await()
                voteLoadJob.await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleUserInfo() {
        val context = requireContext()
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        var accessToken = prefs.getString("access_token", null)

        if (accessToken.isNullOrEmpty()) {
            nicknameTextView.text = "로그인 필요"
            return
        }

        var response = RetrofitClient.instance.getUserInfo("Bearer $accessToken")

        if (!response.isSuccessful) {
            accessToken = TokenManager.refreshAccessTokenIfNeeded(context)
            if (accessToken != null) {
                prefs.edit { putString("access_token", accessToken) }
                response = RetrofitClient.instance.getUserInfo("Bearer $accessToken")
            }
        }

        if (response.isSuccessful) {
            val nickname = response.body()?.nickname
            nicknameTextView.text = "수프라지우에 오신 ${nickname}님 환영합니다!"
        } else {
            nicknameTextView.text = "닉네임 없음"
            startActivity(Intent(context, LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun isAfterToday(dateStr: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
            val date = formatter.parse(dateStr)
            date?.after(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun loadVotes(onItemClick: (VoteItem) -> Unit) {
        try {
            val response = RetrofitClient.instance.getAllPolls()
            if (!response.isSuccessful) return

            val allPolls = response.body().orEmpty()

            val voteItems = allPolls.mapNotNull { poll ->
                val parts = poll.title.split("$")
                if (parts.size != 2) return@mapNotNull null
                val title = parts[0]
                val deadline = parts[1]

                if (!isAfterToday(deadline)) return@mapNotNull null

                lifecycleScope.async {
                    var participants = 0
                    val resultResp = RetrofitClient.instance.getPollResults(poll.id)
                    if (resultResp.isSuccessful) {
                        val resultJson = resultResp.body()?.string()
                        if (resultJson != null) {
                            val obj = JSONObject(resultJson)
                            participants = if (poll.is_anonymous) {
                                obj.keys().asSequence().map { obj.optInt(it, 0) }.sum()
                            } else {
                                val users = mutableSetOf<String>()
                                obj.keys().asSequence().forEach { key ->
                                    val arr = obj.optJSONArray(key)
                                    for (i in 0 until arr.length()) {
                                        users.add(arr.getString(i))
                                    }
                                }
                                users.size
                            }
                        }
                    }

                    VoteItem(
                        title = title,
                        description = poll.description,
                        participants = participants,
                        deadline = deadline,
                        id = poll.id,
                        userId = poll.user_id,
                        is_anonymous = poll.is_anonymous,
                        is_multiple_choice = poll.is_multiple_choice,
                        is_option_add_allowed = poll.is_option_add_allowed,
                        created_at = poll.created_at
                    )
                }
            }.awaitAll()

            val popular = voteItems.sortedByDescending { it.participants }.take(2)
            val recent = voteItems.sortedByDescending { it.created_at }.take(2)
            val dueSoon = voteItems.sortedBy { it.deadline }.take(2)

            popularRecycler.adapter = VoteAdapter(popular, onItemClick)
            recentRecycler.adapter = VoteAdapter(recent, onItemClick)
            dueRecycler.adapter = VoteAdapter(dueSoon, onItemClick)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}