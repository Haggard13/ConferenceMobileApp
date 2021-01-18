package com.example.conference.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.conference.R
import com.example.conference.activity.AddContactActivity
import com.example.conference.activity.CreateDialogueActivity
import com.example.conference.activity.DialogueActivity
import com.example.conference.adapter.DialoguesRecyclerViewAdapter
import com.example.conference.json.OutputDialogueList
import com.example.conference.service.Http
import com.example.conference.vm.DialogueViewModel
import com.example.conference.vm.DialoguesViewModel
import com.google.api.Distribution
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException

class DialoguesFragment : Fragment() {

    private lateinit var vm: DialoguesViewModel
    private var dialogueCount = 0
    private lateinit var pb: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(this).get(DialoguesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_dialogues, container, false)

        pb = v.findViewById(R.id.dialoguesPB)
        vm.viewModelScope.launch {
            withContext(Dispatchers.Main) {
                pb.visibility = View.VISIBLE
            }

            val dialogues = vm.getDialogues()
            val lastMessages = vm.getLastMessages(dialogues)
            val rv = v.findViewById<RecyclerView>(R.id.dialoguesRV)

            vm.adapter = DialoguesRecyclerViewAdapter(dialogues, lastMessages) {
                startActivity(
                    Intent(activity, DialogueActivity::class.java)
                        .putExtra("dialogue_id", it)
                )
            }

            rv.layoutManager = LinearLayoutManager(activity)
            rv.adapter = vm.adapter


            dialogueCount = vm.dialogueCount()

            withContext(Dispatchers.Main) {
                pb.visibility = View.INVISIBLE
            }
        }

        v.findViewById<ImageButton>(R.id.addDialogueIB).setOnClickListener(this::addDialogueOnClick)

        val sr = v.findViewById<SwipeRefreshLayout>(R.id.dialogueSR)
        sr.setColorSchemeResources(R.color.colorAccent)
        sr.setOnRefreshListener {
            GlobalScope.launch {
                refreshList()
                withContext(Dispatchers.Main) {sr.isRefreshing = false}
            }
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                pb.visibility = View.VISIBLE
            }
            refreshList()
            withContext(Dispatchers.Main) {
                pb.visibility = View.INVISIBLE
            }
        }
    }

    private suspend fun refreshList() {
        try {
            val r = Http.get(
                String.format(
                    "/dialogue/getNewDialogue/?user_id=%s&last_dialogue_id=%s",
                    activity?.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        ?.getInt("user_id", 0),
                    vm.getLastID()
                )
            )

            val result = r.body?.string() ?: ""

            if (r.isSuccessful) {
                val ds = Gson().fromJson(result, OutputDialogueList::class.java)
                ds.list.forEach {
                    vm.addDialogue(it)
                }
            }

            if (dialogueCount != vm.dialogueCount()) {
                vm.updateAdapterDialogues()
                dialogueCount = vm.dialogueCount()
            }
            vm.updateAdapterLastMessages()
            withContext(Dispatchers.Main) { vm.adapter.notifyDataSetChanged()}
        }
        catch (e: ConnectException) {
            vm.showToast(activity!!, "Проверьте подключение к сети")
        }
        catch (e: SocketTimeoutException) {
            vm.showToast(activity!!, "Проверьте подключение к сети")
        }
    }

    private fun addDialogueOnClick(v: View) {
        startActivity(Intent(activity, CreateDialogueActivity::class.java))
    }
}