package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.MediaItemEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.WatchProgressEntity
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZorvenBorderSubtle
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenTextMuted
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary
import com.example.ui.theme.parseHexColor

fun resolveLocalBackdropResId(resName: String?): Int? {
    return when (resName) {
        "img_hero_solaris_protocol_1793905910619" -> R.drawable.img_hero_solaris_protocol_1793905910619
        "img_hero_velvet_syndicate_1793905911834" -> R.drawable.img_hero_velvet_syndicate_1793905911834
        "img_hero_crimson_dynasty_1793905912431" -> R.drawable.img_hero_crimson_dynasty_1793905912431
        else -> null
    }
}

@Composable
fun CinematicMediaImage(
    imageUrl: String,
    localResName: String? = null,
    ambientHex: String = "#7A1624",
    secondaryHex: String = "#141824",
    titleForFallback: String = "",
    showTitleOverlayOnPoster: Boolean = false,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(ambientHex) { parseHexColor(ambientHex, Color(0xFF3A1820)) }
    val secondaryColor = remember(secondaryHex) { parseHexColor(secondaryHex, Color(0xFF141824)) }
    val localResId = remember(localResName) { resolveLocalBackdropResId(localResName) }

    Box(modifier = modifier.background(ZorvenSurface)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.75f),
                        secondaryColor.copy(alpha = 0.90f),
                        ZorvenObsidian
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }

        if (localResId != null) {
            Image(
                painter = painterResource(id = localResId),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(250)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showTitleOverlayOnPoster && titleForFallback.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.58f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.86f)
                            )
                        )
                    )
            )
            Text(
                text = titleForFallback,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.7.sp
                ),
                color = ZorvenTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            )
        }
    }
}

/**
 * Clean 2:3 Portrait Movie / Series Poster Card for standard discovery rows.
 * Free of fake Top 10 badges or repetitive corner icons.
 */
@Composable
fun PortraitPosterCard(
    media: MediaItemEntity,
    width: Dp = 118.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "poster_press_scale"
    )

    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(2f / 3f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .pointerInput(media.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .testTag("poster_card_${media.id}")
    ) {
        CinematicMediaImage(
            imageUrl = media.posterUrl,
            localResName = media.localBackdropResName,
            ambientHex = media.ambientColorHex,
            secondaryHex = media.secondaryAmbientHex,
            titleForFallback = media.title,
            showTitleOverlayOnPoster = true,
            contentDescription = media.title,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Redesigned Top 10 Ranked Card:
 * - Coordinated rank number + 2:3 poster layout.
 * - Positions 1, 2, 3 feature a high-contrast pure white-to-silver gradient;
 *   positions 4..10 use a refined light-gray-to-steel vertical gradient.
 * - Positioned cleanly alongside the poster without clipping or covering key artwork.
 */
@Composable
fun Top10PosterCard(
    rank: Int,
    media: MediaItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTopThree = rank in 1..3
    val numberFillBrush = remember(rank) {
        if (isTopThree) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFE4E4E7),
                    Color(0xFF9CA3AF)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF4F4F5),
                    Color(0xFFA1A1AA),
                    Color(0xFF52525B)
                )
            )
        }
    }

    val numberStrokeBrush = remember(rank) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isTopThree) 0.95f else 0.70f),
                Color(0xFF71717A).copy(alpha = 0.35f)
            )
        )
    }

    val cardWidth = if (rank == 10) 192.dp else 174.dp
    val numberColumnWidth = if (rank == 10) 76.dp else 58.dp
    val fontSizeSp = if (rank == 10) 92.sp else 112.sp

    Row(
        modifier = modifier
            .width(cardWidth)
            .height(174.dp)
            .clickable(onClick = onClick)
            .testTag("top10_card_${media.id}"),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(numberColumnWidth)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomEnd
        ) {
            val rankText = rank.toString()
            // Subtle outer edge stroke for crisp legibility
            Text(
                text = rankText,
                style = TextStyle(
                    fontFamily = BebasNeueFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizeSp,
                    lineHeight = fontSizeSp,
                    brush = numberStrokeBrush,
                    drawStyle = Stroke(
                        width = 3.5f,
                        join = StrokeJoin.Round
                    )
                ),
                modifier = Modifier.offset(x = 8.dp, y = 6.dp)
            )
            // White-to-light-gray gradient fill
            Text(
                text = rankText,
                style = TextStyle(
                    fontFamily = BebasNeueFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSizeSp,
                    lineHeight = fontSizeSp,
                    brush = numberFillBrush
                ),
                modifier = Modifier.offset(x = 8.dp, y = 6.dp)
            )
        }

        PortraitPosterCard(
            media = media,
            width = 116.dp,
            onClick = onClick
        )
    }
}

