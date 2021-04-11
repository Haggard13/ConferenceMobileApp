package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.example.conference.R
import com.example.conference.exception.AddOpinionException
import com.example.conference.server.Server
import kotlinx.android.synthetic.main.activity_add_opinion.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URLEncoder

class AddOpinionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_opinion)

        addOpinionBackIB.setOnClickListener { finish() }

        saveOpinionBtn.setOnClickListener {
            val text = opinionET.text.toString()
            if (text.isBlank()) {
                showToast("Введите текст")
            }
            GlobalScope.launch {
                try {
                    val r = Server.get(
                        "/opinions" +
                                "/addOpinion" +
                                "/?user_id=${
                                    getSharedPreferences(
                                        "user_info",
                                        MODE_PRIVATE
                                    ).getInt("user_id", 0)
                                }" +
                                "&text=${URLEncoder.encode(text, "UTF-8")}" +
                                "&type=1" +
                                "&result_id=${intent.getIntExtra("result_id", 0)}" +
                                "&user_name=${
                                    getSharedPreferences("user_info", MODE_PRIVATE).getString(
                                        "user_name",
                                        ""
                                    )
                                }" +
                                "&user_surname=${
                                    getSharedPreferences(
                                        "user_info",
                                        MODE_PRIVATE
                                    ).getString("user_surname", "")
                                }"
                    )
                    if (!r.isSuccessful) throw ConnectException()
                    if (r.body!!.string().toInt() != 1) throw AddOpinionException()
                    showSuspendToast("Успешно")
                    finish()
                } catch (e: ConnectException) {
                    showSuspendToast("Проверьте подключение к сети")
                } catch (e: SocketTimeoutException) {
                    showSuspendToast("Проверьте подключение к сети")
                } catch (e: AddOpinionException) {
                    showSuspendToast("Вы уже оставили свое мнение")
                }
            }

        }
    }

    fun showToast(text: String) =
        Toast.makeText(this, text, LENGTH_LONG).show()

    private suspend fun showSuspendToast(text: String) =
        withContext(Main) {
            Toast.makeText(this@AddOpinionActivity, text, LENGTH_LONG).show()
        }
}