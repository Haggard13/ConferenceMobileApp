package com.example.conference.server.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ConferenceFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        GlobalScope.launch {
            //ConferenceAPIProvider.conferenceAPI.sendFirebaseMessagingToken(p0).execute() fixme
        }
    }
}