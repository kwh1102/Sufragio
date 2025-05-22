package com.example.sufragio

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sufragio.ui.home.HomeFragment
import com.example.sufragio.ui.search.SearchFragment
import com.example.sufragio.ui.creation.CreationFragment
import com.example.sufragio.ui.list.ListFragment
import com.example.sufragio.ui.mypage.MyPageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        setCurrentFragment(HomeFragment(), false, 5, 0)
        currentIndex = 0

        bottomNav.setOnItemSelectedListener {
            val (fragment, index) = when (it.itemId) {
                R.id.navigation_home -> Pair(HomeFragment(), 0)
                R.id.navigation_search -> Pair(SearchFragment(), 1)
                R.id.navigation_creation -> Pair(CreationFragment(), 2)
                R.id.navigation_list -> Pair(ListFragment(), 3)
                R.id.navigation_mypage -> Pair(MyPageFragment(), 4)
                else -> return@setOnItemSelectedListener false
            }

            val isForward = index > currentIndex
            setCurrentFragment(fragment, isForward, currentIndex, index)
            currentIndex = index
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment, isForward: Boolean, currentIndex: Int, index: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        if (currentIndex != index) {
            if (index == 2) {
                transaction.setCustomAnimations(
                    R.anim.fade_slide_in_top,
                    R.anim.fade_slide_out_bottom
                )
            } else {
                if (currentIndex == 2) {
                    transaction.setCustomAnimations(
                        R.anim.fade_slide_out_top,
                        R.anim.fade_slide_in_bottom
                    )
                } else {
                    if (isForward) {
                        transaction.setCustomAnimations(
                            R.anim.fade_slide_in_right,
                            R.anim.fade_slide_out_left
                        )
                    } else {
                        transaction.setCustomAnimations(
                            R.anim.fade_slide_in_left,
                            R.anim.fade_slide_out_right
                        )
                    }
                }
            }

            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        }
    }
}
