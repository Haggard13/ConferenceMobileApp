package com.example.conference.vm

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.example.conference.activity.PhotoReviewerActivity
import com.example.conference.adapter.ConferenceRecyclerViewAdapter
import com.example.conference.db.ConferenceRoomDatabase
import com.example.conference.db.data.SenderEnum
import com.example.conference.db.entity.CMessageEntity
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.MessageUpdateException
import com.example.conference.json.CMessageList
import com.example.conference.service.Server
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.properties.Delegates

class ConferenceViewModel(val app: Application) : AndroidViewModel(app) {
    private val conferenceDao = ConferenceRoomDatabase.getDatabase(app).conferenceDao()
    private val cMessageDao = ConferenceRoomDatabase.getDatabase(app).cMessageDao()

    lateinit var messages: List<CMessageEntity>
    lateinit var adapter: ConferenceRecyclerViewAdapter
    var conferenceID by Delegates.notNull<Int>()
    private val sp: SharedPreferences = app.getSharedPreferences("user_info", MODE_PRIVATE)

    //region DB Methods
    suspend fun getConference(): ConferenceEntity = conferenceDao.getConference(conferenceID)[0]

    private suspend fun getMessages(): List<CMessageEntity> = cMessageDao.getMessages(conferenceID)

    suspend fun saveMessage(m: CMessageEntity) = cMessageDao.insert(m)

    private suspend fun getLastMessageID(conference_id: Int) =
        cMessageDao.getLastMessageID(conference_id)

    private suspend fun getMessagesCount(conference_id: Int) =
        cMessageDao.getMessagesCount(conference_id)

    suspend fun update(c: ConferenceEntity) = conferenceDao.update(c)
    //endregion

    suspend fun updateMessages() {
        try {
            val r = Server.get(
                String.format(
                    "/conference/getNewMessages/?conference_id=%s&" +
                            "last_message_id=%s&" +
                            "user_id=%s",
                    conferenceID,
                    if (getMessagesCount(conferenceID) == 0)
                        0
                    else
                        getLastMessageID(conferenceID),
                    sp.getInt("user_id", 0)
                )
            )
            if (!r.isSuccessful) throw MessageUpdateException()
            val result = r.body!!.string()

            if (result.isNotBlank()) {
                val json = Gson().fromJson(result, CMessageList::class.java)
                json.list.forEach {
                    cMessageDao.insert(it)
                }
            }
            updateRV()
        } catch (e: ConnectException) {
        } catch (e: SocketTimeoutException) {
        } catch (e: MessageUpdateException) {
        } catch (e: SocketException) {
        }
    }

    fun messagesIsInitialize() = ::messages.isInitialized

    suspend fun updateRV() {
        messages = cMessageDao.getMessages(conferenceID)
        adapter.messages = messages
        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    fun generateURL(messageText: String, date: Long) = String.format(
        "/conference/sendMessage/?text=%s&" +
                "conference_id=%s&" +
                "date_time=%s&" +
                "sender_id=%s&" +
                "sender_name=%s&" +
                "sender_surname=%s",
        URLEncoder.encode(messageText, "UTF-8"),
        conferenceID,
        date,
        getUserID(),
        URLEncoder.encode(getUserName(), "UTF-8"),
        URLEncoder.encode(getUserSurname(), "UTF-8")
    )

    fun createCMessageEntity(
        messageText: String,
        date: Long,
        messageId: Int,
        type: Int
    ): CMessageEntity = CMessageEntity(
        messageId,
        messageText,
        date,
        getUserID(),
        conferenceID,
        getUserName() ?: "",
        getUserSurname() ?: "",
        SenderEnum.USER.ordinal,
        type
    )

    suspend fun initMessages() {
        messages = getMessages()
    }

    fun initAdapter(context: Context) {
        adapter = ConferenceRecyclerViewAdapter(
            messages, context,
            this::callbackForPhoto,
            this::callbackForFile
        )
    }

    private fun callbackForPhoto(id: Int) {
        val intent = Intent(app, PhotoReviewerActivity::class.java)
        intent.putExtra("photo_id", id)
        intent.putExtra("is_conference", true)
        app.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callbackForFile(id: Int, name: String) {
        GlobalScope.launch {
            var inputStream: InputStream? = null
            try {
                val response = Server.get("/conference/getFile/?id=$id")

                inputStream = response.body!!.byteStream()
                Files.write(
                    Paths.get(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)!!.path +
                                "/$name"
                    ), inputStream.readBytes()
                )
                showToast("Файл сохранен на устройство")
            } catch (e: SocketTimeoutException) {
                showToast("Возникла ошибка при скачивании")
            } catch (e: ConnectException) {
                showToast("Возникла ошибка при скачивании")
            } catch (e: SocketException) {
                showToast("Возникла ошибка при скачивании")
            } finally {
                inputStream?.close()
            }
        }
    }

    suspend fun showToast(text: String) =
        withContext(Main) { Toast.makeText(app, text, Toast.LENGTH_LONG).show() }

    //region SP Methods
    private fun getUserID(): Int = sp.getInt("user_id", 0)
    private fun getUserName(): String? = sp.getString("user_name", "")
    private fun getUserSurname(): String? = sp.getString("user_surname", "")
    //endregion
}