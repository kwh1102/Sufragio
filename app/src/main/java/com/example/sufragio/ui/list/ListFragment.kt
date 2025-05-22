package com.example.sufragio.ui.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R
import com.example.sufragio.network.RetrofitClient
import com.example.sufragio.ui.detail.VoteDetailActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var voteAdapter: VoteAdapter
    private lateinit var voteList: List<Vote>

    private val voteDetailResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchVotes { allVotes ->
                voteList = allVotes
                val tabLayout = requireView().findViewById<TabLayout>(R.id.tabLayout)
                filterVotes(tabLayout.selectedTabPosition)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

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

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = voteAdapter

        tabLayout.addTab(tabLayout.newTab().setText("진행 중"))
        tabLayout.addTab(tabLayout.newTab().setText("종료됨"))

        fetchVotes { allVotes ->
            voteList = allVotes
            filterVotes(0)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterVotes(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }

    private fun filterVotes(tabPosition: Int) {
        val filtered = when (tabPosition) {
            0 -> voteList.filter { isAfterToday(it.deadline) }
            else -> voteList.filter { !isAfterToday(it.deadline) }
        }
        voteAdapter.submitList(filtered)
    }

    private fun fetchVotes(onResult: (List<Vote>) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAllPolls()
                if (response.isSuccessful) {
                    val allPolls = response.body().orEmpty()
                    val enrichedVotes = allPolls.mapNotNull { poll ->
                        val split = poll.title.split("$")
                        if (split.size != 2) return@mapNotNull null

                        val title = split[0]
                        val deadline = split[1]

                        val resultsResponse = RetrofitClient.instance.getPollResults(poll.id)
                        var participants = 0

                        if (resultsResponse.isSuccessful) {
                            val resultBody = resultsResponse.body()
                            val json = resultBody?.string()
                            if (json != null) {
                                val jsonObject = JSONObject(json)
                                participants = if (poll.is_anonymous) {
                                    jsonObject.keys().asSequence().map { key ->
                                        jsonObject.optInt(key, 0)
                                    }.sum()
                                } else {
                                    val userIds = mutableSetOf<String>()
                                    jsonObject.keys().asSequence().forEach { key ->
                                        val array = jsonObject.optJSONArray(key)
                                        for (i in 0 until array.length()) {
                                            userIds.add(array.getString(i))
                                        }
                                    }
                                    userIds.size
                                }
                            }
                        }

                        Vote(
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
                    onResult(enrichedVotes)
                } else {
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(emptyList())
            }
        }
    }
}

private fun isAfterToday(dateString: String): Boolean {
    return try {
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = formatter.parse(dateString)
        val today = Date()
        date != null && date.after(today)
    } catch (e: Exception) {
        false
    }
}