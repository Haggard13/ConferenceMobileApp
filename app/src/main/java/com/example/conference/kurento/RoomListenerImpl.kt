package com.example.conference.kurento

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import fi.vtt.nubomedia.kurentoroomclientandroid.*
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor


class RoomListenerImpl(
    private val conferenceID: Int,
    private val userEmail: String,
    private val context: Context
) : RoomListener {
    private var executor = LooperExecutor()
    var kurentoRoomAPI: KurentoRoomAPI

    init {
        executor.requestStart()
        val wsRoomUri = "ws://192.168.0.105:8082/call"
        kurentoRoomAPI = KurentoRoomAPI(executor, wsRoomUri, this)
        kurentoRoomAPI.connectWebSocket()
    }

    override fun onRoomResponse(response: RoomResponse?) {
        Toast.makeText(context, "response", LENGTH_LONG).show()
    }

    override fun onRoomError(error: RoomError?) {
        Toast.makeText(context, "error", LENGTH_LONG).show()
    }

    override fun onRoomNotification(notification: RoomNotification?) {
        Toast.makeText(context, "notification", LENGTH_LONG).show()
    }

    override fun onRoomConnected() {
        Toast.makeText(context, "connected", LENGTH_LONG).show()
        kurentoRoomAPI.sendJoinRoom(userEmail, conferenceID.toString(), true, 1)
    }

    override fun onRoomDisconnected() {
        Toast.makeText(context, "disconnected", LENGTH_LONG).show()
    }
}