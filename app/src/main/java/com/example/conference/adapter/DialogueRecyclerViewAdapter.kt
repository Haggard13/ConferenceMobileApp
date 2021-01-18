package com.example.conference.adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.conference.R
import com.example.conference.db.data.SenderEnum
import com.example.conference.db.entity.DMessageEntity
import com.example.conference.service.Http
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_item_view.view.*
import kotlinx.android.synthetic.main.message_item_view_audio_message.view.*
import kotlinx.android.synthetic.main.message_item_view_with_photo.view.*
import kotlinx.android.synthetic.main.your_message_item_view.view.*
import kotlinx.android.synthetic.main.your_message_item_view_audio_message.view.*
import kotlinx.android.synthetic.main.your_message_item_view_with_photo.view.*
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.RecyclerView.Adapter as RVAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder as RVViewHolder

class DialogueRecyclerViewAdapter(
    var messages: List<DMessageEntity>,
    val context: Context,
    val callbackForPhoto: (Int) -> Unit
) : RVAdapter<RVViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVViewHolder {
        return when(viewType) {
            0 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.your_message_item_view, parent, false)
                YourMessagesViewHolder(itemView)
            }
            1 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.your_message_item_view_with_photo, parent, false)
                WPYourMessagesViewHolder(itemView)
            }
            2 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.your_message_item_view_audio_message, parent, false)
                AMYourMessagesViewHolder(itemView)
            }
            3 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_item_view, parent, false)
                MessagesViewHolder(itemView)
            }
            4 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_item_view_with_photo, parent, false)
                WPMessagesViewHolder(itemView)
            }
            5 -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_item_view_audio_message, parent, false)
                AMMessagesViewHolder(itemView)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)){
            0 -> with(holder as DialogueRecyclerViewAdapter.YourMessagesViewHolder) {
                bind(position)
            }
            1 -> with(holder as DialogueRecyclerViewAdapter.WPYourMessagesViewHolder) {
                bind(position)
            }
            2 -> with(holder as DialogueRecyclerViewAdapter.AMYourMessagesViewHolder) {
                bind(position)
            }
            3 -> with(holder as DialogueRecyclerViewAdapter.MessagesViewHolder) {
                bind(position)
            }
            4 -> with(holder as DialogueRecyclerViewAdapter.WPMessagesViewHolder) {
                bind(position)
            }
            5 -> with(holder as DialogueRecyclerViewAdapter.AMMessagesViewHolder) {
                bind(position)
            }
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int =
        when (messages[position].sender_enum) {
            SenderEnum.USER.ordinal ->
                when (messages[position].type) {
                    1 -> 0
                    2 -> 1
                    3 -> 2
                    else -> -1
                }

            SenderEnum.NOT_USER.ordinal ->
                when (messages[position].type) {
                    1 -> 3
                    2 -> 4
                    3 -> 5
                    else -> -1
                }
            else -> -1
        }

    inner class MessagesViewHolder(itemView: View) : RVViewHolder(itemView) {
        var avatar: ImageView = itemView.memberAvatarIV
        var date: TextView = itemView.memberMessageTimeTV
        var message: TextView = itemView.memberMessageTV
        var name: TextView = itemView.memberName

        fun bind(p: Int) {
            Picasso.get()
                .load("${Http.baseURL}/user/avatar/download/?id=${messages[p].sender_id}")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            date.text = getTime(messages[p].date_time)
            message.text = messages[p].text
            name.text = messages[p].sender_name
        }
    }

    inner class WPMessagesViewHolder(
        itemView: View,
        var avatar: ImageView = itemView.WPmemberAvatarIV,
        private var date: TextView = itemView.WPmemberMessageTimeTV,
        private var message: TextView = itemView.WPmemberMessageTV,
        var name: TextView = itemView.WPmemberName
    ) : RVViewHolder(itemView) {
        fun bind(p: Int) {
            //region Picasso
            Picasso.get()
                .load(Http.baseURL + "/user/avatar/download/?id=" + messages[p].sender_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            //endregion
            //region Picasso
            Picasso.get()
                .load(Http.baseURL + "/dialogue/getPhotography/?id=" + messages[p].id)
                .placeholder(R.drawable.photo)
                .error(R.drawable.photo)
                .fit()
                .centerCrop()
                .into(itemView.WPmemberPhotoInMessageIV)
            //endregion
            date.text = getTime(messages[p].date_time)
            message.text = messages[p].text
            name.text = (messages[p].sender_name + " " + messages[p].sender_surname)
            itemView.WPmemberPhotoInMessageIV.setOnClickListener {
                callbackForPhoto(messages[p].id)
            }
        }
    }

    inner class YourMessagesViewHolder(itemView: View) : RVViewHolder(itemView) {
        var avatar: ImageView = itemView.userAvatarIV
        var date: TextView = itemView.userMessageTimeTV
        var message: TextView = itemView.userMessageTV

        fun bind(p: Int) {
            Picasso.get()
                .load("${Http.baseURL}/user/avatar/download/?id=${messages[p].sender_id}")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            date.text = getTime(messages[p].date_time)
            message.text = messages[p].text
        }
    }

    inner class WPYourMessagesViewHolder(itemView: View,
                                         var avatar: ImageView = itemView.WPuserAvatarIV,
                                         private var date: TextView = itemView.WPuserMessageTimeTV,
                                         private var message: TextView = itemView.WPuserMessageTV
    ) : RVViewHolder(itemView) {

        fun bind(p: Int) {
            //region Picasso
            Picasso.get()
                .load(Http.baseURL + "/user/avatar/download/?id=" + messages[p].sender_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            //endregion
            //region Picasso
            Picasso.get()
                .load(Http.baseURL + "/dialogue/getPhotography/?id=" + messages[p].id)
                .placeholder(R.drawable.photo)
                .error(R.drawable.photo)
                .fit()
                .centerCrop()
                .into(itemView.WPuserPhotoInMessageIV)
            //endregion
            date.text = getTime(messages[p].date_time)
            message.text = messages[p].text
            itemView.WPuserPhotoInMessageIV.setOnClickListener {
                callbackForPhoto(messages[p].id)
            }
        }
    }

    inner class AMMessagesViewHolder(
        itemView: View,
        var avatar: ImageView = itemView.AMmemberAvatarIV,
        var date: TextView = itemView.AMmemberMessageTimeTV,
        var name: TextView = itemView.AMmemberName
    ) : RVViewHolder(itemView) {

        lateinit var mp: MediaPlayer
        var p: Int = 0
        private val handler = Handler()
        lateinit var runnable: Runnable

        fun bind(p: Int) {
            //region Picasso
            Picasso.get()
                .load(Http.baseURL + "/user/avatar/download/?id=" + messages[p].sender_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            //endregion
            date.text = getTime(messages[p].date_time)
            this.p = p
            itemView.AMmemberPlayIB.setOnClickListener(this::onPlay)
            itemView.AMmemberProgressAudioSB.isClickable = false
            name.text = messages[p].sender_name
        }

        private fun onPlay(v: View) {
            v.isEnabled = false

            mp = MediaPlayer()
            with(mp) {
                setDataSource(
                    context,
                    Uri.parse("${Http.baseURL}/dialogue/getAudioMessage/?id=${messages[p].id}")
                )
                setOnCompletionListener {
                    (v as ImageButton).setImageResource(R.drawable.play)
                }
                prepare()
                start()
            }

            v.setOnClickListener(this::onStop)
            (v as ImageButton).setImageResource(R.drawable.stop)


            itemView.AMmemberProgressAudioSB.max = mp.duration / 1000
            runnable = Runnable {
                itemView.AMmemberProgressAudioSB.progress = mp.currentPosition / 1000
                handler.postDelayed(runnable, 1000)
            }
            handler.postDelayed(runnable, 1000)

            v.isEnabled = true
        }

        private fun onStop(v: View) {
            v.isEnabled = false

            mp.stop()
            mp.reset()

            handler.removeCallbacks(runnable)

            (v as ImageButton).setImageResource(R.drawable.play)
            v.setOnClickListener(this::onPlay)

            v.isEnabled = true
        }
    }

    inner class AMYourMessagesViewHolder(
        itemView: View,
        var avatar: ImageView = itemView.AMuserAvatarIV,
        var date: TextView = itemView.AMuserMessageTimeTV,
    ) : RVViewHolder(itemView) {

        lateinit var mp: MediaPlayer
        var p: Int = 0
        private val handler = Handler()
        lateinit var runnable: Runnable

        fun bind(p: Int) {
            //region Picasso
            Picasso.get()
                .load(Http.baseURL + "/user/avatar/download/?id=" + messages[p].sender_id)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(avatar)
            //endregion
            date.text = getTime(messages[p].date_time)
            this.p = p
            itemView.AMuserPlayIB.setOnClickListener(this::onPlay)
            itemView.AMuserProgressAudioSB.isClickable = false
        }

        private fun onPlay(v: View) {
            v.isEnabled = false

            mp = MediaPlayer()
            with(mp) {
                setDataSource(
                    context,
                    Uri.parse("${Http.baseURL}/dialogue/getAudioMessage/?id=${messages[p].id}")
                )
                setOnCompletionListener {
                    (v as ImageButton).setImageResource(R.drawable.play)
                }
                prepare()
                start()
            }

            v.setOnClickListener(this::onStop)
            (v as ImageButton).setImageResource(R.drawable.stop)


            itemView.AMuserProgressAudioSB.max = mp.duration / 1000
            runnable = Runnable {
                itemView.AMuserProgressAudioSB.progress = mp.currentPosition / 1000
                handler.postDelayed(runnable, 1000)
            }
            handler.postDelayed(runnable, 1000)

            v.isEnabled = true
        }

        private fun onStop(v: View) {
            v.isEnabled = false

            mp.stop()
            mp.reset()

            handler.removeCallbacks(runnable)

            (v as ImageButton).setImageResource(R.drawable.play)
            v.setOnClickListener(this::onPlay)

            v.isEnabled = true
        }
    }

    private fun getTime(ms: Long) : String = SimpleDateFormat("dd MMM HH:mm").format(Date(ms))
}