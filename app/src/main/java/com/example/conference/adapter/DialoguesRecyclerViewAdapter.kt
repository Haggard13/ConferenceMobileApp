package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.databinding.GroupMessageItemViewBinding
import com.example.conference.db.entity.DialogueEntity
import com.example.conference.server.Server
import com.example.conference.util.LongUtils.toTime
import com.squareup.picasso.Picasso

class DialoguesRecyclerViewAdapter(
    var dialogues: List<DialogueEntity>,
    private val callback: (Int) -> Unit
): RecyclerView.Adapter<DialoguesRecyclerViewAdapter.DialoguesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialoguesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val dialogueBinding = GroupMessageItemViewBinding.inflate(inflater, parent, false)
        return DialoguesViewHolder(dialogueBinding)
    }

    override fun onBindViewHolder(holder: DialoguesViewHolder, position: Int) =
        holder.bind(dialogues[position])

    override fun getItemCount() = dialogues.size

    inner class DialoguesViewHolder(private val binding: GroupMessageItemViewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(dialogue: DialogueEntity) {
            Picasso.get()
                .load(Server.baseURL
                        + "/user/avatar/download/?id="
                        + dialogue.second_user_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(binding.groupAvatarImage)
            binding.apply{
                groupNameTV.text = (dialogue.second_user_name + " " + dialogues[position].second_user_surname)
                lastMessageTimeTV.text = dialogue.last_message
                lastMessageTimeTV.text = dialogue.last_message_time.toTime()
                root.setOnClickListener {
                    callback.invoke(dialogue.id)
                }
            }
        }
    }
}