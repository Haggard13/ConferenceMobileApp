package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.conference.json.Opinion
import com.example.conference.R
import com.example.conference.service.Server
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_opinion.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException

class OpinionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opinion)

        nameOpinionTV.text = intent.getStringExtra("user_name")
        opinionBackIB.setOnClickListener { finish() }

        GlobalScope.launch {
            try {
                val r = Server.get("/opinions" +
                        "/getOpinion" +
                        "/?user_id=${intent.getIntExtra("user_id", 0)}" +
                        "&result_id=${intent.getIntExtra("result_id", 0)}")
                if (!r.isSuccessful) throw ConnectException()
                val opinion = Gson().fromJson(r.body!!.string(), Opinion::class.java)
                opinionTextTV.text = opinion.text
            } catch(e: ConnectException) {
                showToast("Проверьте подключение к сети")
            } catch (e: SocketTimeoutException) {
                showToast("Проверьте подключение к сети")
            }
        }
    }

    suspend fun showToast(text: String) {
        withContext(Dispatchers.Main) {
            Toast
                .makeText(
                    this@OpinionActivity,
                    text,
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }
}