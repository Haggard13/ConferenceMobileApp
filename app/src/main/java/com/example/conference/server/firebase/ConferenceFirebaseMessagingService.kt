package com.example.conference.server.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.example.conference.R
import com.example.conference.account.Account
import com.example.conference.server.Server
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConferenceFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        GlobalScope.launch {
            //ConferenceAPIProvider.conferenceAPI.sendFirebaseMessagingToken(p0).execute() //fixme
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        when (p0.data["notification_type"]) {
            "cmessage" ->
                CoroutineScope(Main).launch {
                    sendBroadcast(Intent("NEW_CONFERENCE_MESSAGE"))
                    notifyNewMessage(p0)
                }
            "dmessage" ->
                CoroutineScope(Main).launch {
                    sendBroadcast(Intent("NEW_DIALOGUE_MESSAGE"))
                    notifyNewMessage(p0)
                }
            "conference" ->
                CoroutineScope(Main).launch {
                    notifyNewConference(p0)
                }
        }

    }

    private fun notifyNewConference(p0: RemoteMessage) {
        TODO("Not yet implemented")
    }

    private suspend fun notifyNewMessage(p0: RemoteMessage) {
        val notificationType = p0.data["notification_type"]
        val id = p0.data["id"]!!.toInt()

        if (p0.data["senderID"]!!.toInt() == Account(this).userID) return

        val nManager = NotificationManagerCompat
            .from(this)

        val avatar = getAvatar(notificationType == "cmessage", id)

        val n: Notification =
            if (SDK_INT >= O) {
                val channel: NotificationChannel =
                    createChannel(notificationType == "cmessage")
                nManager.createNotificationChannel(channel)

                NotificationCompat.Builder(
                    this@ConferenceFirebaseMessagingService,
                    channel.id
                )
            } else {
                NotificationCompat.Builder(this@ConferenceFirebaseMessagingService)
            }
                .setMessagingStyle(p0, avatar)
                .build()

        nManager.notify(id, n)
    }
    
    private fun NotificationCompat.Builder.setMessagingStyle(p0: RemoteMessage, avatar: Bitmap
    ): NotificationCompat.Builder {
        val sender =
            Person.Builder()
                .setName(p0.notification!!.title)
                .setIcon(IconCompat.createWithBitmap(avatar))
                .build()
        val style =
            NotificationCompat.MessagingStyle(sender)
                .addMessage(p0.notification!!.body, p0.data["time"]!!.toLong(), sender)
        
        return this.setSmallIcon(R.drawable.ic_stat_name)
            .setStyle(style)
    }
    
    @RequiresApi(O)
    private fun createChannel(isConference: Boolean) =
        if (isConference) {
            NotificationChannel(
                "CONFERENCE_NEW_MESSAGE", "Конференции",
                NotificationManager.IMPORTANCE_HIGH
            )
        }
        else {
            NotificationChannel(
                "DIALOGUE_NEW_MESSAGE", "Диалоги",
                NotificationManager.IMPORTANCE_HIGH
            )
        }

    private suspend fun getAvatar(isConference: Boolean, id: Int) =
        withContext(IO) {
            Picasso
                .get()
                .load(
                    Server.baseURL +
                            (if (isConference) "/conference" else "/user") +
                            "/avatar/download/?id=$id"
                )
                .get()
        }
}