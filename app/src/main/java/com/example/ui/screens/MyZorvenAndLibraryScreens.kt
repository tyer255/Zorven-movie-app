package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadEntity
import com.example.data.local.MediaItemEntity
import com.example.data.local.MyListEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.UserRatingEntity
import com.example.data.local.WatchProgressEntity
import com.example.ui.components.CinematicMediaImage
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.PortraitPosterCard
import com.example.ui.components.ProfileAvatarBadge
import com.example.ui.components.ZorvenBrandWordmark
import com.example.ui.components.ZorvenMonogramIcon
import com.example.ui.theme.ZorvenBorderSubtle
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenErrorRed
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSuccessGreen
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenSurfaceHigh
import com.example.ui.theme.ZorvenTextMuted
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary
import com.example.ui.theme.ZorvenWarningAmber

/**
 * Completely Redesigned Profile / My ZORVÉN Account Hub:
 * - Deep black cinema aesthetic (#0A0A0C) with restrained dark surfaces (#121217) and subtle 1dp borders.
 * - Large, clean profile header with profile switcher, account status, and Manage Profiles action.
 * - Zero fake Watchlist or fake Continue Watching items — strictly reflects real user actions.
 * - Clean, well-spaced navigation sections for My List, Downloads, Continue Watching, Viewing Activity,
 *   Notifications, App Settings, Help & Support, About ZORVÉN, and Sign Out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyZorvenScreen(
    activeProfile: ProfileEntity?,
    allProfiles: List<ProfileEntity>,
    allMedia: List<MediaItemEntity>,
    myListEntries: List<MyListEntity>,
    watchProgressList: List<WatchProgressEntity>,
    downloadsList: List<DownloadEntity>,
    notifications: List<NotificationEntity>,
    userRatings: List<UserRatingEntity>,
    onSelectProfile: (ProfileEntity) -> Unit,
    onOpenProfileManagement: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onPlayMedia: (String, String?, Long?) -> Unit,
    onRemoveContinueWatching: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showViewingActivitySheet by remember { mutableStateOf(false) }
    var showHelpSupportSheet by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    // Strictly real saved My List items
    val myListMedia = remember(allMedia, myListEntries) {
        val ids = myListEntries.map { it.mediaId }.toSet()
        allMedia.filter { it.id in ids }
    }

    // Strictly real Continue Watching items
    val continueWatchingItems = remember(allMedia, watchProgressList) {
        watchProgressList.mapNotNull { wp ->
            val m = allMedia.find { it.id == wp.mediaId }
            if (m != null && wp.positionMs > 0L) m to wp else null
        }
    }

    // Strictly real Viewing Activity (watched or rated by user; never fabricated)
    val genuineActivityMedia = remember(allMedia, watchProgressList, userRatings) {
        val watchedIds = watchProgressList.map { it.mediaId }
        val ratedIds = userRatings.map { it.mediaId }
        val combinedIds = (watchedIds + ratedIds).distinct()
        combinedIds.mapNotNull { id -> allMedia.find { it.id == id } }
    }

    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }
    val totalDownloadedMb = remember(downloadsList) { downloadsList.sumOf { it.sizeMb } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .statusBarsPadding()
            .testTag("my_zorven_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // 1. Top Bar with Custom ZORVÉN Brand Logo & Quick Actions
        item(key = "profile_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZorvenBrandWordmark(
                    monogramSize = 24.dp,
                    fontSize = 17.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = ZorvenTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (unreadNotifCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(8.dp)
                                    .background(ZorvenCrimson, CircleShape)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("my_zorven_settings_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "App Settings",
                            tint = ZorvenTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Redesigned Profile Header Card
        item(key = "profile_header_section") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF15151C),
                                ZorvenObsidian
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activeProfile != null) {
                    ProfileAvatarBadge(
                        profile = activeProfile,
                        size = 84.dp,
                        isSelected = true,
                        onClick = onOpenProfileManagement,
                        modifier = Modifier.testTag("active_profile_header")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = activeProfile.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = ZorvenTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${activeProfile.maturityRestriction} • Ultra HD 4K & Spatial Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Manage Profiles & Switch Profile Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onOpenProfileManagement,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("manage_profiles_shortcut")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = ZorvenTextPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manage Profiles",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ZorvenTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Horizontal Profile Switcher Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allProfiles.forEach { prof ->
                        val isCurrent = prof.id == activeProfile?.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clickable { onSelectProfile(prof) }
                                .testTag("quick_switch_${prof.id}")
                        ) {
                            ProfileAvatarBadge(
                                profile = prof,
                                size = 44.dp,
                                isSelected = isCurrent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = prof.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isCurrent) ZorvenTextPrimary else ZorvenTextMuted
                            )
                        }
                    }
                }
            }
        }

        // 3. Genuine Continue Watching Section (16:9 Landscape Cards — Only shown when user has actual playback history)
        if (continueWatchingItems.isNotEmpty()) {
            item(key = "profile_continue_watching_section") {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
                    ProfileSectionTitle(
                        title = "Continue Watching",
                        trailingText = "${continueWatchingItems.size} active"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueWatchingItems, key = { "p_cw_${it.first.id}" }) { (media, progress) ->
                            ContinueWatchingCard(
                                media = media,
                                progress = progress,
                                onPlayClick = { onPlayMedia(media.id, progress.episodeId, progress.positionMs) },
                                onInfoClick = { onOpenDetails(media.id) },
                                onRemoveClick = { onRemoveContinueWatching(media.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Genuine My List Section (Shows saved titles or a clean minimal empty state)
        item(key = "profile_my_list_section") {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                ProfileSectionTitle(
                    title = "My List",
                    trailingText = if (myListMedia.isNotEmpty()) "${myListMedia.size} saved" else null
                )

                if (myListMedia.isEmpty()) {
                    Surface(
                        color = Color(0xFF111116),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = ZorvenTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No Saved Titles Yet",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Titles you add with + My List will appear here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZorvenTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myListMedia, key = { "p_mylist_${it.id}" }) { media ->
                            PortraitPosterCard(
                                media = media,
                                width = 114.dp,
                                onClick = { onOpenDetails(media.id) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Structured Library & Account Navigation Sections
        item(key = "profile_navigation_groups") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Group A: Library & Activity
                ProfileNavigationGroup(
                    groupLabel = "LIBRARY & ACTIVITY"
                ) {
                    ProfileCleanNavRow(
                        icon = Icons.Outlined.BookmarkBorder,
                        title = "My List",
                        valueText = if (myListMedia.isEmpty()) "Empty" else "${myListMedia.size} titles",
                        testTag = "hub_row_my_list",
                        onClick = {
                            if (myListMedia.isNotEmpty()) {
                                onOpenDetails(myListMedia.first().id)
                            } else {
                                showViewingActivitySheet = true
                            }
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.Outlined.Download,
                        title = "Downloads",
                        valueText = if (downloadsList.isEmpty()) {
                            "None"
                        } else {
                            "${downloadsList.size} (${String.format("%.1f", totalDownloadedMb / 1024f)} GB)"
                        },
                        testTag = "hub_row_downloads",
                        onClick = onOpenDownloads
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.Outlined.PlayCircle,
                        title = "Continue Watching",
                        valueText = if (continueWatchingItems.isEmpty()) {
                            "No active playback"
                        } else {
                            "${continueWatchingItems.size} in progress"
                        },
                        testTag = "hub_row_continue_watching",
                        onClick = {
                            val first = continueWatchingItems.firstOrNull()
                            if (first != null) {
                                onPlayMedia(first.first.id, first.second.episodeId, first.second.positionMs)
                            } else {
                                showViewingActivitySheet = true
                            }
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.Outlined.History,
                        title = "Viewing Activity",
                        valueText = if (genuineActivityMedia.isEmpty()) {
                            "No history yet"
                        } else {
                            "${genuineActivityMedia.size} titles"
                        },
                        testTag = "hub_row_viewing_activity",
                        onClick = { showViewingActivitySheet = true }
                    )
                }

                // Group B: Account, Settings & Support
                ProfileNavigationGroup(
                    groupLabel = "ACCOUNT & PREFERENCES"
                ) {
                    ProfileCleanNavRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        valueText = if (unreadNotifCount > 0) "$unreadNotifCount new" else "All read",
                        highlightValue = unreadNotifCount > 0,
                        testTag = "hub_row_notifications",
                        onClick = onOpenNotifications
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.Outlined.Person,
                        title = "Manage Profiles",
                        valueText = "${allProfiles.size} profiles",
                        testTag = "hub_row_manage_profiles",
                        onClick = onOpenProfileManagement
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.Outlined.Settings,
                        title = "App Settings",
                        valueText = "Playback, API & Storage",
                        testTag = "hub_row_settings",
                        onClick = onOpenSettings
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Help & Support",
                        valueText = null,
                        testTag = "hub_row_help",
                        onClick = { showHelpSupportSheet = true }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

                    ProfileCleanNavRow(
                        icon = Icons.Outlined.Info,
                        title = "About ZORVÉN",
                        valueText = "v1.0.0",
                        testTag = "hub_row_about",
                        onClick = { showAboutSheet = true }
                    )
                }

                // Sign Out Row
                Surface(
                    color = Color(0xFF111116),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable { showSignOutConfirmDialog = true }
                        .testTag("profile_sign_out_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = ZorvenTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ZorvenTextPrimary
                            )
                        }
                        Text(
                            text = activeProfile?.name ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenTextMuted
                        )
                    }
                }
            }
        }
    }

    // Viewing Activity Bottom Sheet (Strictly real playback & rating activity)
    if (showViewingActivitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showViewingActivitySheet = false },
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
                        text = "Viewing Activity — ${activeProfile?.name ?: "Profile"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { showViewingActivitySheet = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZorvenTextSecondary)
                    }
                }

                if (genuineActivityMedia.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = ZorvenTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No Viewing Activity Recorded Yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ZorvenTextPrimary
                        )
                        Text(
                            text = "When you play a movie or episode, or rate a title, your real activity log will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZorvenTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    genuineActivityMedia.forEach { media ->
                        val progress = watchProgressList.find { it.mediaId == media.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZorvenSurface)
                                .clickable {
                                    showViewingActivitySheet = false
                                    onOpenDetails(media.id)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = media.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZorvenTextPrimary
                                )
                                Text(
                                    text = progress?.episodeLabel ?: progress?.remainingMinutesText ?: "Rated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZorvenTextSecondary
                                )
                            }
                            if (progress != null) {
                                IconButton(onClick = { onRemoveContinueWatching(media.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Remove from activity",
                                        tint = ZorvenTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Help & Support Sheet
    if (showHelpSupportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSupportSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ZorvenSurfaceElevated,
            contentColor = ZorvenTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Help & Support",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { showHelpSupportSheet = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = ZorvenTextSecondary)
                    }
                }

                HelpSupportTopicCard(
                    title = "Connecting Your Custom Streaming Server API",
                    body = "Open App Settings → Custom Movie Streaming Server API to set your server base URL, or configure STREAM_API_BASE_URL and STREAM_API_KEY in the AI Studio Secrets panel. You can switch server mirrors live inside the Video Player."
                )
                HelpSupportTopicCard(
                    title = "Continue Watching & My List Privacy",
                    body = "Continue Watching and My List are stored locally in your device's Room database per profile. Nothing is pre-populated with fake history."
                )
                HelpSupportTopicCard(
                    title = "Offline Downloads & Smart Downloads",
                    body = "Tap Download on any movie or episode to store it for offline viewing. Manage storage or clear downloads anytime from the Downloads screen."
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // About ZORVÉN Sheet with Custom Vector Brand Identity
    if (showAboutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAboutSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ZorvenSurfaceElevated,
            contentColor = ZorvenTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ZorvenMonogramIcon(size = 48.dp)
                ZorvenBrandWordmark(monogramSize = 0.dp, fontSize = 22.sp, showMonogram = false)
                Text(
                    text = "Version 1.0.0 • Android Cinema Edition",
                    style = MaterialTheme.typography.labelMedium,
                    color = ZorvenTextSecondary
                )
                Text(
                    text = "ZORVÉN is a mobile-first cinema and series streaming client engineered with Jetpack Compose, adaptive 16:9 and 2:3 content modules, Room local database persistence, and multi-server custom streaming API resolution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZorvenTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showAboutSheet = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ZorvenObsidian
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            containerColor = ZorvenSurfaceElevated,
            titleContentColor = ZorvenTextPrimary,
            textContentColor = ZorvenTextSecondary,
            title = {
                Text("Sign Out of ${activeProfile?.name ?: "Profile"}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You will return to the profile selection screen. Your saved My List, Downloads, and playback positions remain stored on this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        onOpenProfileManagement()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZorvenCrimson)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancel", color = ZorvenTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileSectionTitle(
    title: String,
    trailingText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = ZorvenTextPrimary
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelSmall,
                color = ZorvenTextSecondary
            )
        }
    }
}

@Composable
private fun ProfileNavigationGroup(
    groupLabel: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = groupLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            ),
            color = ZorvenTextMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            color = Color(0xFF111116),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileCleanNavRow(
    icon: ImageVector,
    title: String,
    valueText: String?,
    highlightValue: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ZorvenTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = ZorvenTextPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!valueText.isNullOrBlank()) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (highlightValue) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (highlightValue) ZorvenCrimson else ZorvenTextMuted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ZorvenTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HelpSupportTopicCard(
    title: String,
    body: String
) {
    Surface(
        color = ZorvenSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ZorvenTextPrimary
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = ZorvenTextSecondary
            )
        }
    }
}

/**
 * Dedicated Downloads Screen with Smart Downloads status, Storage breakdown,
 * active/completed/error download states, remove download action, and empty state.
 */
