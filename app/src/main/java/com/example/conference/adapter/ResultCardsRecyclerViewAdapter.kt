package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.json.ResultCards
import kotlinx.android.synthetic.main.item_view_result_card.view.*

class ResultCardsRecyclerViewAdapter(var results: ResultCards, val callback: (Int, String) -> Unit):
    RecyclerView.Adapter<ResultCardsRecyclerViewAdapter.ResultCardViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResultCardsRecyclerViewAdapter.ResultCardViewHolder =
        ResultCardViewHolder(
            LayoutInflater
            .from(
                parent.context
            )
            .inflate(
                R.layout.item_view_result_card,
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: ResultCardsRecyclerViewAdapter.ResultCardViewHolder,
        position: Int) = holder.bind(position)


    override fun getItemCount() = results.list.size

    inner class ResultCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resultName = itemView.resultNameTV as TextView
        private val resultDescription = itemView.resultDescriptionTV as TextView
        fun bind(position: Int) {
            resultName.text = results.list[position].name
            resultDescription.text = results.list[position].description
            itemView.setOnClickListener {
                callback(results.list[position].id, results.list[position].name)
            }
        }
    }
}