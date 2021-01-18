package com.example.conference.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.conference.adapter.ResultCardRecyclerViewAdapter

class ResultCardViewModel(app: Application): AndroidViewModel(app) {
    lateinit var adapter: ResultCardRecyclerViewAdapter
    fun adapterIsInit() = this::adapter.isInitialized
}