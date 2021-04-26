package com.example.conference.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.databinding.ContactItemViewBinding
import com.example.conference.json.ContactEntityWithStatus
import com.example.conference.server.api.ConferenceAPIProvider
import com.squareup.picasso.Picasso

class ConferenceSettingsRecyclerViewAdapter(
    var conferenceMembers: List<ContactEntityWithStatus>,
    private val callback: (ContactEntityWithStatus) -> Unit):
    RecyclerView.Adapter<ConferenceSettingsRecyclerViewAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ContactItemViewBinding.inflate(inflater, parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(conferenceMembers[position])
    }

    override fun getItemCount() = conferenceMembers.size

    inner class MemberViewHolder(private val binding: ContactItemViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: ContactEntityWithStatus) {
            binding.apply {
                Picasso
                    .get()
                    .load(ConferenceAPIProvider.BASE_URL +
                            "/user/avatar/download/?id=" +
                            member.email.hashCode())
                    .centerCrop()
                    .fit()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(contactAvatarIv)
                contactNameTv.text = member.name
                contactSurnameTv.text = member.surname
                contactEmailTv.text = member.email
                if (member.status == 0)
                    contactChoseImg.isVisible = false
                else
                    contactChoseImg.setImageResource(R.drawable.outline_grade_24)
                itemView.setOnLongClickListener {
                    callback(member)
                    true
                }
            }
        }
    }
}