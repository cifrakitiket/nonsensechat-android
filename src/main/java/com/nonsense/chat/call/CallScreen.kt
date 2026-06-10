package com.nonsense.chat.call

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.theme.OnlineGreen
import kotlinx.coroutines.delay
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun CallScreen(
    onClose: () -> Unit,
    viewModel: CallViewModel = hiltViewModel(),
) {
    val activeChatId by viewModel.manager.activeChatId.collectAsState()
    val peers by viewModel.manager.peers.collectAsState()
    val localTrack by viewModel.manager.localVideoTrack.collectAsState()
    val muted by viewModel.manager.muted.collectAsState()
    val videoOn by viewModel.manager.videoEnabled.collectAsState()
    val speaker by viewModel.manager.speakerOn.collectAsState()
    val title by viewModel.manager.title.collectAsState()
    val startedAt by viewModel.manager.startedAt.collectAsState()
    val names by viewModel.names.collectAsState()

    // End the screen when the call ends.
    LaunchedEffect(activeChatId) { if (activeChatId == null) onClose() }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val elapsed = if (startedAt > 0) (now - startedAt) / 1000 else 0L

    val tiles = buildList {
        add(Tile(viewModel.myUid, localTrack, !muted, videoOn, isSelf = true))
        peers.values.forEach { p -> add(Tile(p.uid, p.videoTrack, !p.muted, p.video, isSelf = false)) }
    }
    val columns = if (tiles.size <= 1) 1 else 2

    Box(Modifier.fillMaxSize().background(Color(0xFF0A0E14))) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title.ifBlank { "Звонок" }, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(formatDuration(elapsed), color = Color(0xFFB0BEC5), style = MaterialTheme.typography.bodyMedium)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.weight(1f).fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(tiles, key = { it.uid }) { tile ->
                    CallTile(
                        tile = tile,
                        name = if (tile.isSelf) "Вы" else (names[tile.uid]?.displayName ?: "…"),
                        avatar = if (tile.isSelf) null else names[tile.uid]?.avatar,
                        eglContext = viewModel.eglBase.eglBaseContext,
                    )
                }
            }

            ControlsBar(
                muted = muted, videoOn = videoOn, speakerOn = speaker,
                onMute = viewModel::toggleMute,
                onVideo = viewModel::toggleVideo,
                onSwitch = viewModel::switchCamera,
                onSpeaker = viewModel::toggleSpeaker,
                onHangUp = { viewModel.hangUp() },
            )
        }
    }
}

private data class Tile(val uid: String, val track: VideoTrack?, val audioOn: Boolean, val videoOn: Boolean, val isSelf: Boolean)

@Composable
private fun CallTile(tile: Tile, name: String, avatar: String?, eglContext: org.webrtc.EglBase.Context) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(14.dp)).background(Color(0xFF161D27)),
        contentAlignment = Alignment.Center,
    ) {
        if (tile.videoOn && tile.track != null) {
            VideoRenderer(tile.track, eglContext, mirror = tile.isSelf, modifier = Modifier.fillMaxSize())
        } else {
            Avatar(name, avatar, size = 84.dp)
        }
        Row(
            Modifier.align(Alignment.BottomStart).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!tile.audioOn) {
                Icon(Icons.Default.MicOff, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
            }
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0x88000000)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(name, color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
private fun VideoRenderer(track: VideoTrack, eglContext: org.webrtc.EglBase.Context, mirror: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val renderer = remember {
        SurfaceViewRenderer(context).apply {
            init(eglContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            setEnableHardwareScaler(true)
            setMirror(mirror)
        }
    }
    DisposableEffect(track) {
        runCatching { track.addSink(renderer) }
        onDispose { runCatching { track.removeSink(renderer) } }
    }
    DisposableEffect(Unit) { onDispose { runCatching { renderer.release() } } }
    AndroidView(factory = { renderer }, modifier = modifier)
}

@Composable
private fun ControlsBar(
    muted: Boolean, videoOn: Boolean, speakerOn: Boolean,
    onMute: () -> Unit, onVideo: () -> Unit, onSwitch: () -> Unit, onSpeaker: () -> Unit, onHangUp: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CtlButton(if (muted) Icons.Default.MicOff else Icons.Default.Mic, "Микрофон", if (muted) Color(0xFF37414D) else Color(0xFF263039), onMute)
        CtlButton(if (videoOn) Icons.Default.Videocam else Icons.Default.VideocamOff, "Камера", Color(0xFF263039), onVideo)
        CtlButton(Icons.Default.Cameraswitch, "Сменить камеру", Color(0xFF263039), onSwitch)
        CtlButton(if (speakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, "Динамик", Color(0xFF263039), onSpeaker)
        CtlButton(Icons.Default.CallEnd, "Завершить", Color(0xFFE5484D), onHangUp)
    }
}

@Composable
private fun CtlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, bg: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(58.dp).clip(CircleShape).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = Color.White, modifier = Modifier.size(26.dp)) }
}

private fun formatDuration(sec: Long): String {
    val h = sec / 3600; val m = (sec % 3600) / 60; val s = sec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
