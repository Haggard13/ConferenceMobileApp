package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.databinding.GroupMessageItemViewBinding
import com.example.conference.db.entity.ConferenceEntity
import com.example.conference.server.Server
import com.example.conference.util.LongUtils.toTime
import com.squareup.picasso.Picasso

class ConferencesRecyclerViewAdapter(
    var conferences: List<ConferenceEntity>,
    private val callback: (Int) -> Unit
): RecyclerView.Adapter<ConferencesRecyclerViewAdapter.ConferencesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConferencesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val conferenceBinding = GroupMessageItemViewBinding.inflate(inflater, parent, false)
        return ConferencesViewHolder(conferenceBinding)
    }

    override fun onBindViewHolder(holder: ConferencesViewHolder, position: Int) =
        holder.bind(conferences[position])


    override fun getItemCount() = conferences.size;

    inner class ConferencesViewHolder(private val binding: GroupMessageItemViewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(conference: ConferenceEntity) {
            Picasso.get()
                .load(Server.baseURL
                        + "/conference/avatar/download/?id="
                        + conference.id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(binding.groupAvatarImage)

            binding.apply {
                groupNameTV.text = conference.name
                groupLastMessageTV.text = conference.last_message
                lastMessageTimeTV.text = conference.last_message_time.toTime()
                root.setOnClickListener {
                    callback.invoke(conference.id)
                }
            }
        }
    }
}