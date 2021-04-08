package com.example.conference.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.conference.R
import com.example.conference.kurento.RoomListenerImpl
import com.example.conference.kurento.WebRTCApp
import kotlinx.android.synthetic.main.activity_meet.*
import org.webrtc.EglBase


class MeetActivity: AppCompatActivity() {
    var conferenceID: Int = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet)

        membersFragment.view!!.visibility = INVISIBLE
        chatFragment.view!!.visibility = INVISIBLE
        roomFragment.view!!.visibility = INVISIBLE

        conferenceID = intent.extras?.getInt("conferenceID")!!

        broadcastSVR.init(EglBase.create().eglBaseContext, null)
        broadcastSVR.keepScreenOn = true
        val roomListener = RoomListenerImpl(conferenceID, "email", this)
        val webRTCApp = WebRTCApp(this, roomListener.kurentoRoomAPI)
    }

    override fun onStop() {
        super.onStop()
    }

    fun onMembersClick(v: View) {
        membersFragment.view!!.visibility = VISIBLE
        chatFragment.view!!.visibility = INVISIBLE
        roomFragment.view!!.visibility = INVISIBLE
    }

    fun onChatClick(v: View) {
        membersFragment.view!!.visibility = INVISIBLE
        chatFragment.view!!.visibility = VISIBLE
        roomFragment.view!!.visibility = INVISIBLE
    }

    fun onRoomClick(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.inflate(R.menu.room_choose_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.chatItem -> {
                    membersFragment.view!!.visibility = INVISIBLE
                    chatFragment.view!!.visibility = INVISIBLE
                    roomFragment.view!!.visibility = VISIBLE
                    true
                }
                R.id.microItem -> {
                    true
                }
                else -> false
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }

        popupMenu.show()
    }
}