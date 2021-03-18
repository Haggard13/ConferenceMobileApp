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
import kotlinx.android.synthetic.main.activity_meet.*

class MeetActivity: AppCompatActivity() {
    private val roomListener: RoomListenerImpl = RoomListenerImpl(0,"email", this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet)

        membersFragment.view!!.visibility = INVISIBLE
        chatFragment.view!!.visibility = INVISIBLE
        roomFragment.view!!.visibility = INVISIBLE
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