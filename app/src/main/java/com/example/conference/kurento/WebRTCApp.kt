package com.example.conference.kurento

import android.content.Context
import fi.vtt.nubomedia.kurentoroomclientandroid.KurentoRoomAPI
import fi.vtt.nubomedia.webrtcpeerandroid.NBMMediaConfiguration
import fi.vtt.nubomedia.webrtcpeerandroid.NBMPeerConnection
import fi.vtt.nubomedia.webrtcpeerandroid.NBMWebRTCPeer
import org.webrtc.*

class WebRTCApp(
    context: Context,
    private val kurentoRoomAPI: KurentoRoomAPI) : NBMWebRTCPeer.Observer{

    private val nbmWebRTCPeer: NBMWebRTCPeer = NBMWebRTCPeer(
        NBMMediaConfiguration(),
        context,
        {  },
        this)
    private var iceSent = false

    init {
        nbmWebRTCPeer.initialize()
    }

    override fun onInitialize() {
        nbmWebRTCPeer.generateOffer("local", true) //fixme(вызывает ошибку)
    }

    override fun onLocalSdpOfferGenerated(
        localSdpOffer: SessionDescription?,
        connection: NBMPeerConnection?
    ) {
        kurentoRoomAPI.sendPublishVideo(
            localSdpOffer?.description
                ?: throw NullPointerException("Local SDP Offer is null"),
        true, 154)
    }

    override fun onLocalSdpAnswerGenerated(
        localSdpAnswer: SessionDescription?,
        connection: NBMPeerConnection?
    ) {

    }

    override fun onIceCandidate(localIceCandidate: IceCandidate, connection: NBMPeerConnection?) =
        if (!iceSent) {
            kurentoRoomAPI.sendOnIceCandidate(
                "MyUsername",
                localIceCandidate.sdp,
                localIceCandidate.sdpMid,
                localIceCandidate.sdpMLineIndex.toString(),
                126)
            iceSent = true;
        } else {
            kurentoRoomAPI.sendOnIceCandidate(
                "MyRoomPeer",
                localIceCandidate.sdp,
                localIceCandidate.sdpMid,
                localIceCandidate.sdpMLineIndex.toString(),
                126)
        }

    override fun onIceStatusChanged(
        state: PeerConnection.IceConnectionState?,
        connection: NBMPeerConnection?
    ) {

    }

    override fun onRemoteStreamAdded(stream: MediaStream?, connection: NBMPeerConnection?) {

    }

    override fun onRemoteStreamRemoved(stream: MediaStream?, connection: NBMPeerConnection?) {

    }

    override fun onPeerConnectionError(error: String?) {

    }

    override fun onDataChannel(dataChannel: DataChannel?, connection: NBMPeerConnection?) {

    }

    override fun onBufferedAmountChange(
        l: Long,
        connection: NBMPeerConnection?,
        channel: DataChannel?
    ) {

    }

    override fun onStateChange(connection: NBMPeerConnection?, channel: DataChannel?) {

    }

    override fun onMessage(
        buffer: DataChannel.Buffer?,
        connection: NBMPeerConnection?,
        channel: DataChannel?
    ) {

    }
}