@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    allMedia: List<MediaItemEntity>,
    smartDownloadsEnabled: Boolean,
    downloadQuality: String,
    onBack: () -> Unit,
    onPlayDownload: (DownloadEntity) -> Unit,
    onRetryOrToggleDownload: (MediaItemEntity) -> Unit,
    onSimulateFailedDownload: (MediaItemEntity) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onClearAllDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    onFindMoreToDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val usedMb = remember(downloads) { downloads.sumOf { it.sizeMb } }
    val usedGb = remember(usedMb) { String.format("%.2f", usedMb / 1024f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("downloads_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("downloads_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ZorvenTextPrimary
                    )
                }
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (downloads.isNotEmpty()) {
                    TextButton(onClick = onClearAllDownloads) {
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelMedium,
                            color = ZorvenTextSecondary
                        )
                    }
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Download Settings",
                        tint = ZorvenTextPrimary
                    )
                }
            }
        }

        Surface(
            color = ZorvenSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenSettings)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Downloads: ${if (smartDownloadsEnabled) "ON" else "OFF"} • $downloadQuality",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ZorvenTextPrimary
                )
                Text(
                    text = "$usedGb GB used",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZorvenTextSecondary
                )
            }
        }

        HorizontalDivider(color = ZorvenBorderSubtle)

        if (downloads.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(ZorvenSurfaceElevated, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        tint = ZorvenTextSecondary,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "No Downloads Yet",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Movies and series you download for offline viewing will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZorvenTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onFindMoreToDownload,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ZorvenObsidian
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Browse Titles to Download",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(downloads, key = { it.id }) { dl ->
                    val linkedMedia = allMedia.find { it.id == dl.mediaId }
                    DownloadItemRow(
                        download = dl,
                        onPlay = { onPlayDownload(dl) },
                        onRetryOrResume = {
                            if (linkedMedia != null) onRetryOrToggleDownload(linkedMedia)
                        },
                        onDelete = { onDeleteDownload(dl.id) }
                    )
                }

                item(key = "downloads_footer_actions") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onFindMoreToDownload,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZorvenSurfaceElevated,
                                contentColor = ZorvenTextPrimary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(1.dp, ZorvenBorderSubtle, RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Find More to Download",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadItemRow(
    download: DownloadEntity,
    onPlay: () -> Unit,
    onRetryOrResume: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (download.status == "COMPLETED") onPlay() else onRetryOrResume()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("download_item_${download.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(118.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(6.dp))
                .border(0.5.dp, ZorvenBorderSubtle, RoundedCornerShape(6.dp))
        ) {
            CinematicMediaImage(
                imageUrl = download.posterUrl,
                ambientHex = download.ambientColorHex,
                titleForFallback = download.title,
                contentDescription = download.title,
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
                    imageVector = if (download.status == "ERROR") Icons.Filled.Refresh else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = download.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ZorvenTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = download.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (download.status == "ERROR") ZorvenErrorRed else ZorvenTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))

            when (download.status) {
                "COMPLETED" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = ZorvenSuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Downloaded • ${download.quality}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenSuccessGreen
                        )
                    }
                }
                "DOWNLOADING", "PAUSED" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { download.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = ZorvenCrimson,
                            trackColor = ZorvenSurfaceHigh
                        )
                        Text(
                            text = "${download.status} (${download.progressPercent}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenWarningAmber
                        )
                    }
                }
                "ERROR" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = ZorvenErrorRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Download Failed • Tap to Retry",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ZorvenErrorRed
                        )
                    }
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = "Remove download",
                tint = ZorvenTextSecondary
            )
        }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit,
    onNotificationClick: (NotificationEntity) -> Unit,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("notifications_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("notifications_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ZorvenTextPrimary
                    )
                }
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary
                )
            }

            if (notifications.any { !it.isRead }) {
                TextButton(onClick = onMarkAllRead) {
                    Text(
                        text = "Mark All Read",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZorvenCrimson
                    )
                }
            }
        }

        HorizontalDivider(color = ZorvenBorderSubtle)

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = ZorvenTextSecondary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "All Caught Up",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZorvenTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "New arrivals, season premieres, and personalized recommendations will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZorvenTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (!notif.isRead) ZorvenSurfaceElevated.copy(alpha = 0.45f)
                                else Color.Transparent
                            )
                            .clickable { onNotificationClick(notif) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("notification_item_${notif.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (!notif.isRead) ZorvenCrimson else Color.Transparent,
                                    CircleShape
                                )
                        )

                        Box(
                            modifier = Modifier
                                .width(122.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(0.5.dp, ZorvenBorderSubtle, RoundedCornerShape(6.dp))
                        ) {
                            CinematicMediaImage(
                                imageUrl = notif.backdropUrl,
                                ambientHex = notif.ambientColorHex,
                                titleForFallback = notif.title,
                                contentDescription = notif.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.categoryTag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZorvenCrimson
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = ZorvenTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = ZorvenTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
