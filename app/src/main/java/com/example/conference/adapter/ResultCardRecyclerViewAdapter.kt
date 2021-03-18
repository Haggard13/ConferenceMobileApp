package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.json.OpinionCards
import com.example.conference.R
import com.example.conference.service.Server
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.opinion_item_view.view.*

class ResultCardRecyclerViewAdapter(
    var opinionList: OpinionCards,
    val callback: (Int, String) -> Unit
):
    RecyclerView.Adapter<ResultCardRecyclerViewAdapter.OpinionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpinionViewHolder =
        OpinionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.opinion_item_view, parent, false))

    override fun onBindViewHolder(holder: OpinionViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = opinionList.list.size

    inner class OpinionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.opinionAvatarIV
        val name: TextView = itemView.userNameOpinionTV
        fun bind(position: Int) {
            Picasso.get().load("${Server.baseURL}/user/avatar/download/?id=${opinionList.list[position].user_id}")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            name.text = ("${opinionList.list[position].user_name} ${opinionList.list[position].user_surname}")
            itemView.setOnClickListener {
                callback(opinionList.list[position].user_id, opinionList.list[position].user_name)
            }
        }
    }
}