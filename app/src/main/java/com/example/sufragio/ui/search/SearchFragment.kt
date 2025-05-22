package com.example.sufragio.ui.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R
import com.example.sufragio.network.RetrofitClient
import com.example.sufragio.ui.detail.VoteDetailActivity
import com.example.sufragio.ui.list.Vote
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.sequences.forEach

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter

    private lateinit var voteList: List<VoteSearchData>

    private val voteDetailResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchVotesAndFilter()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.searchResultRecyclerView)

        val onItemClick: (VoteSearchData) -> Unit = { vote ->
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

        adapter = SearchAdapter(emptyList(), onItemClick)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filterVotes(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchVotesAndFilter()

        return view
    }

    private fun fetchVotesAndFilter() {
        lifecycleScope.launch {
            fetchVotes { allVotes ->
                voteList = allVotes
                filterVotes(searchEditText.text.toString())
            }
        }
    }

    private fun filterVotes(query: String) {
        val filtered = voteList.filter {
            it.title.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }

    private fun fetchVotes(onResult: (List<VoteSearchData>) -> Unit) {
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

                        VoteSearchData(
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