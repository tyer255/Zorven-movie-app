package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.ClipItemEntity
import com.example.data.local.MediaItemEntity
import com.example.data.local.MyListEntity
import com.example.ui.components.CinematicMediaImage
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary

@Composable
fun ClipsScreen(
    clips: List<ClipItemEntity>,
    allMedia: List<MediaItemEntity>,
    myListEntries: List<MyListEntity>,
    onToggleMyList: (MediaItemEntity) -> Unit,
    onWatchFullTitle: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (clips.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ZorvenObsidian),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ZorvenCrimson)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { clips.size })
    var isMuted by remember { mutableStateOf(false) }
    val myListIds = remember(myListEntries) { myListEntries.map { it.mediaId }.toSet() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .testTag("clips_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 0
        ) { page ->
            val clip = clips[page]
            val isPageActive = pagerState.currentPage == page
            val linkedMedia = allMedia.find { it.id == clip.mediaId }
            val isInMyList = myListIds.contains(clip.mediaId)

            VerticalClipPage(
                clip = clip,
                linkedMedia = linkedMedia,
                isActive = isPageActive,
                isMuted = isMuted,
                isInMyList = isInMyList,
                onToggleMute = { isMuted = !isMuted },
                onToggleMyList = {
                    if (linkedMedia != null) onToggleMyList(linkedMedia)
                },
                onWatchFullTitle = { onWatchFullTitle(clip.mediaId) },
                onOpenDetails = { onOpenDetails(clip.mediaId) }
            )
        }

        // Top Floating Header ("Moments & Clips" + Sound Mute/Unmute Control)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ZorvenCrimson, CircleShape)
                    )
                    Text(
                        text = "CLIPS & MOMENTS • ${pagerState.currentPage + 1}/${clips.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = ZorvenTextPrimary
                    )
                }
            }

            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .testTag("clips_mute_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute clip" else "Mute clip",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun VerticalClipPage(
    clip: ClipItemEntity,
    linkedMedia: MediaItemEntity?,
    isActive: Boolean,
    isMuted: Boolean,
    isInMyList: Boolean,
    onToggleMute: () -> Unit,
    onToggleMyList: () -> Unit,
    onWatchFullTitle: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val context = LocalContext.current
    var isVideoPrepared by remember(clip.id) { mutableStateOf(false) }
    var isPausedByUser by remember(clip.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isPausedByUser = !isPausedByUser }
    ) {
        // Guaranteed atmospheric cinema poster background underneath the active video surface
        CinematicMediaImage(
            imageUrl = clip.posterUrl,
            localResName = linkedMedia?.localBackdropResName,
            ambientHex = clip.ambientColorHex,
            secondaryHex = linkedMedia?.secondaryAmbientHex ?: "#10141E",
            titleForFallback = clip.movieTitle,
            showTitleOverlayOnPoster = false,
            contentDescription = clip.clipTitle,
            modifier = Modifier.fillMaxSize()
        )

        // Only mount & play the VideoView when this vertical page is the active visible page
        if (isActive) {
            var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setVideoURI(Uri.parse(clip.videoUrl))
                        setOnPreparedListener { mp ->
                            isVideoPrepared = true
                            mp.isLooping = true
                            val vol = if (isMuted) 0f else 1f
                            mp.setVolume(vol, vol)
                            if (!isPausedByUser) {
                                start()
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            isVideoPrepared = false
                            true
                        }
                    }
                },
                update = { view ->
                    if (isPausedByUser && view.isPlaying) {
                        view.pause()
                    } else if (!isPausedByUser && isVideoPrepared && !view.isPlaying) {
                        view.start()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            DisposableEffect(clip.id) {
                onDispose {
                    videoViewRef?.stopPlayback()
                }
            }
        }

        // Bottom-to-Black Scrim for Crisp Readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.35f),
                            0.45f to Color.Transparent,
                            0.75f to Color.Black.copy(alpha = 0.72f),
                            1.0f to Color(0xFF0A0A0C).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // Pause indicator in center when user taps to pause
        if (isPausedByUser) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Resume clip",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Content & Right-Rail Actions
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, bottom = 20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Metadata Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Surface(
                    color = ZorvenCrimson,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = clip.tagsCsv,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = clip.movieTitle,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = BebasNeueFontFamily,
                        fontSize = 32.sp,
                        lineHeight = 34.sp
                    ),
                    color = ZorvenTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = clip.clipTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = clip.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = ZorvenTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onWatchFullTitle,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ZorvenObsidian
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("clip_watch_full_${clip.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Full Title",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Right Vertical Action Rail (My List, Info, Share, Sound)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ClipRailButton(
                    icon = if (isInMyList) Icons.Filled.Check else Icons.Filled.Add,
                    label = "My List",
                    tint = if (isInMyList) ZorvenCrimson else Color.White,
                    onClick = onToggleMyList
                )

                ClipRailButton(
                    icon = Icons.Outlined.Info,
                    label = "Details",
                    tint = Color.White,
                    onClick = onOpenDetails
                )

                ClipRailButton(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    tint = Color.White,
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out \"${clip.clipTitle}\" from ${clip.movieTitle} on Zorvén!"
                            )
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Clip"))
                    }
                )

                ClipRailButton(
                    icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    label = if (isMuted) "Muted" else "Sound",
                    tint = Color.White,
                    onClick = onToggleMute
                )
            }
        }
    }
}

@Composable
private fun ClipRailButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.52f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ZorvenTextPrimary
        )
    }
}
