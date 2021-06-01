package com.example.conference.activity

import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.conference.R
import com.example.conference.account.Account
import com.example.conference.server.PeerConnectionObserver
import com.example.conference.server.SdpObserverImpl
import com.example.conference.server.websocket.WebSocketHandler
import com.example.conference.server.websocket.dto.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_meet.*
import kotlinx.coroutines.*
import org.webrtc.*
import org.webrtc.PeerConnectionFactory.InitializationOptions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import com.example.conference.server.websocket.dto.IceCandidate as IceCandt


class MeetActivity: AppCompatActivity(), WebSocketHandler.SocketListener {

    private var conferenceID: Int = 0
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private val peers = HashMap<Int, PeerConnection>()
    private val views = HashMap<Int, SurfaceViewRenderer>()
    private lateinit var localVideoTrack: VideoTrack
    private lateinit var localAudioTrack: AudioTrack
    private lateinit var localStream: MediaStream
    private lateinit var rootEglBase: EglBase
    private var videoCapturerAndroid: CameraVideoCapturer? = null
    private lateinit var socket: WebSocketHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet)

        membersFragment.requireView().visibility = INVISIBLE
        chatFragment.requireView().visibility = INVISIBLE
        roomFragment.requireView().visibility = INVISIBLE

        conferenceID = intent.extras?.getInt("conferenceID")!!

        initVideos()
        start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        super.onDestroy()
        onHangupCallClick(Button(this))
    }


    private fun initVideos() {
        rootEglBase = EglBase.create()
        localViewRenderer.init(rootEglBase.eglBaseContext, null)
        localViewRenderer.setZOrderMediaOverlay(true)
    }

    private fun start() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val initializationOptions = InitializationOptions.builder(this)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        videoCapturerAndroid = createCameraCapturer()
        startCapturing()

        socket = WebSocketHandler(this@MeetActivity) {
            createPeerPool()
        }
    }

    private fun startCapturing() {
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
        val videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid!!.isScreencast)
        videoCapturerAndroid!!.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)

        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)

        videoCapturerAndroid?.startCapture(640, 480, 24)

        localViewRenderer.setMirror(true)

        runOnUiThread { localVideoTrack.addSink(localViewRenderer) }
    }

    private fun createPeerPool() =
        socket.join(JoiningMessage("join", conferenceID, Account(applicationContext).id))

    private fun createPeer(id: Int) = peerConnectionFactory.createPeerConnection(
        getIceServers(),
        object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                socket.sendIceCandidate(
                    IceCandt(
                        id = "sendIceCandidate",
                        conferenceID,
                        userId = id,
                        senderId = Account(applicationContext).id,
                        iceCandidate = Gson().toJson(p0)
                    )
                )
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onAddStream(p0: MediaStream?) =
                onGotRemoteStream(p0!!, id)


            override fun onRemoveStream(p0: MediaStream?) {
                runOnUiThread { usersVideoLL.removeViewInLayout(views[id]) }
                views.remove(id)
            }
        }
    )

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(this)
        val deviceNames = enumerator.deviceNames
        deviceNames
            .filter { enumerator.isFrontFacing(it) }
            .mapNotNull { enumerator.createCapturer(it, null) }
            .forEach { return it }

        return null
    }

    private fun addStreamToLocalPeer(peerConnection: PeerConnection) {
        localStream = peerConnectionFactory.createLocalMediaStream("102")
        localAudioTrack.setVolume(100.0)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

        peerConnection.addStream(localStream)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onGotRemoteStream(stream: MediaStream, id: Int) {
        val videoTrack = stream.videoTracks[0]
        runOnUiThread {
            try {
                val display = windowManager.defaultDisplay
                val metricsB = DisplayMetrics()
                display.getMetrics(metricsB)

                getSystemService(AudioManager::class.java) .apply {
                    mode = AudioManager.MODE_IN_COMMUNICATION
                    ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }

                val remoteView = SurfaceViewRenderer(this)

                remoteView.apply {
                    init(rootEglBase.eglBaseContext, null)
                    setZOrderMediaOverlay(true)
                    layoutParams =
                        LayoutParams(metricsB.widthPixels, metricsB.heightPixels)
                    setOnClickListener(this@MeetActivity::onRemoteViewClick)
                }

                views[id] = remoteView

                usersVideoLL.addView(remoteView)

                getSystemService(AudioManager::class.java).isSpeakerphoneOn = true

                videoTrack.addSink(remoteView)


                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) { onStopBroadcast(videoBroadcastIB) }
                    delay(700)
                    withContext(Dispatchers.Main) { onStopBroadcast(videoBroadcastIB) }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getIceServers(): MutableList<PeerConnection.IceServer> {
        val iceServers: MutableList<PeerConnection.IceServer> = ArrayList()
        iceServers.apply {
            add(PeerConnection.IceServer.builder("stun:stun01.sipphone.com").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.ekiga.net").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.fwdnet.net").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.ideasip.com").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.iptel.org").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.rixtelecom.se").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.schlund.de").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("turn:numb.viagenie.ca").setUsername("webrtc@live.com").setPassword("muazkh").createIceServer())
            add(PeerConnection.IceServer.builder("turn:192.158.29.39:3478?transport=udp").setUsername("28224511:1379330808").setPassword("JZEOEt2V3Qb0y27GRntt2u2PAYA=").createIceServer())
            add(PeerConnection.IceServer.builder("turn:192.158.29.39:3478?transport=tcp").setUsername("28224511:1379330808").setPassword("JZEOEt2V3Qb0y27GRntt2u2PAYA=").createIceServer())
        }
        return iceServers
    }

    //region ClickListeners
    @RequiresApi(Build.VERSION_CODES.M)
    fun onHangupCallClick(v: View) {
        peers.forEach {
            it.value.removeStream(localStream)
            it.value.close()
        }
        socket.close()
        videoCapturerAndroid?.dispose()
        getSystemService(AudioManager::class.java) .apply {
            mode = AudioManager.MODE_NORMAL
            ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onMuteMicClick(v: View) {
        localAudioTrack.setEnabled(!localAudioTrack.enabled())
        if (localAudioTrack.enabled()) {
            (v as ImageButton).setImageResource(R.drawable.outline_mic_24)
        } else {
            (v as ImageButton).setImageResource(R.drawable.outline_mic_off_24)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onBackClick(v: View) = onHangupCallClick(v)

    fun onSwitchVideoClick(v: View) = videoCapturerAndroid?.switchCamera(null)

    fun onStopBroadcast(v: View) {
        localVideoTrack.setEnabled(!localVideoTrack.enabled())
        if (localVideoTrack.enabled()) {
            (v as ImageButton).setImageResource(R.drawable.outline_videocam_24)
            localViewRenderer.isVisible = true
        } else {
            (v as ImageButton).setImageResource(R.drawable.outline_videocam_off_24)
            localViewRenderer.isVisible = false
        }
    }

    fun onRemoteViewClick(v: View) {
        if (meet_tool_bar.isVisible) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.hide_button_anim)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    bottomNavigationView.isVisible = false
                    meet_tool_bar.isVisible = false
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            animation.repeatCount = 1

            bottomNavigationView.startAnimation(animation)
            meet_tool_bar.startAnimation(animation)
        } else {
            val animation = AnimationUtils.loadAnimation(this, R.anim.show_button_anim)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    bottomNavigationView.isVisible = true
                    meet_tool_bar.isVisible = true
                }

                override fun onAnimationEnd(animation: Animation?) {}

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            animation.repeatCount = 1

            bottomNavigationView.startAnimation(animation)
            meet_tool_bar.startAnimation(animation)
        }
    }

    fun onMembersClick(v: View) {
        membersFragment.requireView().visibility = VISIBLE
        chatFragment.requireView().visibility = INVISIBLE
        roomFragment.requireView().visibility = INVISIBLE
    }

    fun onChatClick(v: View) {
        membersFragment.requireView().visibility = INVISIBLE
        chatFragment.requireView().visibility = VISIBLE
        roomFragment.requireView().visibility = INVISIBLE
    }

    fun onRoomClick(v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.inflate(R.menu.room_choose_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.chatItem -> {
                    membersFragment.requireView().visibility = INVISIBLE
                    chatFragment.requireView().visibility = INVISIBLE
                    roomFragment.requireView().visibility = VISIBLE
                    true
                }
                R.id.microItem -> {
                    true
                }
                else -> false
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }

        popupMenu.show()
    }
    //endregion

    //region SocketListener
    override fun onJoin(members: Members) {
        members.members.forEach {
            val peerConnection: PeerConnection = createPeer(it)!!
            peers[it] = peerConnection

            addStreamToLocalPeer(peerConnection)

            peerConnection.createOffer(object : SdpObserverImpl() {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    socket.sendOffer(
                        Offer(
                            "sendOffer",
                            conferenceID,
                            it,
                            Account(applicationContext).id,
                            Gson().toJson(p0)
                        )
                    )
                    peerConnection.setLocalDescription(SdpObserverImpl(), p0)
                }
            }, MediaConstraints())
        }
    }

    override fun onOffer(offer: Offer2) {
        val peerConnection: PeerConnection = createPeer(offer.userId)!!
        peers[offer.userId] = peerConnection

        addStreamToLocalPeer(peerConnection)

        peerConnection.setRemoteDescription(SdpObserverImpl(),
            Gson().fromJson(offer.offer, SessionDescription::class.java)
        )

        peerConnection.createAnswer(
            object : SdpObserverImpl() {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    socket.sendAnswer(
                        Answer(
                            "sendAnswer",
                            conferenceID,
                            Account(applicationContext).id,
                            offer.userId,
                            Gson().toJson(p0)
                        )
                    )
                    peerConnection.setLocalDescription(SdpObserverImpl(), p0)
                }
            },
            MediaConstraints()
        )
    }

    override fun onAnswer(answer: Answer2) {
        peers[answer.userId]
            ?.setRemoteDescription(
                SdpObserverImpl(),
                Gson().fromJson(answer.answer, SessionDescription::class.java)
            )
    }

    override fun onIceCandidate(ice: IceCandidate2) {
        peers[ice.userId]
            ?.addIceCandidate(Gson().fromJson(ice.iceCandidate, IceCandidate::class.java))
    }

    override fun onLeaving(user: LeavingUser) {
        Toast.makeText(this, "left", Toast.LENGTH_SHORT).show()
        peers[user.userId]?.close()
        peers.remove(user.userId)
    }
    //endregion
}