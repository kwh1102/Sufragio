package com.example.sufragio.ui.creation

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.sufragio.MainActivity
import com.example.sufragio.R
import com.example.sufragio.network.RetrofitClient
import com.example.sufragio.network.TokenManager
import com.example.sufragio.ui.login_register.LoginActivity
import com.example.sufragio.ui.mypage.MyPageFragment
import kotlinx.coroutines.launch
import java.util.*

class CreationFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var deadlineEditText: EditText
    private lateinit var optionsContainer: LinearLayout
    private lateinit var createButton: Button
    private lateinit var addOptionButton: Button
    private lateinit var anonymousSwitch: androidx.appcompat.widget.SwitchCompat
    private lateinit var multipleSwitch: androidx.appcompat.widget.SwitchCompat
    private lateinit var optionSwitch: androidx.appcompat.widget.SwitchCompat
    private var optionCount = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.editTitle)
        descriptionEditText = view.findViewById(R.id.editDescription)
        deadlineEditText = view.findViewById(R.id.deadline)
        optionsContainer = view.findViewById(R.id.optionsContainer)
        createButton = view.findViewById(R.id.createVoteButton)
        addOptionButton = view.findViewById(R.id.btnAddOption)
        anonymousSwitch = view.findViewById(R.id.switchAnonymous)
        multipleSwitch = view.findViewById(R.id.switchMultiple)
        optionSwitch = view.findViewById(R.id.switchOption)

        addOptionField("항목 1")
        addOptionField("항목 2")

        addOptionButton.setOnClickListener {
            optionCount++
            addOptionField("항목 $optionCount (선택)")
        }

        deadlineEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                val formatted = String.format("%04d.%02d.%02d", y, m + 1, d)
                deadlineEditText.setText(formatted)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePicker.show()
        }

        createButton.setOnClickListener {
            lifecycleScope.launch {
                val context = requireContext()
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                var accessToken = prefs.getString("access_token", null)

                if (accessToken.isNullOrEmpty()) {
                    showToast("로그인이 필요합니다.")
                    return@launch
                }

                val options = mutableListOf<String>()
                for (i in 0 until optionsContainer.childCount) {
                    val optionText = (optionsContainer.getChildAt(i) as EditText).text.toString()
                    if (optionText.isNotBlank()) {
                        options.add(optionText)
                    }
                }

                if (titleEditText.text.isNullOrBlank()) {
                    showToast("투표 제목을 입력해주세요.")
                    return@launch
                }

                if (titleEditText.text.contains("$")) {
                    showToast("제목에 $ 문자는 사용할 수 없습니다.")
                    return@launch
                }

                if (options.size < 2) {
                    showToast("항목 2개 이상이 필요합니다.")
                    return@launch
                }

                if (deadlineEditText.text.isNullOrBlank()) {
                    showToast("마감일을 입력해주세요.")
                    return@launch
                }

                val combinedTitle = "${titleEditText.text}\$${deadlineEditText.text}"

                var response = RetrofitClient.instance.createPoll(
                    authHeader = "Bearer $accessToken",
                    title = combinedTitle,
                    description = descriptionEditText.text.toString(),
                    isAnonymous = anonymousSwitch.isChecked,
                    isMultipleChoice = multipleSwitch.isChecked,
                    isOptionAddAllowed = optionSwitch.isChecked,
                    isRevotingAllowed = false,
                    options = options
                )

                if (!response.isSuccessful) {
                    accessToken = TokenManager.refreshAccessTokenIfNeeded(context)
                    if (accessToken != null) {
                        prefs.edit { putString("access_token", accessToken) }

                        response = RetrofitClient.instance.createPoll(
                            authHeader = "Bearer $accessToken",
                            title = titleEditText.text.toString(),
                            description = descriptionEditText.text.toString(),
                            isAnonymous = anonymousSwitch.isChecked,
                            isMultipleChoice = multipleSwitch.isChecked,
                            isOptionAddAllowed = optionSwitch.isChecked,
                            isRevotingAllowed = false,
                            options = options
                        )
                    }
                }

                if (response.isSuccessful) {
                    showToast("투표가 성공적으로 생성되었습니다!")
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MyPageFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    showToast("투표 생성 실패: ${response.code()}")
                }
            }
        }
    }

    private fun addOptionField(hint: String) {
        val editText = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
            this.hint = hint
        }
        optionsContainer.addView(editText)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
