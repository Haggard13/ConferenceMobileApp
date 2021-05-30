package com.example.conference.server.websocket

import com.example.conference.server.websocket.dto.*
import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import org.json.JSONObject

class WebSocketHandler(private val listener: SocketListener, private val onConnected: () -> Unit) {

    private val socket = WebSocketFactory().createSocket("ws://conferenceappalpha.herokuapp.com/groupcall"/*"ws://192.168.0.137:8082/groupcall"*/)
    private val gson = Gson()

    init {
        socket.addListener(object : WebSocketAdapter() {
            override fun onConnected(
                websocket: WebSocket?,
                headers: MutableMap<String, MutableList<String>>?
            ) {
                super.onConnected(websocket, headers)
                onConnected.invoke()
            }

            override fun onTextMessage(webSocket: WebSocket, jsonMessage: String) {
                val message = JSONObject(jsonMessage)
                when (message.getString("id")) {
                    "youJoin" -> listener.onJoin(gson.fromJson(message.toString(), Members::class.java))
                    "offer" -> listener.onOffer(gson.fromJson(message.toString(), Offer2::class.java))
                    "answer" -> listener.onAnswer(gson.fromJson(message.toString(), Answer2::class.java))
                    "iceCandidate" -> listener.onIceCandidate(gson.fromJson(message.toString(), IceCandidate2::class.java))
                    "userLeft" -> listener.onLeaving(gson.fromJson(message.toString(), LeavingUser::class.java))
                }
            }
        })
        socket.connectAsynchronously()
    }

    fun join(joiningMessage: JoiningMessage) {
        socket.sendText(gson.toJson(joiningMessage))
    }

    fun sendOffer(offer: Offer) {
        socket.sendText(gson.toJson(offer))
    }

    fun sendAnswer(answer: Answer) {
        socket.sendText(gson.toJson(answer))
    }

    fun sendIceCandidate(iceCandidate: IceCandidate) {
        socket.sendText(gson.toJson(iceCandidate))
    }

    fun close(): WebSocket =
        socket.disconnect()


    interface SocketListener {
        fun onJoin(members: Members)
        fun onOffer(offer: Offer2)
        fun onIceCandidate(ice: IceCandidate2)
        fun onAnswer(answer: Answer2)
        fun onLeaving(user: LeavingUser)
    }
}