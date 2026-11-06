package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.data.local.EpisodeEntity
import com.example.data.local.MediaItemEntity
import com.example.data.local.WatchProgressEntity
import com.example.ui.components.CinematicMediaImage
import com.example.ui.components.PortraitPosterCard
import com.example.ui.theme.ArchivoBlackFontFamily
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.ZorvenBorderSubtle
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSuccessGreen
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenSurfaceHigh
import com.example.ui.theme.ZorvenTextMuted
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary
import com.example.ui.theme.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    media: MediaItemEntity?,
    allMedia: List<MediaItemEntity>,
    episodes: List<EpisodeEntity>,
    selectedSeason: Int,
    isInMyList: Boolean,
    userRating: Int?,
    watchProgress: WatchProgressEntity?,
    downloads: List<DownloadEntity>,
    configuredApiUrl: String,
    onBack: () -> Unit,
    onPlay: (String, String?, Long?) -> Unit,
    onSelectSeason: (Int) -> Unit,
    onToggleMyList: (MediaItemEntity) -> Unit,
    onRate: (MediaItemEntity, Int) -> Unit,
    onDownload: (MediaItemEntity, EpisodeEntity?) -> Unit,
    onOpenOtherMediaDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    if (media == null) {
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

    val isSeries = media.type == "SERIES"
    var isSynopsisExpanded by remember(media.id) { mutableStateOf(false) }
    var activeTabIndex by remember(media.id) { mutableIntStateOf(0) }
    var showSeasonPickerSheet by remember { mutableStateOf(false) }
    var showRatingPicker by remember { mutableStateOf(false) }
    var showServerInfoSheet by remember { mutableStateOf(false) }

    val seasonNumbers = remember(media.seasonCount, episodes) {
        val maxSeason = maxOf(media.seasonCount, episodes.maxOfOrNull { it.seasonNumber } ?: 1)
        (1..maxSeason.coerceAtLeast(1)).toList()
    }
    val episodesForSelectedSeason = remember(episodes, selectedSeason) {
        episodes.filter { it.seasonNumber == selectedSeason }
    }

    val similarMedia = remember(media, allMedia) {
        val primaryGenres = media.genres.toSet()
        allMedia
            .filter { it.id != media.id }
            .sortedByDescending { candidate ->
                candidate.genres.count { it in primaryGenres } * 10 + (if (candidate.type == media.type) 3 else 0)
            }
            .take(9)
    }

    val mainDownload = remember(downloads, media.id) {
        downloads.find { it.mediaId == media.id && it.episodeId == null }
            ?: downloads.firstOrNull { it.mediaId == media.id }
    }

    val ambientColor = remember(media.ambientColorHex) {
        parseHexColor(media.ambientColorHex, Color(0xFF5A1822))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .testTag("details_screen_${media.id}")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // 1. 16:9 Hero Backdrop / Trailer Preview Area
            item(key = "details_hero_${media.id}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9.4f)
                            .clickable {
                                onPlay(media.id, watchProgress?.episodeId, watchProgress?.positionMs)
                            }
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

                        // Bottom-to-black & ambient gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Black.copy(alpha = 0.42f),
                                            0.45f to Color.Transparent,
                                            0.82f to ambientColor.copy(alpha = 0.36f),
                                            1.0f to ZorvenObsidian
                                        )
                                    )
                                )
                        )

                        // Center Play Preview Circle
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .align(Alignment.Center)
                                .background(Color.Black.copy(alpha = 0.62f), CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play ${media.title}",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Preview tag in bottom-right
                        Surface(
                            color = Color.Black.copy(alpha = 0.68f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Preview • ${media.qualityBadge}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZorvenTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Top Navigation Overlay (Back & Close)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("details_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { showServerInfoSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Dns,
                                    contentDescription = "Streaming servers",
                                    tint = ZorvenSuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "4 Servers Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = ZorvenTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Title, Metadata, Primary CTAs, Synopsis, Credits & Action Row
            item(key = "details_metadata_${media.id}") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ambientColor.copy(alpha = 0.22f),
                                    ZorvenObsidian
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Brand Series/Film Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.ui.components.ZorvenMonogramIcon(size = 18.dp)
                        Text(
                            text = if (isSeries) "S E R I E S" else "F I L M",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.5.sp,
                                color = ZorvenTextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = BebasNeueFontFamily,
                            fontSize = 34.sp,
                            lineHeight = 36.sp,
                            letterSpacing = 1.sp
                        ),
                        color = ZorvenTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata Badges Row (Match %, Year, Maturity, Duration/Seasons, Quality, Audio, CC)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${media.matchScore}% Match",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZorvenSuccessGreen
                        )
                        Text(
                            text = media.releaseYear.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = ZorvenTextSecondary
                        )
                        MetadataOutlineBadge(text = media.maturityRating)
                        Text(
                            text = media.durationOrSeasons,
                            style = MaterialTheme.typography.labelMedium,
                            color = ZorvenTextSecondary
                        )
                        MetadataOutlineBadge(text = media.qualityBadge)
                        Icon(
                            imageVector = Icons.Outlined.Subtitles,
                            contentDescription = "Subtitles available",
                            tint = ZorvenTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Audio & Subtitle language line
                    Text(
                        text = "${media.audioBadge} • Audio: English, French, Spanish, Japanese • Subtitles: [CC]",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextMuted
                    )

                    // Top 10 Rank Callout if present
                    if (media.top10Rank != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = ZorvenCrimson,
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text(
                                    text = "TOP 10",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "#${media.top10Rank} in ${if (isSeries) "TV Shows" else "Movies"} Today",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary
                            )
                        }
                    }

                    // Watch Progress Bar if partially watched
                    if (watchProgress != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = watchProgress.episodeLabel ?: "Continue Watching",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = ZorvenTextPrimary
                                )
                                Text(
                                    text = watchProgress.remainingMinutesText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZorvenTextSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { watchProgress.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ZorvenCrimson,
                                trackColor = ZorvenSurfaceHigh
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Play / Resume Button
                    Button(
                        onClick = {
                            onPlay(media.id, watchProgress?.episodeId, watchProgress?.positionMs)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("details_play_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = ZorvenObsidian
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (watchProgress != null) "Resume" else "Play",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Download Button
                    val downloadBtnText = when (mainDownload?.status) {
                        "COMPLETED" -> "Downloaded (${mainDownload.sizeMb} MB)"
                        "DOWNLOADING" -> "Downloading ${mainDownload.progressPercent}%..."
                        "PAUSED" -> "Paused (${mainDownload.progressPercent}%) • Tap to Resume"
                        else -> if (isSeries) "Download S$selectedSeason:E1" else "Download"
                    }
                    Button(
                        onClick = {
                            val epToDownload = if (isSeries) episodesForSelectedSeason.firstOrNull() else null
                            onDownload(media, epToDownload)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("details_download_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZorvenSurfaceHigh,
                            contentColor = ZorvenTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (mainDownload?.status == "COMPLETED") {
                                Icons.Filled.DownloadDone
                            } else {
                                Icons.Filled.Download
                            },
                            contentDescription = null,
                            tint = if (mainDownload?.status == "COMPLETED") ZorvenSuccessGreen else ZorvenTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = downloadBtnText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expandable Description
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(animationSpec = tween(220))
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                            .testTag("details_synopsis_box")
                    ) {
                        Text(
                            text = media.synopsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp,
                                color = ZorvenTextPrimary
                            ),
                            maxLines = if (isSynopsisExpanded) 20 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isSynopsisExpanded) "Show less" else "...more",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ZorvenTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cast, Creators & Genres
                    Text(
                        text = "Cast: ${media.castList.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Creators: ${media.creatorsList.joinToString(", ")} • Genres: ${media.genres.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Action Strip: My List, Rate, Share, Servers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailsActionItem(
                            icon = if (isInMyList) Icons.Filled.Check else Icons.Filled.Add,
                            label = "My List",
                            tint = if (isInMyList) ZorvenCrimson else ZorvenTextPrimary,
                            testTag = "details_action_mylist",
                            onClick = { onToggleMyList(media) }
                        )

                        DetailsActionItem(
                            icon = when (userRating) {
                                2 -> Icons.Filled.Favorite
                                1 -> Icons.Filled.ThumbUp
                                else -> Icons.Outlined.ThumbUp
                            },
                            label = when (userRating) {
                                2 -> "Loved"
                                1 -> "Rated"
                                0 -> "Not For Me"
                                else -> "Rate"
                            },
                            tint = if (userRating != null) ZorvenCrimson else ZorvenTextPrimary,
                            testTag = "details_action_rate",
                            onClick = { showRatingPicker = !showRatingPicker }
                        )

                        DetailsActionItem(
                            icon = Icons.Filled.Share,
                            label = "Share",
                            tint = ZorvenTextPrimary,
                            testTag = "details_action_share",
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Watch ${media.title} (${media.releaseYear}) in ${media.qualityBadge} on Zorvén!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share ${media.title}"))
                            }
                        )

                        DetailsActionItem(
                            icon = Icons.Filled.Dns,
                            label = "Servers",
                            tint = ZorvenSuccessGreen,
                            testTag = "details_action_servers",
                            onClick = { showServerInfoSheet = true }
                        )
                    }

                    // Expandable Rating Bar when "Rate" is tapped
                    AnimatedVisibility(
                        visible = showRatingPicker,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(150))
                    ) {
                        Surface(
                            color = ZorvenSurfaceElevated,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(50))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RatingOptionChip(
                                    icon = Icons.Outlined.ThumbDown,
                                    label = "Not for me",
                                    selected = userRating == 0,
                                    onClick = {
                                        onRate(media, 0)
                                        showRatingPicker = false
                                    }
                                )
                                RatingOptionChip(
                                    icon = Icons.Filled.ThumbUp,
                                    label = "I like this",
                                    selected = userRating == 1,
                                    onClick = {
                                        onRate(media, 1)
                                        showRatingPicker = false
                                    }
                                )
                                RatingOptionChip(
                                    icon = Icons.Filled.Favorite,
                                    label = "Love this!",
                                    selected = userRating == 2,
                                    onClick = {
                                        onRate(media, 2)
                                        showRatingPicker = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ZorvenBorderSubtle, thickness = 1.dp)

                    // Section Tabs (Episodes / More Like This / Trailers & More)
                    val tabs = if (isSeries) {
                        listOf("Episodes", "More Like This", "Trailers & More")
                    } else {
                        listOf("More Like This", "Trailers & More")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        tabs.forEachIndexed { idx, tabTitle ->
                            val isSelected = activeTabIndex == idx
                            Column(
                                modifier = Modifier
                                    .clickable { activeTabIndex = idx }
                                    .padding(top = 12.dp, bottom = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(84.dp)
                                        .height(3.5.dp)
                                        .background(
                                            if (isSelected) ZorvenCrimson else Color.Transparent,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) ZorvenTextPrimary else ZorvenTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Tab Content
            val showEpisodesTab = isSeries && activeTabIndex == 0
            val showMoreLikeThisTab = (isSeries && activeTabIndex == 1) || (!isSeries && activeTabIndex == 0)

            if (showEpisodesTab) {
                // Season Selector Header
                item(key = "season_selector_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ZorvenSurfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { showSeasonPickerSheet = true }
                                .testTag("season_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Season $selectedSeason",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenTextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Select season",
                                    tint = ZorvenTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "${episodesForSelectedSeason.size} Episodes",
                            style = MaterialTheme.typography.labelMedium,
                            color = ZorvenTextSecondary
                        )
                    }
                }

                // Episode List for Selected Season
                items(
                    items = episodesForSelectedSeason,
                    key = { it.id }
                ) { episode ->
                    val epDownload = downloads.find { it.episodeId == episode.id }
                    val isWatchedCurrentEp = watchProgress?.episodeId == episode.id
                    EpisodeListItemCard(
                        media = media,
                        episode = episode,
                        watchFraction = if (isWatchedCurrentEp) watchProgress?.progressFraction ?: 0f else 0f,
                        downloadState = epDownload,
                        onPlayEpisode = { onPlay(media.id, episode.id, 0L) },
                        onDownloadEpisode = { onDownload(media, episode) }
                    )
                }
            } else if (showMoreLikeThisTab) {
                // 3-Column Grid of Similar Movies & Series
                val chunkedSimilar = similarMedia.chunked(3)
                items(chunkedSimilar, key = { row -> row.joinToString("_") { it.id } }) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { similarItem ->
                            Box(modifier = Modifier.weight(1f)) {
                                PortraitPosterCard(
                                    media = similarItem,
                                    width = 160.dp,
                                    onClick = { onOpenOtherMediaDetails(similarItem.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Trailers & More Tab
                item(key = "trailers_and_specs") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = ZorvenSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlay(media.id, null, 0L) }
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
                                        contentDescription = "Official Trailer: ${media.title}",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.Center)
                                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play trailer",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Official 4K Theatrical Trailer — ${media.title}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ZorvenTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "2m 24s • ${media.qualityBadge} • ${media.audioBadge}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZorvenTextSecondary
                                    )
                                }
                            }
                        }

                        // Stream Server Endpoint Info Card
                        Surface(
                            color = ZorvenSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Custom Streaming Server Route",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenTextPrimary
                                )
                                Text(
                                    text = "Base API: $configuredApiUrl",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZorvenTextSecondary
                                )
                                Text(
                                    text = "Stream Path: ${media.apiEndpointSlug}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZorvenSuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Season Selector Bottom Sheet
        if (showSeasonPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSeasonPickerSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = ZorvenSurfaceElevated,
                contentColor = ZorvenTextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZorvenTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    seasonNumbers.forEach { seasonNum ->
                        val isCurrent = seasonNum == selectedSeason
                        Surface(
                            color = if (isCurrent) ZorvenCrimson.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSeason(seasonNum)
                                    showSeasonPickerSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Season $seasonNum",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isCurrent) ZorvenTextPrimary else ZorvenTextSecondary
                                )
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = ZorvenCrimson
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Server Info Bottom Sheet
        if (showServerInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showServerInfoSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = ZorvenSurfaceElevated,
                contentColor = ZorvenTextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Streaming Servers — ${media.title}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZorvenTextPrimary
                        )
                        IconButton(onClick = { showServerInfoSheet = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZorvenTextSecondary)
                        }
                    }
                    Text(
                        text = "Select any server to launch immediate full-screen playback, or switch servers on the fly inside the video player.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextSecondary
                    )

                    val serverNames = listOf(
                        "Custom API Server 1 • Primary" to "24 ms • Direct HLS/MP4 (${media.qualityBadge})",
                        "Custom API Server 2 • High-Bitrate Mirror" to "38 ms • Multi-CDN Relay",
                        "Zorvén Cinema Edge • EU-West" to "46 ms • Low-Latency Stream",
                        "Backup Server 4 • Global Failover" to "62 ms • Anycast Failover"
                    )
                    serverNames.forEach { (srvName, srvMeta) ->
                        Surface(
                            color = ZorvenSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    showServerInfoSheet = false
                                    onPlay(media.id, watchProgress?.episodeId, watchProgress?.positionMs)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = srvName,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = ZorvenTextPrimary
                                    )
                                    Text(
                                        text = srvMeta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZorvenSuccessGreen
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play on $srvName",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MetadataOutlineBadge(text: String) {
    Surface(
        color = ZorvenSurfaceElevated,
        shape = RoundedCornerShape(3.dp),
        modifier = Modifier.border(1.dp, ZorvenTextSecondary.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            ),
            color = ZorvenTextPrimary,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
        )
    }
}

@Composable
private fun DetailsActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ZorvenTextSecondary
        )
    }
}

@Composable
private fun RatingOptionChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) ZorvenCrimson.copy(alpha = 0.22f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) ZorvenCrimson else ZorvenTextPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = ZorvenTextPrimary
        )
    }
}

@Composable
private fun EpisodeListItemCard(
    media: MediaItemEntity,
    episode: EpisodeEntity,
    watchFraction: Float,
    downloadState: DownloadEntity?,
    onPlayEpisode: () -> Unit,
    onDownloadEpisode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayEpisode)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("episode_item_${episode.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 16:9 Episode Thumbnail with Play Button & Progress Bar
            Box(
                modifier = Modifier
                    .width(126.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, ZorvenBorderSubtle, RoundedCornerShape(6.dp))
            ) {
                CinematicMediaImage(
                    imageUrl = episode.thumbnailUrl,
                    localResName = media.localBackdropResName,
                    ambientHex = media.ambientColorHex,
                    secondaryHex = media.secondaryAmbientHex,
                    titleForFallback = episode.title,
                    showTitleOverlayOnPoster = false,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play ${episode.title}",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (watchFraction > 0f) {
                    LinearProgressIndicator(
                        progress = { watchFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = ZorvenCrimson,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            // Episode Number, Title & Duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.title}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${episode.durationMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZorvenTextMuted
                )
            }

            // Episode Download Button
            IconButton(
                onClick = onDownloadEpisode,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (downloadState?.status == "COMPLETED") {
                        Icons.Filled.DownloadDone
                    } else {
                        Icons.Filled.Download
                    },
                    contentDescription = "Download episode ${episode.episodeNumber}",
                    tint = if (downloadState?.status == "COMPLETED") ZorvenSuccessGreen else ZorvenTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = episode.synopsis,
            style = MaterialTheme.typography.bodySmall,
            color = ZorvenTextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
