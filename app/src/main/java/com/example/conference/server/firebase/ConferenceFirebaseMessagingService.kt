package com.example.conference.server.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.os.Build
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
        if (p0.data["senderID"]!!.toInt() == Account(this).userID) {
            //return
        }
        CoroutineScope(Main).launch {
            val notificationManager = NotificationManagerCompat
                .from(this@ConferenceFirebaseMessagingService)

            val avatar = getAvatar(p0.data["isConference"].toBoolean(), p0.data["id"]!!.toInt())

            val notification: Notification =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel: NotificationChannel =
                        createChannel(p0.data["isConference"].toBoolean())
                    notificationManager.createNotificationChannel(channel)
                    NotificationCompat.Builder(
                        this@ConferenceFirebaseMessagingService,
                        channel.id
                    )
                } else {
                    NotificationCompat.Builder(this@ConferenceFirebaseMessagingService)
                }.setMessagingStyle(p0, avatar)
                    .build()
            notificationManager.notify(p0.data["id"]!!.toInt(), notification)
        }
    }
    
    private fun NotificationCompat.Builder.setMessagingStyle(p0: RemoteMessage, avatar: Bitmap):
            NotificationCompat.Builder {
        val sender = Person.Builder()
            .setName(p0.notification!!.title)
            .setIcon(IconCompat.createWithBitmap(avatar))
            .build()
        val style = NotificationCompat.MessagingStyle(sender)
            .addMessage(p0.notification!!.body, p0.data["time"]!!.toLong(), sender)
        
        return this.setSmallIcon(R.drawable.ic_stat_name)
            .setStyle(style)
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
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