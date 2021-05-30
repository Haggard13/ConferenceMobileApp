package com.example.conference.activity

import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
import org.webrtc.*
import org.webrtc.PeerConnectionFactory.InitializationOptions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import com.example.conference.server.websocket.dto.IceCandidate as IceCandt


class MeetActivity: AppCompatActivity(), WebSocketHandler.SocketListener {
    private val tag = "MeetActivity"

    private var conferenceID: Int = 0

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var sdpConstraints: MediaConstraints
    private val peers = HashMap<Int, PeerConnection>()

    private lateinit var videoSource: VideoSource
    private lateinit var localVideoTrack: VideoTrack

    private lateinit var audioConstraints: MediaConstraints
    private lateinit var audioSource: AudioSource
    private lateinit var localAudioTrack: AudioTrack
    private lateinit var rootEglBase: EglBase

    private var videoCapturerAndroid: CameraVideoCapturer? = null

    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()

    private lateinit var socket: WebSocketHandler

    private var isBroadcastRun = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet)

        membersFragment.requireView().visibility = INVISIBLE
        chatFragment.requireView().visibility = INVISIBLE
        roomFragment.requireView().visibility = INVISIBLE

        conferenceID = intent.extras?.getInt("conferenceID")!!

        initVideos()
        initSdpConstraint()
        start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        super.onDestroy()
        onHangupCallClick(Button(this))
    }

    //region Init
    private fun initVideos() {
        rootEglBase = EglBase.create()

        localViewRenderer.init(rootEglBase.eglBaseContext, null)
        remoteViewRenderer.init(rootEglBase.eglBaseContext, null)

        localViewRenderer.setZOrderMediaOverlay(true)
        remoteViewRenderer.setZOrderMediaOverlay(true)
    }

    private fun initSdpConstraint() {
        sdpConstraints = MediaConstraints()
        sdpConstraints.mandatory.apply {
            add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
    }
    //endregion

    private fun start() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        peerIceServers.apply {
            add(PeerConnection.IceServer("stun:stun01.sipphone.com"))
            add(PeerConnection.IceServer("stun:stun.ekiga.net"))
            add(PeerConnection.IceServer("stun:stun.fwdnet.net"))
            add(PeerConnection.IceServer("stun:stun.ideasip.com"))
            add(PeerConnection.IceServer("stun:stun.iptel.org"))
            add(PeerConnection.IceServer("stun:stun.rixtelecom.se"))
            add(PeerConnection.IceServer("stun:stun.schlund.de"))
            add(PeerConnection.IceServer("stun:stun.l.google.com:19302"))
            add(PeerConnection.IceServer("stun:stun1.l.google.com:19302"))
            add(PeerConnection.IceServer("stun:stun2.l.google.com:19302"))
            add(PeerConnection.IceServer("stun:stun3.l.google.com:19302"))
            add(PeerConnection.IceServer("stun:stun4.l.google.com:19302"))
            add(PeerConnection.IceServer("turn:numb.viagenie.ca", "webrtc@live.com", "muazkh"))
            add(PeerConnection.IceServer("turn:192.158.29.39:3478?transport=udp", "28224511:1379330808", "JZEOEt2V3Qb0y27GRntt2u2PAYA="))
            add(PeerConnection.IceServer("turn:192.158.29.39:3478?transport=tcp", "28224511:1379330808", "JZEOEt2V3Qb0y27GRntt2u2PAYA="))
        }

        val initializationOptions = InitializationOptions.builder(this)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,  /* enableIntelVp8Encoder */
            true,  /* enableH264HighProfile */
            true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        videoCapturerAndroid = createCameraCapturer()
        Log.e(tag, "video capturer created")
        audioConstraints = MediaConstraints()

        startCapturing()

        socket = WebSocketHandler(this@MeetActivity) {
            createPeerPool()
        }
    }

    private fun startCapturing() {
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
        videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid!!.isScreencast)
        videoCapturerAndroid!!.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)

        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)

        audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)

        videoCapturerAndroid?.startCapture(1024, 720, 30)

        localViewRenderer.setMirror(true)

        localVideoTrack .addSink(localViewRenderer)
    }

    private fun createPeerPool() =
        socket.join(JoiningMessage("join", conferenceID, Account(applicationContext).id))

    private fun addStreamToLocalPeer(peerConnection: PeerConnection) {
        val localStream = peerConnectionFactory.createLocalMediaStream("102")
        localStream?.addTrack(localVideoTrack)
        localStream?.addTrack(localAudioTrack)

        peerConnection.addStream(localStream)
    }

    private fun createScreenCapture(): ScreenCapturerAndroid? {
        return null
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(this)
        val deviceNames = enumerator.deviceNames
        deviceNames
            .filter { enumerator.isFrontFacing(it) }
            .mapNotNull { enumerator.createCapturer(it, null) }
            .forEach { return it }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onGotRemoteStream(stream: MediaStream) {
        val videoTrack = stream.videoTracks[0]
        runOnUiThread {
            try {
                getSystemService(AudioManager::class.java) .apply {
                    mode = AudioManager.MODE_IN_COMMUNICATION
                    ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }

                videoTrack.addSink(remoteViewRenderer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createPeer(id: Int) = peerConnectionFactory.createPeerConnection(
            peerIceServers,
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
                override fun onAddStream(p0: MediaStream?) = onGotRemoteStream(p0!!)

                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                    if (p0?.track()?.id() == "103") {
                        (p0.track() as VideoTrack).addSink(remoteViewRendererScreen)
                    }
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    if (transceiver!!.receiver!!.track()!!
                            .id() == "103" && !transceiver.receiver!!.track()!!
                            .enabled()
                    )
                        (transceiver.receiver.track() as VideoTrack).removeSink(
                            remoteViewRendererScreen
                        )

                }
            }
        )

    //region ClickListeners
    @RequiresApi(Build.VERSION_CODES.M)
    fun onHangupCallClick(v: View) {
        peers.forEach { it.value.close() }
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
            peerConnection.setAudioPlayout(true)
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