package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.data.SenderEnum
import com.example.conference.json.CMessageList
import com.example.conference.service.Server
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_item_view.view.*
import kotlinx.android.synthetic.main.your_message_item_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class MeetChatRecyclerViewAdapter(
    var messages: CMessageList
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.your_message_item_view, parent, false)
                YourMessagesViewHolder(itemView)
            }
            1 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_item_view, parent, false)
                MessagesViewHolder(itemView)
            }
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            0 -> (holder as YourMessagesViewHolder).bind(position)
            1 -> (holder as MessagesViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int = messages.list.size

    override fun getItemViewType(position: Int): Int =
        when (messages.list[position].sender_enum) {
            SenderEnum.USER.ordinal -> 0
            SenderEnum.NOT_USER.ordinal -> 1
            else -> throw IllegalStateException()
        }

    inner class MessagesViewHolder(
        itemView: View,
        var avatar: ImageView = itemView.memberAvatarIV,
        var date: TextView = itemView.memberMessageTimeTV,
        var message: TextView = itemView.memberMessageTV,
        var name: TextView = itemView.memberName
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(p: Int) {
            //region Picasso
            Picasso.get()
                .load(Server.baseURL + "/user/avatar/download/?id=" + messages.list[p].sender_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            //endregion
            date.text = getTime(messages.list[p].date_time)
            message.text = messages.list[p].text
            name.text = (messages.list[p].sender_name + " " + messages.list[p].sender_surname)
        }
    }

    inner class YourMessagesViewHolder(itemView: View,
                                       var avatar: ImageView = itemView.userAvatarIV,
                                       var date: TextView = itemView.userMessageTimeTV,
                                       var message: TextView = itemView.userMessageTV
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(p: Int) {
            //region Picasso
            Picasso.get()
                .load(Server.baseURL + "/user/avatar/download/?id=" + messages.list[p].sender_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            //endregion
            date.text = getTime(messages.list[p].date_time)
            message.text = messages.list[p].text
        }
    }

    private fun getTime(ms: Long) : String = SimpleDateFormat("dd MMM HH:mm").format(Date(ms))
}