package com.example.conference.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.json.OpinionCards
import com.example.conference.R
import com.example.conference.adapter.ResultCardRecyclerViewAdapter
import com.example.conference.service.Server
import com.example.conference.vm.ResultCardViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_result_card.*
import kotlinx.android.synthetic.main.activity_result_cards.*
import kotlinx.android.synthetic.main.item_view_result_card.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException

class ResultCardActivity : AppCompatActivity() {
    lateinit var vm: ResultCardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_card)

        vm = ViewModelProvider(this).get(ResultCardViewModel::class.java)

        cardNameTV.text = intent.getStringExtra("result_name")

        GlobalScope.launch {
            try {
                val opinionCards = Gson().fromJson(
                    Server.get(
                        "/opinions/getOpinions/?result_id=" +
                                "${intent.getIntExtra("result_id", 0)}"
                    ).body!!.string(), OpinionCards::class.java
                )
                withContext(Main) {
                    opinionsRV.layoutManager = LinearLayoutManager(this@ResultCardActivity)
                    vm.adapter = ResultCardRecyclerViewAdapter(opinionCards) { user_id, user_name ->
                        val i = Intent(this@ResultCardActivity, OpinionActivity::class.java)
                        i.putExtra("user_id", user_id)
                        i.putExtra("user_name", user_name)
                        i.putExtra("result_id", intent.getIntExtra("result_id", 0))
                        startActivity(i)
                    }
                    opinionsRV.adapter = vm.adapter
                }
            } catch (e: ConnectException) {
                showToast("Проверьте подключение к сети")
            } catch (e: SocketTimeoutException) {
                showToast("Проверьте подключение к сети")
            }
        }

        cardBackIB.setOnClickListener { finish() }
        addOpinionIB.setOnClickListener {
            val i = Intent(this, AddOpinionActivity::class.java)
            i.putExtra("result_id", intent.getIntExtra("result_id", 0))
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        if (vm.adapterIsInit()) refreshRV()
    }

    suspend fun showToast(text: String) {
        withContext(Main) {
            Toast
                .makeText(
                    this@ResultCardActivity,
                    text,
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    private fun refreshRV() {
        GlobalScope.launch {
            try {
                val opinionCards = Gson().fromJson(
                    Server.get(
                        "/opinions/getOpinions/?result_id=" +
                                "${intent.getIntExtra("result_id", 0)}"
                    ).body!!.string(), OpinionCards::class.java
                )
                withContext(Main) {
                    vm.adapter.opinionList = opinionCards
                    vm.adapter.notifyDataSetChanged()
                }
            } catch (e: ConnectException) {
                withContext(Main) {
                    Toast
                        .makeText(
                            this@ResultCardActivity,
                            "Проверьте подключение к сети",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            } catch (e: SocketTimeoutException) {
                withContext(Main) {
                    Toast
                        .makeText(
                            this@ResultCardActivity,
                            "Проверьте подключение к сети",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        }
    }
}