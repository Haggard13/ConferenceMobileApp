package com.example.conference.activity

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.conference.R
import com.example.conference.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


class MainActivity : AppCompatActivity() {
    var avatar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainVP.adapter = ViewPagerAdapter(supportFragmentManager)
        mainVP.offscreenPageLimit = 3

        mainTL.setupWithViewPager(mainVP)
        mainTL.getTabAt(0)?.setIcon(R.drawable.outline_groups_black_48)
        mainTL.getTabAt(1)?.setIcon(R.drawable.outline_people_black_48)
        mainTL.getTabAt(2)?.setIcon(R.drawable.outline_manage_accounts_black_48)

        CoroutineScope(IO).launch {
            ConferenceRoomDatabase().
        }
    }
}