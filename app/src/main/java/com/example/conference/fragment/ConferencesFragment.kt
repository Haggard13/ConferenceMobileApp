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
import com.example.conference.activity.ConferenceActivity
import com.example.conference.activity.CreateConferenceActivity
import com.example.conference.adapter.ConferencesRecyclerViewAdapter
import com.example.conference.databinding.FragmentConferencesBinding
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.exception.ConferencesGettingException
import com.example.conference.server.provider.ConferenceProvider
import com.example.conference.vm.ConferencesViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConferencesFragment : Fragment() {

    private lateinit var viewModel: ConferencesViewModel
    private lateinit var adapter: ConferencesRecyclerViewAdapter
    private val conferenceProvider = ConferenceProvider()
    private var binding: FragmentConferencesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(ConferencesViewModel::class.java)

        activity!!.registerReceiver(
            NewMessageBroadcastReceiver(),
            IntentFilter("NEW_CONFERENCE_MESSAGE")
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentConferencesBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewModelScope.launch {
            binding?.conferencesRV?.apply {

                layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                
                this@ConferencesFragment.adapter = ConferencesRecyclerViewAdapter(
                    viewModel
                        .getConferences()
                        .sortedByDescending { it.last_message_time }
                ) { conferenceID ->
                    startActivity(
                        Intent(activity, ConferenceActivity::class.java)
                            .putExtra("conference_id", conferenceID)
                    )
                }
                adapter = this@ConferencesFragment.adapter
            }
        }

        binding?.addConferenceIB?.setOnClickListener(this::onAddConferenceClick)

        binding?.conferenceSR?.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener {
                CoroutineScope(Main).launch {
                    updateConferences()
                    isRefreshing = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Main).launch {
            binding?.conferencesPB?.isVisible = true
            updateConferences()
            binding?.conferencesPB?.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onAddConferenceClick(v: View) =
        startActivity(Intent(activity, CreateConferenceActivity::class.java))


    private suspend fun updateConferences() {
        try {
            val conferences: List<ConferenceEntity> =
                withContext(IO) {
                    conferenceProvider
                        .getAllConferences(activity!!.applicationContext)
                        .sortedByDescending { it.last_message_time }
                }

            if (conferences.isEmpty())
                return

            adapter.apply {
                this.conferences = conferences
                notifyDataSetChanged()
            }

            withContext(IO) {
                conferences.forEach {
                    viewModel.addConference(it)
                }
            }
        } catch (e: ConferencesGettingException) {
            Snackbar
                .make(binding?.root!!, "Проверьте подключение к сети", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private inner class NewMessageBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            CoroutineScope(Main).launch {
                if (lifecycle.currentState == Lifecycle.State.RESUMED)
                    updateConferences()
            }
        }
    }
}
