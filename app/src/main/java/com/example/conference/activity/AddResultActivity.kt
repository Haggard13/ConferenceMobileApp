package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.example.conference.R
import com.example.conference.exception.AddResultException
import com.example.conference.server.Server
import kotlinx.android.synthetic.main.activity_add_result.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder

class AddResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_result)

        addResultBtn.setOnClickListener(this::onAddResultClick)
        addResultCardsBackIB.setOnClickListener { finish() }
    }

    private fun onAddResultClick(v: View) {
        GlobalScope.launch {
            val conferenceID = intent.getIntExtra("conference_id", 0)
            val resultName = resultNameET.text.toString()
            val resultDescription = resultDescriptionET.text.toString()
            if (resultName.isBlank() || resultDescription.isBlank()) {
                showToast("Введите текст")
                return@launch
            }
            try {
                val response = Server.get(
                    "/results" +
                            "/addResult" +
                            "/?conference_id=$conferenceID" +
                            "&name=${URLEncoder.encode(resultName, "UTF-8")}" +
                            "&description=${URLEncoder.encode(resultDescription, "UTF-8")}"
                )
                if (!response.isSuccessful || response.body!!.string().toInt() != 1)
                    throw AddResultException()

            } catch (e: ConnectException) {
                showToast("Проверьте подключение к сети")
                return@launch
            } catch (e: SocketTimeoutException) {
                showToast("Проверьте подключение к сети")
                return@launch
            } catch (e: AddResultException) {
                showToast("Что-то пошло не так")
                return@launch
            }
            showToast("Успешно")
            finish()
        }
    }

    private suspend fun showToast(text: String) {
        withContext(Main) {
            Toast
                .makeText(this@AddResultActivity, text, LENGTH_LONG)
                .show()
        }
    }
}