package com.example.conference.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.conference.R
import com.example.conference.activity.CreateDialogueActivity
import com.example.conference.activity.DialogueActivity
import com.example.conference.adapter.DialoguesRecyclerViewAdapter
import com.example.conference.databinding.FragmentDialoguesBinding
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.exception.DialoguesGettingException
import com.example.conference.server.provider.DialogueProvider
import com.example.conference.vm.DialoguesViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class DialoguesFragment : Fragment() {

    private lateinit var viewModel: DialoguesViewModel
    private lateinit var adapter: DialoguesRecyclerViewAdapter
    private val dialogueProvider = DialogueProvider()
    private var binding: FragmentDialoguesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(DialoguesViewModel::class.java)

        activity!!.registerReceiver(
            NewMessageBroadcastReceiver(),
            IntentFilter("NEW_DIALOGUE_MESSAGE")
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDialoguesBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewModelScope.launch {
            binding?.dialoguesRV?.apply {

                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

                this@DialoguesFragment.adapter = DialoguesRecyclerViewAdapter(
                    viewModel
                        .getDialogues()
                        .sortedByDescending { it.last_message_time }
                ) { dialogueID ->
                    startActivity(
                        Intent(activity, DialogueActivity::class.java)
                            .putExtra("dialogue_id", dialogueID)
                    )
                }
                adapter = this@DialoguesFragment.adapter
            }
        }

        binding?.addDialogueIB?.setOnClickListener(this::onAddDialogueClick)

        binding?.dialogueSR?.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener {
                CoroutineScope(Main).launch {
                    //updateDialogues()
                    isRefreshing = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Main).launch {
            binding?.dialoguesPB?.isVisible = true
            //updateDialogues()
            binding?.dialoguesPB?.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onAddDialogueClick(v: View) {
        startActivity(Intent(activity, CreateDialogueActivity::class.java))
    }

    private suspend fun updateDialogues() {
        try {
            val dialogues: List<DialogueEntity> =
                withContext(IO) {
                    dialogueProvider
                        .getAllDialogues(activity!!.applicationContext)
                        .sortedByDescending { it.last_message_time }
                }

            if (dialogues.isEmpty())
                return

            adapter.apply {
                this.dialogues = dialogues
                notifyDataSetChanged()
            }

            withContext(IO) {
                dialogues.forEach {
                    viewModel.addDialogue(it)
                }
            }
        } catch (e: DialoguesGettingException) {
            Snackbar
                .make(binding?.root!!, "Проверьте подключение к сети", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private inner class NewMessageBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            CoroutineScope(Main).launch {
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {}
                    //updateDialogues()
            }
        }
    }
}