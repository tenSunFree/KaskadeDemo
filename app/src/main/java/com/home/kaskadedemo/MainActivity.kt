package com.home.kaskadedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) { // 為了不添加Fragment兩次
            val host = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerFragment, host)
                .setPrimaryNavigationFragment(host)
                .commit()
        }
    }
}
