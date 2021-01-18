package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.entity.CMessageMinimal
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.service.Http
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.group_message_item_view.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ConferencesRecyclerViewAdapter(
    var conferences: List<ConferenceEntity>,
    var lastMessages: HashMap<Int, CMessageMinimal>,
    private val callback: (Int) -> Unit
): RecyclerView.Adapter<ConferencesRecyclerViewAdapter.ConferencesViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConferencesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_message_item_view, parent, false)
        return ConferencesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConferencesViewHolder, position: Int) = holder.bind(position)


    override fun getItemCount() = conferences.size;

    inner class ConferencesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar: ImageView = itemView.groupAvatarImage
        var name: TextView = itemView.groupNameTV
        var lastMessage: TextView = itemView.groupLastMessageTV
        var time: TextView = itemView.lastMessageTimeTV
        fun bind(position: Int) {
            Picasso.get()
                .load(Http.baseURL
                        + "/conference/avatar/download/?id="
                        + conferences[position].id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            name.text = conferences[position].name
            lastMessage.text = lastMessages[conferences[position].id]!!.text
            time.text =
                if (lastMessages[conferences[position].id]!!.date_time == 0.toLong())
                    ""
                else
                    getTime(lastMessages[conferences[position].id]!!.date_time)
            itemView.setOnClickListener {
                callback.invoke(conferences[position].id)
            }
        }
        private fun getTime(ms: Long) : String = SimpleDateFormat("dd MMM HH:mm").format(Date(ms))
    }
}