/**
 * 16:9 Landscape Continue Watching Card connected strictly to genuine user playback history.
 * Displays landscape artwork, accurate playback progress bar, full movie/series title,
 * remaining duration / episode info, Resume playback action, and Info / Remove controls.
 */
@Composable
fun ContinueWatchingCard(
    media: MediaItemEntity,
    progress: WatchProgressEntity,
    onPlayClick: () -> Unit,
    onInfoClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(248.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .testTag("continue_watching_${media.id}"),
        color = ZorvenSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // 16:9 Landscape Thumbnail Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable(onClick = onPlayClick)
            ) {
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

                // Subtle bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.68f)
                                )
                            )
                        )
                )

                // Centered Play Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Resume ${media.title}",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Remaining time pill in bottom-right above progress bar
                Surface(
                    color = Color.Black.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = progress.remainingMinutesText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ZorvenTextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Accurate playback progress bar
                LinearProgressIndicator(
                    progress = { progress.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .align(Alignment.BottomCenter),
                    color = ZorvenCrimson,
                    trackColor = Color.White.copy(alpha = 0.24f)
                )
            }

            // Bottom Metadata & Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZorvenSurfaceElevated)
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = ZorvenTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = progress.episodeLabel ?: "${media.releaseYear} • ${media.qualityBadge}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZorvenTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Details for ${media.title}",
                            tint = ZorvenTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove ${media.title} from Continue Watching",
                            tint = ZorvenTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Special 16:9 Landscape Featured Collection / Widescreen Preview Card.
 */
@Composable
fun LandscapePreviewCard(
    media: MediaItemEntity,
    width: Dp = 264.dp,
    onPlayClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(width)
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .clickable(onClick = onDetailsClick)
            .testTag("landscape_card_${media.id}"),
        color = ZorvenSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.50f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.86f)
                                )
                            )
                        )
                )

                // Top-right format badge
                Surface(
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = media.qualityBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Bottom title & play button inside widescreen frame
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = ZorvenTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${media.genres.take(2).joinToString(" • ")} • ${media.durationOrSeasons}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onPlayClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play ${media.title}",
                            tint = ZorvenObsidian,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Refined Architectural Profile Avatar Badge with clean dark-and-silver/crimson styling.
 */
@Composable
fun ProfileAvatarBadge(
    profile: ProfileEntity,
    size: Dp = 28.dp,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = remember(profile.avatarColorHex) { parseHexColor(profile.avatarColorHex, Color(0xFF18181F)) }
    val accentColor = remember(profile.avatarAccentHex) { parseHexColor(profile.avatarAccentHex, ZorvenCrimson) }
    val cornerRadius = (size.value * 0.22f).dp

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        bgColor,
                        Color(0xFF0E0E12)
                    )
                )
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle top accent bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height((size.value * 0.08f).coerceAtLeast(2f).dp)
                .background(accentColor)
        )

        Text(
            text = if (profile.isKids) "KIDS" else profile.avatarInitials,
            style = TextStyle(
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (profile.isKids) (size.value * 0.28f).sp else (size.value * 0.38f).sp,
                letterSpacing = 0.8.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun ShimmerCatalogPlaceholder(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer_loader")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            ZorvenSurface,
            ZorvenSurfaceElevated,
            ZorvenSurface
        ),
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 250f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(shimmerBrush)
        )
        repeat(2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(114.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(shimmerBrush)
                        )
                    }
                }
            }
        }
    }
}
