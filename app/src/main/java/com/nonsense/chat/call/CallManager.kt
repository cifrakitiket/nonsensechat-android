package com.nonsense.chat.call

import android.content.Context
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.IceEntry
import com.nonsense.chat.data.CallSession
import com.nonsense.chat.data.repos.CallRepository
import com.nonsense.chat.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Per-participant live state for the call UI. */
data class CallPeer(
    val uid: String,
    val muted: Boolean = false,
    val video: Boolean = false,
    val videoTrack: VideoTrack? = null,
)

/**
 * Full-mesh WebRTC call engine + signalling orchestrator. One [CallManager] backs the whole app
 * (a @Singleton) so the call survives navigation and feeds both the call screen and the minimized
 * bar. Signalling mirrors the web client over the `call_sessions` doc (offers from the lexically
 * smaller uid to avoid glare).
 */
@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calls: CallRepository,
    private val account: AccountManager,
    @AppScope private val scope: CoroutineScope,
) {
    val eglBase: EglBase by lazy { EglBase.create() }

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId

    private val _peers = MutableStateFlow<Map<String, CallPeer>>(emptyMap())
    val peers: StateFlow<Map<String, CallPeer>> = _peers

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack

    val muted = MutableStateFlow(false)
    val videoEnabled = MutableStateFlow(false)
    val speakerOn = MutableStateFlow(true)
    val title = MutableStateFlow("")
    val startedAt = MutableStateFlow(0L)

    private var factory: PeerConnectionFactory? = null
    private val connections = HashMap<String, PeerConnection>()
    private var audioSource: org.webrtc.AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoSource: VideoSource? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceHelper: SurfaceTextureHelper? = null

    private val handledSdp = HashSet<String>()
    private val iceCounts = HashMap<String, Int>()
    private val pendingIce = HashMap<String, MutableList<IceEntry>>()
    private var sessionJob: Job? = null

    private val myUid get() = account.uid.orEmpty()

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
            .setUsername("openrelayproject").setPassword("openrelayproject").createIceServer(),
    )

    // ── Public API ─────────────────────────────────────────────────────────────

    fun start(chatId: String, video: Boolean, title: String, asCaller: Boolean) {
        if (_activeChatId.value != null) return
        this.title.value = title
        _activeChatId.value = chatId
        startedAt.value = System.currentTimeMillis()
        videoEnabled.value = video
        ensureFactory()
        createLocalMedia(video)
        CallService.start(context)
        scope.launch {
            calls.join(chatId, myUid, video, asCaller)
            sessionJob = scope.launch {
                calls.observe(chatId).collect { s -> if (s != null) onSession(s) else if (_activeChatId.value == chatId) hangUp() }
            }
        }
    }

    fun toggleMute() {
        val m = !muted.value
        muted.value = m
        localAudioTrack?.setEnabled(!m)
        _activeChatId.value?.let { id -> scope.launch { calls.setMuted(id, myUid, m) } }
    }

    fun toggleVideo() {
        val v = !videoEnabled.value
        videoEnabled.value = v
        if (v && localAudioTrack != null && videoCapturer == null) createVideo()
        localVideoTrack.value?.setEnabled(v)
        runCatching { if (v) videoCapturer?.startCapture(1280, 720, 30) else videoCapturer?.stopCapture() }
        _activeChatId.value?.let { id -> scope.launch { calls.setVideo(id, myUid, v) } }
    }

    fun switchCamera() {
        (videoCapturer as? CameraVideoCapturer)?.switchCamera(null)
    }

    fun toggleSpeaker() { speakerOn.value = !speakerOn.value }

    fun hangUp() {
        val chatId = _activeChatId.value ?: return
        val others = _peers.value.keys.filter { it != myUid }
        scope.launch { runCatching { calls.leave(chatId, myUid, lastOne = others.isEmpty()) } }
        sessionJob?.cancel(); sessionJob = null
        connections.values.forEach { runCatching { it.close() } }
        connections.clear()
        runCatching { videoCapturer?.stopCapture() }
        runCatching { videoCapturer?.dispose() }
        runCatching { surfaceHelper?.dispose() }
        runCatching { localAudioTrack?.dispose() }
        runCatching { videoSource?.dispose() }
        runCatching { audioSource?.dispose() }
        videoCapturer = null; surfaceHelper = null; localAudioTrack = null
        videoSource = null; audioSource = null
        _localVideoTrack.value = null
        _peers.value = emptyMap()
        handledSdp.clear(); iceCounts.clear(); pendingIce.clear()
        muted.value = false; videoEnabled.value = false
        _activeChatId.value = null; title.value = ""; startedAt.value = 0L
        CallService.stop(context)
    }

    // ── Engine ─────────────────────────────────────────────────────────────────

    private fun ensureFactory() {
        if (factory != null) return
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions(),
        )
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private fun createLocalMedia(video: Boolean) {
        val f = factory ?: return
        val src = f.createAudioSource(MediaConstraints())
        audioSource = src
        localAudioTrack = f.createAudioTrack("audio0", src).apply { setEnabled(true) }
        if (video) createVideo()
    }

    private fun createVideo() {
        val f = factory ?: return
        val enumerator = Camera2Enumerator(context)
        val device = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
            ?: enumerator.deviceNames.firstOrNull() ?: return
        val capturer = enumerator.createCapturer(device, null) ?: return
        val helper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        val source = f.createVideoSource(false)
        capturer.initialize(helper, context, source.capturerObserver)
        runCatching { capturer.startCapture(1280, 720, 30) }
        val track = f.createVideoTrack("video0", source).apply { setEnabled(true) }
        videoCapturer = capturer; surfaceHelper = helper; videoSource = source
        _localVideoTrack.value = track
        // Hot-add the new track to existing connections.
        connections.values.forEach { runCatching { it.addTrack(track, listOf("stream0")) } }
    }

    private suspend fun onSession(s: CallSession) {
        val chatId = _activeChatId.value ?: return
        val remotes = s.participants.keys.filter { it != myUid }

        // New participants → create a peer; lower uid offers.
        for (r in remotes) {
            if (r !in connections) {
                createPeer(chatId, r)
                if (myUid < r) makeOffer(chatId, r)
            }
        }
        // Departed participants → tear down.
        (connections.keys - remotes.toSet()).forEach { closePeer(it) }

        // Reflect mute/video flags in UI.
        _peers.value = remotes.associateWith { r ->
            val p = s.participants[r]
            val existing = _peers.value[r]
            CallPeer(r, p?.muted ?: false, p?.video ?: false, existing?.videoTrack)
        }

        // SDP addressed to me.
        for ((key, entry) in s.sdp) {
            val parts = key.split("__"); if (parts.size != 2) continue
            val (from, to) = parts
            if (to != myUid || key in handledSdp) continue
            val pc = connections[from] ?: continue
            handledSdp += key
            when (entry.type) {
                "offer" -> {
                    setRemote(pc, SessionDescription(SessionDescription.Type.OFFER, entry.sdp))
                    flushIce(from, pc)
                    val answer = createAnswer(pc)
                    setLocal(pc, answer)
                    calls.setSdp(chatId, myUid, from, "answer", answer.description)
                }
                "answer" -> {
                    setRemote(pc, SessionDescription(SessionDescription.Type.ANSWER, entry.sdp))
                    flushIce(from, pc)
                }
            }
        }

        // ICE addressed to me.
        for ((key, list) in s.ice) {
            val parts = key.split("__"); if (parts.size != 2) continue
            val (from, to) = parts
            if (to != myUid) continue
            val pc = connections[from] ?: continue
            val seen = iceCounts[key] ?: 0
            if (list.size > seen) {
                list.drop(seen).forEach { c ->
                    val cand = IceCandidate(c.sdpMid, c.sdpMLineIndex, c.candidate)
                    if (pc.remoteDescription != null) pc.addIceCandidate(cand)
                    else pendingIce.getOrPut(from) { mutableListOf() }.add(c)
                }
                iceCounts[key] = list.size
            }
        }
    }

    private fun flushIce(from: String, pc: PeerConnection) {
        pendingIce.remove(from)?.forEach { c -> pc.addIceCandidate(IceCandidate(c.sdpMid, c.sdpMLineIndex, c.candidate)) }
    }

    private fun createPeer(chatId: String, remote: String) {
        val f = factory ?: return
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        val pc = f.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onIceCandidate(c: IceCandidate) {
                scope.launch { runCatching { calls.addIce(chatId, myUid, remote, IceEntry(c.sdpMid ?: "", c.sdpMLineIndex, c.sdp)) } }
            }
            override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                (receiver.track() as? VideoTrack)?.let { vt ->
                    _peers.value = _peers.value.toMutableMap().also {
                        it[remote] = (it[remote] ?: CallPeer(remote)).copy(videoTrack = vt)
                    }
                }
            }
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                if (state == PeerConnection.IceConnectionState.FAILED) scope.launch { runCatching { makeOffer(chatId, remote) } }
            }
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: org.webrtc.DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {}
        }) ?: return
        localAudioTrack?.let { pc.addTrack(it, listOf("stream0")) }
        _localVideoTrack.value?.let { pc.addTrack(it, listOf("stream0")) }
        connections[remote] = pc
    }

    private suspend fun makeOffer(chatId: String, remote: String) {
        val pc = connections[remote] ?: return
        val offer = createOffer(pc)
        setLocal(pc, offer)
        calls.setSdp(chatId, myUid, remote, "offer", offer.description)
    }

    private fun closePeer(remote: String) {
        connections.remove(remote)?.let { runCatching { it.close() } }
        _peers.value = _peers.value - remote
    }

    // ── SDP coroutine bridges ───────────────────────────────────────────────────

    private val mediaConstraints get() = MediaConstraints()

    private suspend fun createOffer(pc: PeerConnection): SessionDescription =
        suspendCancellableCoroutine { cont ->
            pc.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription) { cont.resume(desc) }
            }, mediaConstraints)
        }

    private suspend fun createAnswer(pc: PeerConnection): SessionDescription =
        suspendCancellableCoroutine { cont ->
            pc.createAnswer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription) { cont.resume(desc) }
            }, mediaConstraints)
        }

    private suspend fun setLocal(pc: PeerConnection, desc: SessionDescription) =
        suspendCancellableCoroutine { cont ->
            pc.setLocalDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() { if (cont.isActive) cont.resume(Unit) }
                override fun onSetFailure(p0: String?) { if (cont.isActive) cont.resume(Unit) }
            }, desc)
        }

    private suspend fun setRemote(pc: PeerConnection, desc: SessionDescription) =
        suspendCancellableCoroutine { cont ->
            pc.setRemoteDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() { if (cont.isActive) cont.resume(Unit) }
                override fun onSetFailure(p0: String?) { if (cont.isActive) cont.resume(Unit) }
            }, desc)
        }
}

private open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(p0: String?) {}
    override fun onSetFailure(p0: String?) {}
}
