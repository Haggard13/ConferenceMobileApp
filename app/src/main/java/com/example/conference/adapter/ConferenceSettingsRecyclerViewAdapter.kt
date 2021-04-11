package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.json.ContactEntityWithStatus
import com.example.conference.server.Server
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.contact_item_view.view.*

class ConferenceSettingsRecyclerViewAdapter(
    var conferenceMembers: List<ContactEntityWithStatus>,
    private val callback: (ContactEntityWithStatus) -> Unit):
    RecyclerView.Adapter<ConferenceSettingsRecyclerViewAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.contact_item_view, parent, false))
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = conferenceMembers.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(p: Int) {
            Picasso
                .get()
                .load("${Server.baseURL}/user/avatar/download/?id=${conferenceMembers[p].email.hashCode()}")
                .centerCrop()
                .fit()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(itemView.contact_avatar_iv)
            itemView.contact_name_tv.text = conferenceMembers[p].name
            itemView.contact_surname_tv.text = conferenceMembers[p].surname
            itemView.contact_email_tv.text = conferenceMembers[p].email
            if (conferenceMembers[p].status == 0)
                itemView.contactChoseImg.visibility = View.INVISIBLE
            else
                itemView.contactChoseImg.setImageResource(R.drawable.admin)
            itemView.setOnLongClickListener {
                callback(conferenceMembers[p])
                true
            }
        }
    }
}