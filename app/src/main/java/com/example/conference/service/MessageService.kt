package com.example.conference.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.conference.R
import com.example.conference.application.ConferenceApplication
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.json.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.sql.*
import kotlin.properties.Delegates
import com.example.conference.R.drawable.notification_icon as notificationIcon


class MessageService : Service() {
    private var USER_ID by Delegates.notNull<Int>()
    private lateinit var db: ConferenceRoomDatabase

    //region Service Methods
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GlobalScope.launch {
            db = ConferenceRoomDatabase.getDatabase(this@MessageService)
            USER_ID = getSharedPreferences("user_info", MODE_PRIVATE).getInt("user_id", 0)
            checkServerData()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    //endregion

    private suspend fun checkServerData() {
        while(true) {
            try {
                checkNewConference()
                checkNewConferenceMessages()
                checkNewDialogue()
                checkNewDialogueMessages()
            }
            catch (e: ConnectException) {}
            catch (e: SocketTimeoutException) {}
            catch (e: SocketException) {}

            delay(15 * 1000)
        }
    }

    //region Check Methods
    private suspend fun checkNewConference() {
        val r = Server.get(String.format("/conference/checkNewConference/?user_id=%s", USER_ID))
        if (r.isSuccessful) {
            val cs: ConferenceNotification =
                Gson().fromJson(r.body?.string(), ConferenceNotification::class.java)
            cs.conference_list.forEach {
                withContext(Main) {
                    newConferenceNotification(it)
                }
            }
        }
    }

    private suspend fun checkNewConferenceMessages() {
        val json = URLEncoder.encode(Gson().toJson(getCMessagesList()), "UTF-8")
        val r = Server.get(String.format("/conference/checkNewMessage/?conference_list=%s&user_id=%s",
            json, USER_ID))
        if (r.isSuccessful) {
            val mns: MNList =
                Gson().fromJson(r.body?.string(), MNList::class.java)
            mns.list.forEach {
                val app: ConferenceApplication = applicationContext as ConferenceApplication
                if (app.conference_id != it.id
                    && getConferenceNotificationState(it.id).notification == 1) {
                    withContext(Main) {
                        newMessageNotification(it, true)
                    }
                }
            }
        }
    }

    private suspend fun checkNewDialogue() {
        val r = Server.get(String.format("/dialogue/checkNewDialogue/?user_id=%s",
            USER_ID))
        if (r.isSuccessful) {
            val ds: DialogueNotification = Gson().fromJson(r.body?.string(), DialogueNotification::class.java)
            ds.dialogue_list.forEach {
                withContext(Dispatchers.Main) {
                    newDialogueNotification(it)
                }
            }
        }
    }

    private suspend fun checkNewDialogueMessages() {
        val json = URLEncoder.encode(Gson().toJson(getDMessagesList()), "UTF-8")
        val r = Server.get(String.format("/dialogue/checkNewMessage/?dialogue_list=%s&user_id=%s",
            json, getSharedPreferences("user_info", MODE_PRIVATE).getInt("user_id", 0)))
        if (r.isSuccessful) {
            val nms: MNList =
                Gson().fromJson(r.body?.string(), MNList::class.java)
            nms.list.forEach {
                val app: ConferenceApplication = applicationContext as ConferenceApplication
                if (app.dialogue_id != it.id) {
                    withContext(Dispatchers.Main) {
                        newMessageNotification(it, false)
                    }
                }
            }
        }
    }
    //endregion

    //region Notification Methods
    private fun newConferenceNotification(name: String) {
        val notificationManager =
            NotificationManagerCompat.from(this@MessageService)
        lateinit var builder: NotificationCompat.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c =
                NotificationChannel("CHANNEL_NEW_CONFERENCE", "Новые конференции",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(c)
            builder =
                NotificationCompat.Builder(this@MessageService, c.id)
                    .setSmallIcon(notificationIcon)
                    .setContentTitle(name)
                    .setContentText("Вас добавили в конференцию")
        } else {
            builder =
                NotificationCompat.Builder(this@MessageService)
                    .setSmallIcon(notificationIcon)
                    .setContentTitle(name)
                    .setContentText("Вас добавили в конференцию")
        }
        val notification: Notification = builder.build()

        notificationManager.notify(1, notification)
    }

    private fun newDialogueNotification(name: String) {
        val notificationManager =
            NotificationManagerCompat.from(this@MessageService)
        lateinit var builder: NotificationCompat.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c =
                NotificationChannel("CHANNEL_NEW_DIALOGUE", "Новые диалоги",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(c)
            builder =
                NotificationCompat.Builder(this@MessageService, c.id)
                    .setSmallIcon(notificationIcon)
                    .setContentTitle(name)
                    .setContentText("Создан диалог с вашим участием")
        } else {
            builder =
                NotificationCompat.Builder(this@MessageService)
                    .setSmallIcon(notificationIcon)
                    .setContentTitle(name)
                    .setContentText("Создан диалог с вашим участием")
        }
        val notification: Notification = builder.build()

        notificationManager.notify(2, notification)
    }

    private fun newMessageNotification(nm: MessagesNotification, isConference: Boolean) {
        GlobalScope.launch {
            val notificationManager =
                NotificationManagerCompat.from(this@MessageService)
            lateinit var builder: NotificationCompat.Builder

            val conferenceIcon = Picasso
                .get()
                .load(if (isConference) "${Server.baseURL}/conference/avatar/download/?id=${nm.id}" else "${Server.baseURL}/user/avatar/download/?id=${nm.id}")
                .get()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val c =
                    NotificationChannel(
                        "CHANNEL_NEW_MESSAGE", "Новые сообщения",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                notificationManager.createNotificationChannel(c)
                builder =
                    NotificationCompat.Builder(this@MessageService, c.id)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(nm.name)
                        .setContentText(nm.text)
                        .setLargeIcon(conferenceIcon)
            } else {
                builder =
                    NotificationCompat.Builder(this@MessageService)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(nm.name)
                        .setContentText(nm.text)
                        .setLargeIcon(conferenceIcon)
            }
            val notification: Notification = builder.build()

            withContext(Main) {
                notificationManager.notify(nm.id, notification)
            }
        }
    }


    //endregion

    //region Остальное
    private suspend fun getCMessagesList(): ConferenceIDList {
        val cl = ConferenceIDList()
        getLocalAllConferences().forEach {
            if (it.notification == 1) cl.conferenceList.add(ConferenceID(it.id))
        }

        return cl
    }

    private suspend fun getConferenceNotificationState(id: Int) =
        db.conferenceDao().getConference(id)[0]

    private suspend fun getDMessagesList(): DialogueIDList {
        val dl = DialogueIDList()
        getLocalAllDialogues().forEach {
            dl.dialogueList.add(DialogueID(it.id))
        }

        return dl
    }


    private suspend fun getLocalAllConferences(): List<ConferenceEntity> =
        db.conferenceDao().getAll()

    private suspend fun getLocalAllDialogues(): List<DialogueEntity> =
        db.dialogueDao().getAll()
    //endregion
}