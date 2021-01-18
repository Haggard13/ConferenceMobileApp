package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.conference.R
import com.example.conference.fragment.MeetMembersFragment

class MeetActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet)
    }

    fun onBroadcastClick(v: View) {
        val membersF = MeetMembersFragment()

    }
}