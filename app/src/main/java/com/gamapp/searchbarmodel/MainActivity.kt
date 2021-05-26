package com.gamapp.searchbarmodel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gamapp.searchbar.SearchFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fragment = SearchFragment()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frame_layout,fragment)
            addToBackStack(null)
            commit()
        }
        fragment.setOnSearchListener {
            supportFragmentManager.popBackStack()
        }
    }
}