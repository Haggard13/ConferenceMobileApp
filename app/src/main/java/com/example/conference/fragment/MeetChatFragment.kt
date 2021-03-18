package com.example.conference.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.conference.R

class MeetChatFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_meet_chat, container, false)

        v.findViewById<ImageButton>(R.id.chatBackIB).setOnClickListener(this::onBackClick)

        return v
    }

    private fun onBackClick(v: View) {
        this.view!!.visibility = View.INVISIBLE
    }
}