package com.example.conference.activity

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.conference.R
import com.example.conference.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var avatar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainVP.adapter = ViewPagerAdapter(supportFragmentManager)
        mainVP.offscreenPageLimit = 3

        mainTL.setupWithViewPager(mainVP)
    }
}