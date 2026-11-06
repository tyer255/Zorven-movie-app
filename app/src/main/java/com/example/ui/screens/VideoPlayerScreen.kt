package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.EpisodeEntity
import com.example.data.remote.StreamServerNode
import com.example.ui.PlayerSessionUiState
import com.example.ui.components.CinematicMediaImage
import com.example.ui.theme.ZorvenBorderSubtle
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenErrorRed
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSuccessGreen
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenTextMuted
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    session: PlayerSessionUiState,
    initialPositionMs: Long,
    onBack: (Long, Long) -> Unit,
    onSelectServer: (StreamServerNode) -> Unit,
    onSelectQuality: (String) -> Unit,
    onSelectAudioAndSubtitles: (String, String) -> Unit,
    onSwitchEpisode: (EpisodeEntity) -> Unit,
    onToggleErrorSimulation: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val media = session.mediaItem
    val currentEpisode = session.currentEpisode
    val activeServer = session.selectedServer

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember(activeServer?.serverId, currentEpisode?.id) { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var isLandscapeMode by remember { mutableStateOf(false) }
    var interactionTick by remember { mutableIntStateOf(0) }

    var currentPositionMs by remember(media?.id, currentEpisode?.id) {
        mutableLongStateOf(initialPositionMs.coerceAtLeast(0L))
    }
    var totalDurationMs by remember(media?.id, currentEpisode?.id) {
        mutableLongStateOf(
            if (currentEpisode != null) currentEpisode.durationMinutes * 60_000L
            else 734_000L
        )
    }

    var showServerPickerSheet by remember { mutableStateOf(false) }
    var showAudioSubtitleSheet by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    fun bumpInteraction() {
        controlsVisible = true
        interactionTick++
    }

    // Restore portrait orientation when leaving the player
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler {
        onBack(currentPositionMs, totalDurationMs)
    }

    // Auto-hide controls after 4 seconds of inactivity while playing
    LaunchedEffect(controlsVisible, isPlaying, interactionTick, showServerPickerSheet, showAudioSubtitleSheet, showQualitySheet) {
        if (controlsVisible && isPlaying && !showServerPickerSheet && !showAudioSubtitleSheet && !showQualitySheet) {
            delay(4200L)
            controlsVisible = false
        }
    }

    // Poll playback position from VideoView (or advance smoothly if stream is still buffering)
    LaunchedEffect(isPlaying, isBuffering, session.hasPlaybackError) {
        while (true) {
            delay(500L)
            if (isPlaying && !session.hasPlaybackError) {
                val view = videoViewRef
                if (view != null && view.isPlaying) {
                    val pos = view.currentPosition.toLong()
                    val dur = view.duration.toLong()
                    if (dur > 0L) totalDurationMs = dur
                    currentPositionMs = pos
                    isBuffering = false
                } else if (!isBuffering) {
                    currentPositionMs = (currentPositionMs + 500L).coerceAtMost(totalDurationMs)
                }
            }
        }
    }

    // Fallback timeout so buffering spinner never hangs indefinitely in restricted container networks
    LaunchedEffect(activeServer?.serverId, currentEpisode?.id) {
        delay(2200L)
        isBuffering = false
    }

    val nextEpisode = remember(session.allEpisodesForSeries, currentEpisode) {
        if (currentEpisode == null) null
        else {
            val idx = session.allEpisodesForSeries.indexOfFirst { it.id == currentEpisode.id }
            if (idx >= 0 && idx + 1 < session.allEpisodesForSeries.size) {
                session.allEpisodesForSeries[idx + 1]
            } else null
        }
    }

    val subtitleLines = remember(media?.title) {
        listOf(
            "\"Telemetry confirmed on Custom Server Node. Signal integrity at 99.4%.\"",
            "\"Hold course through the outer ring—don't touch the thruster array.\"",
            "\"Every streetlamp on the boulevard has eyes tonight.\"",
            "\"The vault key was never in Zurich. It was inside the ledger all along.\""
        )
    }
    val currentSubtitleLine = remember(currentPositionMs / 6000L) {
        subtitleLines[((currentPositionMs / 6000L).toInt()).mod(subtitleLines.size)]
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { bumpInteraction() }
            )
            .testTag("video_player_screen")
    ) {
        // 1. Background Cinema Still + Real VideoView Surface
        if (media != null) {
            CinematicMediaImage(
                imageUrl = media.backdropUrl,
                localResName = media.localBackdropResName,
                ambientHex = media.ambientColorHex,
                secondaryHex = media.secondaryAmbientHex,
                titleForFallback = media.title,
                showTitleOverlayOnPoster = false,
                contentDescription = media.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        val streamUrl = activeServer?.streamUrl
        if (!streamUrl.isNullOrBlank() && !session.hasPlaybackError) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setVideoURI(Uri.parse(streamUrl))
                        setOnPreparedListener { mp ->
                            isBuffering = false
                            val dur = mp.duration.toLong()
                            if (dur > 0L) totalDurationMs = dur
                            if (initialPositionMs > 0L && initialPositionMs < dur) {
                                seekTo(initialPositionMs.toInt())
                            }
                            if (isPlaying) start()
                        }
                        setOnErrorListener { _, _, _ ->
                            isBuffering = false
                            true
                        }
                    }
                },
                update = { view ->
                    if (isPlaying && !view.isPlaying && !isBuffering) {
                        view.start()
                    } else if (!isPlaying && view.isPlaying) {
                        view.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            DisposableEffect(streamUrl) {
                onDispose {
                    videoViewRef?.stopPlayback()
                }
            }
        }

        // 2. Live Closed Captions / Subtitles Overlay
        if (session.selectedSubtitleTrack != "Off" && !session.hasPlaybackError) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (controlsVisible) 138.dp else 36.dp, start = 24.dp, end = 24.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = currentSubtitleLine,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 3. Playback Error State Overlay
        if (session.hasPlaybackError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = ZorvenErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Stream Server Connection Interrupted",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZorvenTextPrimary
                    )
                    Text(
                        text = "Unable to stream from ${activeServer?.serverName ?: "Primary Server"}. Retry or switch to another streaming server node.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onToggleErrorSimulation(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = ZorvenCrimson)
                        ) {
                            Text("Retry Stream")
                        }
                        Button(
                            onClick = { showServerPickerSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ZorvenSurfaceElevated)
                        ) {
                            Text("Switch Server")
                        }
                    }
                }
            }
        }

        // 4. Auto-Hiding Full-Screen Player HUD Overlay
        AnimatedVisibility(
            visible = controlsVisible && !session.hasPlaybackError,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.78f),
                                0.35f to Color.Black.copy(alpha = 0.25f),
                                0.65f to Color.Black.copy(alpha = 0.25f),
                                1.0f to Color.Black.copy(alpha = 0.86f)
                            )
                        )
                    )
            ) {
                if (isScreenLocked) {
                    // Screen Locked Mode: Only show Unlock button at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clickable {
                                    isScreenLocked = false
                                    bumpInteraction()
                                }
                                .testTag("player_unlock_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LockOpen,
                                    contentDescription = "Unlock Controls",
                                    tint = ZorvenObsidian,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Screen Locked • Tap to Unlock",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenObsidian
                                )
                            }
                        }
                    }
                } else {
                    // Top Bar: Back, Title & Episode, Server Pill, Orientation Toggle
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { onBack(currentPositionMs, totalDurationMs) },
                                modifier = Modifier.testTag("player_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Player",
                                    tint = Color.White
                                )
                            }
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = media?.title ?: "Now Playing",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val subtitleMeta = if (currentEpisode != null) {
                                    "S${currentEpisode.seasonNumber}:E${currentEpisode.episodeNumber} \"${currentEpisode.title}\" • ${session.selectedQuality}"
                                } else {
                                    "${media?.releaseYear ?: 2026} • ${session.selectedQuality}"
                                }
                                Text(
                                    text = subtitleMeta,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZorvenTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Active Custom Streaming Server Pill
                            Surface(
                                color = ZorvenSurfaceElevated.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .border(1.dp, ZorvenSuccessGreen.copy(alpha = 0.6f), RoundedCornerShape(50))
                                    .clickable {
                                        bumpInteraction()
                                        showServerPickerSheet = true
                                    }
                                    .testTag("player_server_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(ZorvenSuccessGreen, CircleShape)
                                    )
                                    Text(
                                        text = activeServer?.serverName?.substringBefore("•")?.trim() ?: "Server 1",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            // Landscape / Portrait Orientation Toggle
                            IconButton(
                                onClick = {
                                    bumpInteraction()
                                    isLandscapeMode = !isLandscapeMode
                                    activity?.requestedOrientation = if (isLandscapeMode) {
                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    } else {
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    }
                                },
                                modifier = Modifier.testTag("player_orientation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ScreenRotation,
                                    contentDescription = "Toggle Landscape Orientation",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Center Transport Controls: Rewind 10s, Play/Pause (or Buffering), Forward 10s
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(42.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                bumpInteraction()
                                val target = (currentPositionMs - 10_000L).coerceAtLeast(0L)
                                currentPositionMs = target
                                videoViewRef?.seekTo(target.toInt())
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .testTag("player_rewind_10_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay10,
                                contentDescription = "Rewind 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Box(
                            modifier = Modifier.size(74.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBuffering || session.isLoadingServers) {
                                CircularProgressIndicator(
                                    color = ZorvenCrimson,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    bumpInteraction()
                                    isPlaying = !isPlaying
                                },
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .testTag("player_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                bumpInteraction()
                                val target = (currentPositionMs + 10_000L).coerceAtMost(totalDurationMs)
                                currentPositionMs = target
                                videoViewRef?.seekTo(target.toInt())
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .testTag("player_forward_10_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Forward10,
                                contentDescription = "Forward 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Bottom Scrubber & Action Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Timecode & Scrubber Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = formatMillisToClock(currentPositionMs),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Slider(
                                value = if (totalDurationMs > 0L) {
                                    (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                                } else 0f,
                                onValueChange = { frac ->
                                    bumpInteraction()
                                    val newPos = (frac * totalDurationMs).toLong()
                                    currentPositionMs = newPos
                                    videoViewRef?.seekTo(newPos.toInt())
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("player_seek_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = ZorvenCrimson,
                                    activeTrackColor = ZorvenCrimson,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )

                            Text(
                                text = formatMillisToClock(totalDurationMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = ZorvenTextSecondary
                            )
                        }

                        // Bottom Action Strip: Servers, Quality, Audio & Subtitles, Lock, Next Episode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerControlStripButton(
                                icon = Icons.Filled.Dns,
                                label = "Servers (${session.servers.size})",
                                testTag = "player_servers_button",
                                onClick = {
                                    bumpInteraction()
                                    showServerPickerSheet = true
                                }
                            )

                            PlayerControlStripButton(
                                icon = Icons.Filled.HighQuality,
                                label = "Quality",
                                testTag = "player_quality_button",
                                onClick = {
                                    bumpInteraction()
                                    showQualitySheet = true
                                }
                            )

                            PlayerControlStripButton(
                                icon = Icons.Filled.Subtitles,
                                label = "Audio & Subtitles",
                                testTag = "player_subtitles_button",
                                onClick = {
                                    bumpInteraction()
                                    showAudioSubtitleSheet = true
                                }
                            )

                            PlayerControlStripButton(
                                icon = Icons.Filled.Lock,
                                label = "Lock",
                                testTag = "player_lock_button",
                                onClick = {
                                    isScreenLocked = true
                                    controlsVisible = true
                                }
                            )

                            if (nextEpisode != null) {
                                PlayerControlStripButton(
                                    icon = Icons.Filled.SkipNext,
                                    label = "Next Ep",
                                    testTag = "player_next_episode_button",
                                    onClick = {
                                        bumpInteraction()
                                        onSwitchEpisode(nextEpisode)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Streaming Server / Mirror Switcher Bottom Sheet
        if (showServerPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showServerPickerSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = ZorvenSurfaceElevated,
                contentColor = ZorvenTextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Streaming Server Node",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showServerPickerSheet = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZorvenTextSecondary)
                        }
                    }

                    session.servers.forEach { server ->
                        val isSelected = server.serverId == activeServer?.serverId
                        Surface(
                            color = if (isSelected) ZorvenCrimson.copy(alpha = 0.2f) else ZorvenSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) ZorvenCrimson else ZorvenBorderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectServer(server)
                                    showServerPickerSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = server.serverName,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = ZorvenTextPrimary
                                    )
                                    Text(
                                        text = "${server.region} • ${server.protocol} • ${server.latencyMs} ms",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZorvenSuccessGreen
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = ZorvenCrimson)
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            showServerPickerSheet = false
                            onToggleErrorSimulation(true)
                        }
                    ) {
                        Text(
                            text = "Simulate Stream Interruption Error State",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Video Quality Selector Sheet
        if (showQualitySheet) {
            val qualities = activeServer?.qualityOptions ?: listOf("Auto (4K / 1080p)", "1080p Full HD", "720p HD", "480p Data Saver")
            ModalBottomSheet(
                onDismissRequest = { showQualitySheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = ZorvenSurfaceElevated,
                contentColor = ZorvenTextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Playback Video Quality",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    qualities.forEach { q ->
                        val selected = q == session.selectedQuality
                        Surface(
                            color = if (selected) ZorvenCrimson.copy(alpha = 0.2f) else ZorvenSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectQuality(q)
                                    showQualitySheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(q, style = MaterialTheme.typography.bodyMedium, color = ZorvenTextPrimary)
                                if (selected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = ZorvenCrimson)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Audio & Subtitles Selector Sheet
        if (showAudioSubtitleSheet) {
            val audioList = activeServer?.audioTracks ?: listOf("English (Dolby Atmos)", "French (5.1)", "Spanish (5.1)")
            val subtitleList = activeServer?.subtitleTracks ?: listOf("Off", "English [CC]", "Spanish", "French")

            ModalBottomSheet(
                onDismissRequest = { showAudioSubtitleSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = ZorvenSurfaceElevated,
                contentColor = ZorvenTextPrimary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Audio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        audioList.forEach { aud ->
                            val selected = aud == session.selectedAudioTrack
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) ZorvenCrimson.copy(alpha = 0.22f) else Color.Transparent)
                                    .clickable {
                                        onSelectAudioAndSubtitles(aud, session.selectedSubtitleTrack)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = aud,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZorvenTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = ZorvenCrimson, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Subtitles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        subtitleList.forEach { sub ->
                            val selected = sub == session.selectedSubtitleTrack
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) ZorvenCrimson.copy(alpha = 0.22f) else Color.Transparent)
                                    .clickable {
                                        onSelectAudioAndSubtitles(session.selectedAudioTrack, sub)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZorvenTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = ZorvenCrimson, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PlayerControlStripButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

private fun formatMillisToClock(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSec / 60L
    val seconds = totalSec % 60L
    return String.format("%02d:%02d", minutes, seconds)
}
