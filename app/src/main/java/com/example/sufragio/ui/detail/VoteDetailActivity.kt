package com.example.sufragio.ui.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart
import androidx.lifecycle.lifecycleScope
import com.example.sufragio.R
import com.example.sufragio.network.RetrofitClient
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch

class VoteDetailActivity : AppCompatActivity() {

    private lateinit var authToken: String
    private lateinit var btnDelVote: Button
    private lateinit var addOptionBtn: Button
    private var pollId: String? = null
    private var pollUserId: String? = null

    private var isMultipleChoice = false
    private var isAnonymous = false
    private var isOptionAddAllowed= false
    private val selectedOptionIds = mutableListOf<String>()


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_vote_detail)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        authToken = sharedPref.getString("access_token", "") ?: ""

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnDelVote = findViewById(R.id.btnDelVote)
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val participants = intent.getIntExtra("participants", 0)
        val deadline = intent.getStringExtra("deadline")
        pollId = intent.getStringExtra("poll_id")
        pollUserId = intent.getStringExtra("poll_user_id")

        findViewById<TextView>(R.id.textDetailTitle).text = title
        findViewById<TextView>(R.id.textDetailDescription).text = description
        if (description == "") {
            findViewById<TextView>(R.id.textDetailDescription).visibility = View.GONE
        }


        val creatorTextView = findViewById<TextView>(R.id.textDetailCreator)
        if (!pollUserId.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val userResp = RetrofitClient.instance.getUserNickname(pollUserId!!, "Bearer $authToken")
                    if (userResp.isSuccessful) {
                        val nickname = userResp.body()?.nickname ?: "알 수 없음"
                        val created_at = intent.getStringExtra("created_at")?.split("T")[0]?.replace("-", ".") ?: "알 수 없음"
                        creatorTextView.text = "$nickname • $created_at"
                    } else {
                        creatorTextView.text = "생성자 알 수 없음"
                    }
                } catch (e: Exception) {
                    creatorTextView.text = "생성자 알 수 없음"
                }
            }
        } else {
            creatorTextView.text = "생성자 알 수 없음"
        }

        findViewById<TextView>(R.id.textDetailParticipants).text = "참여자 ${participants}명"
        findViewById<TextView>(R.id.textDetailDeadline).text = "마감일: $deadline"

        isAnonymous = intent.getBooleanExtra("is_anonymous", false)
        isMultipleChoice = intent.getBooleanExtra("is_multiple_choice", false)
        isOptionAddAllowed = intent.getBooleanExtra("is_option_add_allowed", false)
        findViewById<TextView>(R.id.textDetailMultiple).text = if (isMultipleChoice) { "다중 선택 가능" } else { "단일 선택" }

        if (isOptionAddAllowed) {
            addOptionBtn = findViewById<Button>(R.id.btnAddOption)
            addOptionBtn.visibility = View.VISIBLE
            addOptionBtn.setOnClickListener {
                showAddOptionDialog()
            }
        } else {
            findViewById<Button>(R.id.btnAddOption).visibility = View.GONE
        }

        if (!isAfterToday(deadline)) {
            pollId?.let { showPollResults(it) }

            findViewById<LinearLayout>(R.id.voteOptionGroupContainer).visibility = View.GONE
            findViewById<Button>(R.id.btnSubmitVote).visibility = View.GONE
            findViewById<TextView>(R.id.textDetailEnd).visibility = View.VISIBLE

            checkOwnershipAndSetDeleteButton()

            return
        }


        checkIfUserVoted()
        loadPollOptions()

        findViewById<Button>(R.id.btnSubmitVote).setOnClickListener {
            if (selectedOptionIds.isEmpty()) {
                Toast.makeText(this, "옵션을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val response = RetrofitClient.instance.votePoll(pollId!!, selectedOptionIds, "Bearer $authToken")
                if (response.isSuccessful) {
                    Toast.makeText(this@VoteDetailActivity, "투표 완료!", Toast.LENGTH_SHORT).show()
                    pollId?.let { id ->
                        showPollResults(id)
                    }
                } else {
                    Toast.makeText(this@VoteDetailActivity, "투표 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        checkOwnershipAndSetDeleteButton()
    }

    private fun checkIfUserVoted() {
        pollId?.let { id ->
            lifecycleScope.launch {
                val voteResponse = RetrofitClient.instance.getVotesForPoll(id, "Bearer $authToken")
                if (voteResponse.isSuccessful) {
                    val voteData = voteResponse.body()
                    if (voteData != null && voteData.votes.isNotEmpty()) {
                        showPollResults(id)
                    }
                }
            }
        }
    }

    private fun showAddOptionDialog() {
        val context = this

        val displayMetrics = resources.displayMetrics
        val widthInPx = (300 * displayMetrics.density).toInt()

        val editText = EditText(context).apply {
            hint = "새 항목 입력"
            layoutParams = LinearLayout.LayoutParams(widthInPx, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 40, 50, 10)
            addView(editText)
        }

        AlertDialog.Builder(context)
            .setTitle("항목 추가")
            .setView(container)
            .setPositiveButton("추가") { _, _ ->
                val optionText = editText.text.toString().trim() + " (추가됨)"
                if (optionText.isNotEmpty()) {
                    addOptionToPoll(optionText)
                } else {
                    Toast.makeText(context, "항목 내용이 비어있습니다", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun addOptionToPoll(optionText: String) {
            pollId?.let { id ->
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.addPollOption(id, optionText, "Bearer $authToken")
                        if (response.isSuccessful) {
                            Toast.makeText(this@VoteDetailActivity, "항목이 추가되었습니다", Toast.LENGTH_SHORT).show()
                            loadPollOptions()
                        } else {
                            Toast.makeText(this@VoteDetailActivity, "추가 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@VoteDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        private fun showPollResults(pollId: String) {
        val optionContainer = findViewById<LinearLayout>(R.id.voteOptionGroupContainer)
        val resultContainer = findViewById<LinearLayout>(R.id.voteResultContainer)
        val submitButton = findViewById<Button>(R.id.btnSubmitVote)
        val barChart = findViewById<BarChart>(R.id.barChart)

        optionContainer.visibility = View.GONE
        submitButton.visibility = View.GONE
        resultContainer.visibility = View.VISIBLE
        barChart.visibility = View.VISIBLE

        lifecycleScope.launch {
            val optionsResp = RetrofitClient.instance.getPollOptions(pollId)
            val resultResp = RetrofitClient.instance.getPollResults(pollId)

            if (optionsResp.isSuccessful && resultResp.isSuccessful) {
                val optionList = optionsResp.body() ?: emptyList()
                val resultJson = resultResp.body()?.string() ?: "{}"
                val jsonObj = org.json.JSONObject(resultJson)

                val resultLabels = mutableListOf<String>()
                val resultCounts = mutableListOf<Int>()

                val optionVotesMap = mutableMapOf<String, Int>()
                val optionVotersMap = mutableMapOf<String, List<String>>()

                for (option in optionList) {
                    val optionId = option.id
                    val label = option.option_text

                    val count: Int
                    val nicknames = mutableListOf<String>()

                    if (jsonObj.has(optionId)) {
                        val value = jsonObj.get(optionId)
                        when (value) {
                            is Int -> {
                                count = value
                            }
                            is org.json.JSONArray -> {
                                count = value.length()
                                for (i in 0 until value.length()) {
                                    val userId = value.getString(i)
                                    val userResp = RetrofitClient.instance.getUserNickname(userId, "Bearer $authToken")
                                    if (userResp.isSuccessful) {
                                        userResp.body()?.let { userInfo ->
                                            nicknames.add(userInfo.nickname)
                                        }
                                    } else {
                                        nicknames.add("알 수 없음")
                                    }
                                }
                            }
                            else -> {
                                count = 0
                            }
                        }
                    } else {
                        count = 0
                    }

                    optionVotesMap[label] = count
                    optionVotersMap[label] = nicknames
                }

                val totalVotes = optionVotesMap.values.sum().takeIf { it > 0 } ?: 1

                resultContainer.removeAllViews()

                for (option in optionList) {
                    val label = option.option_text
                    val count = optionVotesMap[label] ?: 0
                    val nicknames = optionVotersMap[label] ?: emptyList()
                    val percent = (count * 100.0 / totalVotes)

                    val resultText = TextView(this@VoteDetailActivity).apply {
                        text = "$label: ${count}명 (${String.format("%.1f", percent)}%)"
                        textSize = 16f
                        setPadding(0, 8, 0, 4)
                    }
                    resultContainer.addView(resultText)

                    if (nicknames.isNotEmpty()) {
                        val votersText = TextView(this@VoteDetailActivity).apply {
                            textSize = 14f
                            text = "투표한 사람: ${nicknames.joinToString(", ")}"
                            setPadding(0, 0, 0, 16)
                            setTextColor(resources.getColor(R.color.purple))
                        }
                        resultContainer.addView(votersText)
                    } else {
                        if (isAnonymous) {
                            val votersText = TextView(this@VoteDetailActivity).apply {
                                textSize = 14f
                                text = "투표한 사람: 비공개"
                                setPadding(0, 0, 0, 16)
                                setTextColor(resources.getColor(R.color.purple))
                            }
                            resultContainer.addView(votersText)
                        } else {
                            val votersText = TextView(this@VoteDetailActivity).apply {
                                textSize = 14f
                                text = "투표한 사람: 없음"
                                setPadding(0, 0, 0, 16)
                                setTextColor(resources.getColor(R.color.purple))
                            }
                            resultContainer.addView(votersText)
                        }
                    }

                    resultLabels.add(label)
                    resultCounts.add(count)
                }

                val entries = resultCounts.mapIndexed { index, count ->
                    BarEntry(index.toFloat(), count.toFloat())
                }

                val barDataSet = BarDataSet(entries, "투표 결과").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextSize = 12f
                }

                barChart.data = BarData(barDataSet)
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(resultLabels)
                barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                barChart.xAxis.setDrawGridLines(false)
                barChart.axisLeft.setDrawGridLines(false)
                barChart.axisRight.isEnabled = false
                barChart.description.isEnabled = false
                barChart.setFitBars(true)
                barChart.animateY(1000)
                barChart.invalidate()
            } else {
                Toast.makeText(this@VoteDetailActivity, "결과 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun loadPollOptions() {
        pollId?.let { id ->
            lifecycleScope.launch {
                val response = RetrofitClient.instance.getPollOptions(id)
                if (response.isSuccessful) {
                    val options = response.body() ?: return@launch
                    setupOptionViews(options)
                } else {
                    Toast.makeText(this@VoteDetailActivity, "옵션 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupOptionViews(options: List<PollOptionResponse>) {
        val container = findViewById<LinearLayout>(R.id.voteOptionGroupContainer)
        container.removeAllViews()

        if (isMultipleChoice) {
            options.forEach { option ->
                val checkBox = CheckBox(this).apply {
                    text = option.option_text
                    textSize = 16f
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedOptionIds.add(option.id)
                        else selectedOptionIds.remove(option.id)
                    }
                }
                container.addView(checkBox)
            }
        } else {
            val radioGroup = RadioGroup(this).apply {
                orientation = RadioGroup.VERTICAL
            }
            options.forEach { option ->
                val radioButton = RadioButton(this).apply {
                    text = option.option_text
                    textSize = 16f
                    tag = option.id
                }
                radioGroup.addView(radioButton)
            }
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedButton = group.findViewById<RadioButton>(checkedId)
                selectedOptionIds.clear()
                selectedButton?.tag?.let {
                    selectedOptionIds.add(it.toString())
                }
            }
            container.addView(radioGroup)
        }
    }



    private fun checkOwnershipAndSetDeleteButton() {
        lifecycleScope.launch {
            val response = RetrofitClient.instance.getUserInfo("Bearer $authToken")
            if (response.isSuccessful) {
                val myUserId = response.body()?.id
                if (myUserId != null && myUserId == pollUserId) {
                    btnDelVote.visibility = View.VISIBLE
                    btnDelVote.setOnClickListener {
                        deleteVote()
                    }
                }
            } else {
                Toast.makeText(this@VoteDetailActivity, "사용자 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteVote() {
        pollId?.let { id ->
            lifecycleScope.launch {
                val response = RetrofitClient.instance.deletePoll(id, "Bearer $authToken")
                if (response.isSuccessful) {
                    Toast.makeText(this@VoteDetailActivity, "투표가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@VoteDetailActivity, "삭제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
private fun isAfterToday(dateString: String?): Boolean {
    return try {
        val formatter = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        val date = formatter.parse(dateString)
        val today = java.util.Date()
        date != null && date.after(today)
    } catch (e: Exception) {
        false
    }
}

