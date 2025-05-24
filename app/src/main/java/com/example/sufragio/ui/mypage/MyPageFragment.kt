package com.example.sufragio.ui.mypage

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R
import com.example.sufragio.SplashActivity.Companion.EXTRA_SHOW_LOGIN_INFO
import com.example.sufragio.network.RetrofitClient
import com.example.sufragio.network.TokenManager
import com.example.sufragio.ui.detail.VoteDetailActivity
import com.example.sufragio.ui.login_register.LoginActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.json.JSONObject

class MyPageFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var voteAdapter: VoteAdapter
    private lateinit var nicknameTextView: TextView

    private val createdVotes = mutableListOf<Vote>()
    private val joinedVotes = mutableListOf<Vote>()
    private var myUserId: String? = null

    private val voteDetailResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchVotesAndUserInfo()
            val currentTabPosition = tabLayout.selectedTabPosition
            tabLayout.selectTab(tabLayout.getTabAt(currentTabPosition))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        tabLayout = view.findViewById(R.id.myTabLayout)
        recyclerView = view.findViewById(R.id.recyclerMyVotes)
        val logoutButton: Button = view.findViewById(R.id.buttonLogout)
        val editNicknameButton: Button = view.findViewById(R.id.buttonEditNickname)
        nicknameTextView = view.findViewById(R.id.textNickname)

        voteAdapter = VoteAdapter { vote ->
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

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = voteAdapter

        tabLayout.addTab(tabLayout.newTab().setText("내 투표"))
        tabLayout.addTab(tabLayout.newTab().setText("참여한 투표"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                voteAdapter.submitList(if (tab?.position == 0) createdVotes else joinedVotes)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        logoutButton.setOnClickListener {
            requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                clear()
                apply()
                putBoolean("onboardingShown", true)
            }

            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(EXTRA_SHOW_LOGIN_INFO, true)
            }
            startActivity(intent)
        }

        editNicknameButton.setOnClickListener {
            showEditNicknameDialog()
        }

        fetchVotesAndUserInfo()

        return view
    }

    private fun fetchVotesAndUserInfo() {
        lifecycleScope.launch {
            val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var token = prefs.getString("access_token", null) ?: return@launch
            var authHeader = "Bearer $token"

            var response = RetrofitClient.instance.getUserInfo(authHeader)
            if (!response.isSuccessful) {
                token = TokenManager.refreshAccessTokenIfNeeded(requireContext()) ?: return@launch
                authHeader = "Bearer $token"
                response = RetrofitClient.instance.getUserInfo(authHeader)
            }

            if (!response.isSuccessful) return@launch
            val userInfo = response.body() ?: return@launch
            myUserId = userInfo.id
            nicknameTextView.text = userInfo.nickname

            val pollResponse = RetrofitClient.instance.getAllPolls()
            if (!pollResponse.isSuccessful) return@launch

            val polls = pollResponse.body() ?: return@launch
            createdVotes.clear()
            joinedVotes.clear()

            for (poll in polls) {
                val split = poll.title.split("$")
                if (split.size != 2) continue

                val title = split[0]
                val deadline = split[1]

                var participants = 0
                val resultResponse = RetrofitClient.instance.getPollResults(poll.id)
                if (resultResponse.isSuccessful) {
                    val resultBody = resultResponse.body()?.string()
                    if (resultBody != null) {
                        val json = JSONObject(resultBody)
                        participants = if (poll.is_anonymous) {
                            json.keys().asSequence().sumOf { key -> json.optInt(key, 0) }
                        } else {
                            val userIds = mutableSetOf<String>()
                            json.keys().asSequence().forEach { key ->
                                val arr = json.optJSONArray(key)
                                for (i in 0 until arr.length()) {
                                    userIds.add(arr.getString(i))
                                }
                            }
                            userIds.size
                        }
                    }
                }

                val vote = Vote(
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

                if (poll.user_id == myUserId) {
                    createdVotes.add(vote)
                }

                val voteResp = RetrofitClient.instance.getVotesForPoll(poll.id, authHeader)
                if (voteResp.isSuccessful && !voteResp.body()?.votes.isNullOrEmpty()) {
                    joinedVotes.add(vote)
                }
            }
            voteAdapter.submitList(createdVotes)
        }
    }

    private fun showEditNicknameDialog() {
        val context = requireContext()

        val displayMetrics = resources.displayMetrics
        val widthInPx = (300 * displayMetrics.density).toInt()

        val editText = EditText(context).apply {
            hint = "새 닉네임 입력"
            layoutParams = LinearLayout.LayoutParams(widthInPx, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 40, 50, 10)
            addView(editText)
        }

        AlertDialog.Builder(context)
            .setTitle("닉네임 수정")
            .setView(container)
            .setPositiveButton("확인") { _, _ ->
                val newNickname = editText.text.toString().trim()
                if (newNickname.isNotEmpty()) {
                    updateNickname(newNickname)
                } else {
                    Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateNickname(newNickname: String) {
        lifecycleScope.launch {
            val context = requireContext()
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var accessToken = prefs.getString("access_token", null)

            if (accessToken.isNullOrEmpty()) {
                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            var response = RetrofitClient.instance.updateNickname(newNickname, "Bearer $accessToken")

            if (!response.isSuccessful) {
                accessToken = TokenManager.refreshAccessTokenIfNeeded(context)
                if (accessToken != null) {
                    prefs.edit { putString("access_token", accessToken) }
                    response = RetrofitClient.instance.updateNickname(newNickname, "Bearer $accessToken")
                }
            }

            if (response.isSuccessful) {
                Toast.makeText(context, "닉네임이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                nicknameTextView.text = newNickname
                prefs.edit { putString("nickname", newNickname) }
            } else {
                Toast.makeText(context, "닉네임 수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}