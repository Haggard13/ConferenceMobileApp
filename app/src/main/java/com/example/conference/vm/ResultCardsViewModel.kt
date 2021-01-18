package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.ResultCardsRecyclerViewAdapter

class ResultCardsViewModel(app: Application): AndroidViewModel(app) {
    lateinit var adapter: ResultCardsRecyclerViewAdapter
    fun adapterIsInit() = this::adapter.isInitialized
}