package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.entity.CMessageMinimal
import com.example.conference.db.entity.DMessageMinimal
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.service.Http
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_conference_settings.*
import kotlinx.android.synthetic.main.group_message_item_view.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DialoguesRecyclerViewAdapter(
    var dialogues: List<DialogueEntity>,
    var lastMessages: HashMap<Int, DMessageMinimal>,
    private val callback: (Int) -> Unit
): RecyclerView.Adapter<DialoguesRecyclerViewAdapter.DialoguesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialoguesViewHolder {
        return DialoguesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.group_message_item_view, parent, false))
    }

    override fun onBindViewHolder(holder: DialoguesViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount() = dialogues.size

    inner class DialoguesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var avatar: ImageView = itemView.groupAvatarImage
        var name: TextView = itemView.groupNameTV
        var lastMessage: TextView = itemView.groupLastMessageTV
        var time: TextView = itemView.lastMessageTimeTV
        fun bind(position: Int) {
            Picasso.get()
                .load("${Http.baseURL}/user/avatar/download/?id=${dialogues[position].second_user_id}")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            name.text = (dialogues[position].second_user_name + " " + dialogues[position].second_user_surname)
            lastMessage.text = lastMessages[dialogues[position].id]!!.text
            time.text =
                if (lastMessages[dialogues[position].id]!!.date_time == 0.toLong())
                    ""
                else
                    getTime(lastMessages[dialogues[position].id]!!.date_time)
            itemView.setOnClickListener {
                callback.invoke(dialogues[position].id)
            }
        }

        private fun getTime(ms: Long) : String = SimpleDateFormat("dd MMM HH:mm").format(Date(ms))
    }
}