package com.example.conference.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.conference.R
import com.example.conference.activity.ConferenceActivity
import com.example.conference.activity.CreateConferenceActivity
import com.example.conference.adapter.ConferencesRecyclerViewAdapter
import com.example.conference.json.OutputConferenceList
import com.example.conference.server.Server
import com.example.conference.vm.ConferencesViewModel
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.net.ConnectException
import java.net.SocketTimeoutException

class ConferencesFragment : Fragment() {

    private lateinit var vm: ConferencesViewModel
    private var conferenceCount = 0
    private lateinit var pb: ProgressBar
    private var needRVUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this).get(ConferencesViewModel::class.java)
        activity!!.registerReceiver(
            NewNameBroadcastReceiver(),
            IntentFilter("NEW_CONFERENCE_NAME"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_conferences, container, false)
        pb = v.findViewById(R.id.conferencesPB)

        vm.viewModelScope.launch {
            onProgressBar()

            val confs = vm.getConferences()
            val lastMessages = vm.getLastMessages(confs)
            val rv = v.findViewById<RecyclerView>(R.id.conferencesRV)

            vm.adapter = ConferencesRecyclerViewAdapter(confs, lastMessages) {
                startActivity(
                    Intent(activity, ConferenceActivity::class.java)
                        .putExtra("conference_id", it)
                )
            }

            with(rv) {
                layoutManager = LinearLayoutManager(activity)
                adapter = vm.adapter
            }

            conferenceCount = vm.conferenceCount()

            offProgressBar()
        }

        v.findViewById<ImageButton>(R.id.addConferenceIB).setOnClickListener {
            startActivity(Intent(activity, CreateConferenceActivity::class.java))
        }

        val sr = v.findViewById<SwipeRefreshLayout>(R.id.conferenceSR)
        sr.setColorSchemeResources(R.color.colorAccent)
        sr.setOnRefreshListener {
            GlobalScope.launch {
                refreshList()
                withContext(Main) {sr.isRefreshing = false}
            }
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        if (needRVUpdate) {
            vm.adapter.notifyDataSetChanged()
            needRVUpdate = false
        }
        GlobalScope.launch {
            onProgressBar()
            refreshList()
            offProgressBar()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private suspend fun refreshList() {
        try {
            val r = Server.get(String.format("/conference/getNewConference/?user_id=%s&last_conference_id=%s",
                activity?.getSharedPreferences("user_info", MODE_PRIVATE)
                    ?.getInt("user_id", 0),
                vm.getLastID()))
            if (r.isSuccessful) {
                val cs = Gson().fromJson(r.body?.string(), OutputConferenceList::class.java)
                cs.list.forEach {
                    vm.addConference(it)
                }
            }

            while (!vm.adapterIsInitialized()) delay(500)

            if (conferenceCount != vm.conferenceCount()) {
                vm.updateAdapterConferences()
                conferenceCount = vm.conferenceCount()
            }
            vm.updateAdapterLastMessages()
            withContext(Main) { vm.adapter.notifyDataSetChanged()}
        }
        catch (e: ConnectException) {
            vm.showToast(activity!!, "Проверьте подключение к сети")
        }
        catch (e: SocketTimeoutException) {
            vm.showToast(activity!!, "Проверьте подключение к сети")
        }
    }

    private suspend fun onProgressBar() = withContext(Main) {
        pb.visibility = View.VISIBLE
    }

    private suspend fun offProgressBar() = withContext(Main) {
        pb.visibility = View.INVISIBLE
    }

    inner class NewNameBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vm.viewModelScope.launch {
                vm.updateAdapterConferences()
                needRVUpdate = true
            }
        }
    }
}
