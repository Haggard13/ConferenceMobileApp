package com.example.conference.activity

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import com.example.conference.R
import com.example.conference.adapter.ViewPagerAdapter
import com.example.conference.service.MessageService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager


class MainActivity : AppCompatActivity() {
    var avatar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainVP.adapter = ViewPagerAdapter(supportFragmentManager)
        mainVP.offscreenPageLimit = 3

        mainTL.setupWithViewPager(mainVP)

        startService(Intent(this, MessageService::class.java))
    }
}