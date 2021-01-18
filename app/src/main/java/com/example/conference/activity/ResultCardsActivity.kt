package com.example.conference.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.ResultCards
import com.example.conference.adapter.ResultCardsRecyclerViewAdapter
import com.example.conference.service.Http
import com.example.conference.vm.ResultCardsViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_result_cards.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException

class ResultCardsActivity : AppCompatActivity() {
    lateinit var vm: ResultCardsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_cards)

        vm = ViewModelProvider(this).get(ResultCardsViewModel::class.java)

        GlobalScope.launch {
            try {
                val results = Gson().fromJson(
                    Http.get(
                        "/results/getResults/?conference_id=" +
                                "${intent.getIntExtra("conference_id", 0)}"
                    ).body!!.string(), ResultCards::class.java
                )
                withContext(Main) {
                    resultCardsRV.layoutManager = LinearLayoutManager(this@ResultCardsActivity)
                    vm.adapter = ResultCardsRecyclerViewAdapter(results) { id, name ->
                        run {
                            val i = Intent(this@ResultCardsActivity, ResultCardActivity::class.java)
                            i.putExtra("result_id", id)
                            i.putExtra("result_name", name)
                            startActivity(i)
                        }
                    }
                    resultCardsRV.adapter = vm.adapter
                }
            } catch (e: ConnectException) {
                withContext(Main) {
                    Toast
                        .makeText(
                            this@ResultCardsActivity,
                            "Проверьте подключение к сети",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            } catch (e: SocketTimeoutException) {
                withContext(Main) {
                    Toast
                        .makeText(
                            this@ResultCardsActivity,
                            "Проверьте подключение к сети",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        }

        resultCardsBackIB.setOnClickListener { finish() }

        addResultCardIB.setOnClickListener {
            val i = Intent(this, AddResultActivity::class.java)
            i.putExtra("conference_id", intent.getIntExtra("conference_id", 0))
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        if (vm.adapterIsInit()) refreshRV()
    }

    private fun refreshRV() {
        GlobalScope.launch {
            try {
                val results = Gson().fromJson(
                    Http.get(
                        "/results/getResults/?conference_id=" +
                                "${intent.getIntExtra("conference_id", 0)}"
                    ).body!!.string(), ResultCards::class.java
                )
                withContext(Main) {
                    vm.adapter.results = results
                    vm.adapter.notifyDataSetChanged()
                }
            } catch (e: ConnectException) {
                withContext(Main) {
                    Toast
                        .makeText(
                            this@ResultCardsActivity,
                            "Проверьте подключение к сети",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            } catch (e: SocketTimeoutException) {
                withContext(Main) {
                    Toast
                        .makeText(
                            this@ResultCardsActivity,
                            "Проверьте подключение к сети",
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        }
    }
}