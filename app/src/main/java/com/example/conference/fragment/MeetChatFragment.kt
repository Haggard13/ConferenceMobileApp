package com.example.conference.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.MessageType.MESSAGE_WITH_PHOTO
import com.example.conference.MessageType.MESSAGE_WITH_TEXT
import com.example.conference.R
import com.example.conference.activity.MeetActivity
import com.example.conference.adapter.MeetChatRecyclerViewAdapter
import com.example.conference.exception.SendMessageException
import com.example.conference.file.Addition
import com.example.conference.service.Server
import com.example.conference.vm.MeetChatViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_meet_chat.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MeetChatFragment : Fragment() {
    companion object {
        private const val TAG = "MeetChatFragment"
    }

    private lateinit var viewModel: MeetChatViewModel
    private var checkingNewMessageIsPossible = false
    private var messageType = MESSAGE_WITH_TEXT
    private lateinit var addition: Addition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MeetChatViewModel::class.java)
        viewModel.conferenceID = activity?.intent?.extras?.getInt("conferenceID")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "Create view")

        val v = inflater.inflate(R.layout.fragment_meet_chat, container, false)

        v.findViewById<ImageButton>(R.id.meet_chat_back_ib).setOnClickListener(this::onBackClick)
        v.findViewById<ImageButton>(R.id.meet_chat_send_message_btn)
            .setOnClickListener(this::onSendMessageClick)
        viewModel.viewModelScope.launch {
            val recyclerView = v.findViewById<RecyclerView>(R.id.chat_meet_rv)
            val messages = viewModel.getMessagesFromLocalDataBase()
            messages.list
            viewModel.recyclerViewAdapter =
                MeetChatRecyclerViewAdapter(messages)
            recyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
            recyclerView.adapter = viewModel.recyclerViewAdapter
        }

        return v
    }

    override fun onResume() {
        super.onResume()

        checkingNewMessageIsPossible = true

        GlobalScope.launch {
            while (checkingNewMessageIsPossible) {
                val newMessages: Boolean = Server.checkNewMeetChatMessages(
                    viewModel.conferenceID,
                    viewModel.getLastMessageID()
                )
                if (newMessages) {
                    updateRecyclerView()
                }
                delay(5 * 1000)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        checkingNewMessageIsPossible = false
    }

    private fun onBackClick(v: View) {
        Log.i(TAG, "Back button click")

        this.view!!.visibility = View.INVISIBLE
    }

    private fun onSendMessageClick(v: View) {
        Log.i(TAG, "Send message button click")

        meet_chat_message_sending_pb.visibility = View.VISIBLE
        when (messageType) {
            MESSAGE_WITH_TEXT -> sendTextMessage()
            MESSAGE_WITH_PHOTO -> sendMessageWithPhoto()
            else -> throw IllegalStateException("Message type out of permissible values")
        }
    }

    private fun sendTextMessage() {
        val messageText: String = chat_message_et.text.toString().trim()

        if (messageText.isEmpty()) {
            Log.i(TAG, "Message field is blank or empty")
            return
        }
        GlobalScope.launch {
            try {
                Server.sendMeetChatTextMessage(
                    viewModel.conferenceID,
                    messageText,
                    context = activity as MeetActivity
                )
                chat_message_et.text.clear()
            } catch (e: SendMessageException) {
                showSendErrorSnackBar()
            }
            updateRecyclerView()
            meet_chat_message_sending_pb.visibility = View.INVISIBLE
        }
    }

    private fun sendMessageWithPhoto() {
        TODO()
    }

    private suspend fun showSendErrorSnackBar() {
        withContext(Main) {
            val snackBar =
                Snackbar.make(
                    chat_meet_rv,
                    "Ошибка отправки",
                    Snackbar.LENGTH_LONG
                )

            snackBar.setAction("Повторить") {
                onSendMessageClick(it)
            }

            snackBar.show()
        }
    }

    private suspend fun updateRecyclerView() {
        val messages = Server.getNewMeetChatMessages(
            viewModel.conferenceID,
            viewModel.getLastMessageID(),
            activity!!
        )
        viewModel.updateMessageRecyclerView(messages)
    }
}