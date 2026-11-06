package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaItemEntity
import com.example.data.local.MyListEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.WatchProgressEntity
import com.example.ui.HomeFilterPill
import com.example.ui.components.CinematicMediaImage
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.LandscapePreviewCard
import com.example.ui.components.PortraitPosterCard
import com.example.ui.components.ProfileAvatarBadge
import com.example.ui.components.ShimmerCatalogPlaceholder
import com.example.ui.components.Top10PosterCard
import com.example.ui.components.ZorvenBrandWordmark
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenSurfaceHigh
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary
import com.example.ui.theme.parseHexColor

// Consistent Home Spacing System Tokens
private val SECTION_VERTICAL_SPACING = 28.dp
private val HEADING_BOTTOM_SPACING = 14.dp
private val CARD_HORIZONTAL_GAP = 12.dp
private val SCREEN_HORIZONTAL_PADDING = 16.dp

@Composable
fun HomeScreen(
    allMedia: List<MediaItemEntity>,
    activeProfile: ProfileEntity?,
    myListEntries: List<MyListEntity>,
    watchProgressList: List<WatchProgressEntity>,
    unreadNotificationCount: Int,
    selectedPill: HomeFilterPill,
    selectedCategoryGenre: String?,
    showCategoriesModal: Boolean,
    heroCarouselIndex: Int,
    isSkeletonLoading: Boolean,
    onSelectPill: (HomeFilterPill) -> Unit,
    onSelectCategoryGenre: (String?) -> Unit,
    onDismissCategoriesModal: () -> Unit,
    onStepHero: (Int) -> Unit,
    onOpenDetails: (String) -> Unit,
    onPlayMedia: (String, String?, Long?) -> Unit,
    onToggleMyList: (MediaItemEntity) -> Unit,
    onRemoveContinueWatching: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenProfileSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredCatalog = remember(allMedia, activeProfile, selectedPill, selectedCategoryGenre) {
        val maturityFiltered = if (activeProfile?.isKids == true) {
            allMedia.filter { it.maturityRating == "TV-Y7" || it.maturityRating == "PG" || it.maturityRating == "PG-13" }
        } else {
            allMedia
        }

        val pillFiltered = when (selectedPill) {
            HomeFilterPill.ALL -> maturityFiltered
            HomeFilterPill.TV_SHOWS -> maturityFiltered.filter { it.type == "SERIES" }
            HomeFilterPill.MOVIES -> maturityFiltered.filter { it.type == "MOVIE" }
            HomeFilterPill.NEW_AND_HOT -> maturityFiltered.filter { it.isNewRelease || it.isTrending }
            HomeFilterPill.CATEGORIES -> {
                if (selectedCategoryGenre.isNullOrBlank()) maturityFiltered
                else maturityFiltered.filter { item ->
                    item.genres.any { it.contains(selectedCategoryGenre, ignoreCase = true) }
                }
            }
        }
        pillFiltered.ifEmpty { maturityFiltered }
    }

    val heroPool = remember(filteredCatalog) {
        filteredCatalog.filter { it.localBackdropResName != null }.ifEmpty { filteredCatalog.take(4) }
    }
    val featuredHero = remember(heroPool, heroCarouselIndex) {
        if (heroPool.isEmpty()) null else heroPool[heroCarouselIndex % heroPool.size]
    }

    val targetAmbientColor = remember(featuredHero) {
        featuredHero?.let { parseHexColor(it.ambientColorHex, Color(0xFF42161C)) } ?: Color(0xFF281014)
    }
    val animatedAmbientColor by animateColorAsState(
        targetValue = targetAmbientColor,
        animationSpec = tween(durationMillis = 600),
        label = "home_ambient_canvas"
    )

    val myListIds = remember(myListEntries) { myListEntries.map { it.mediaId }.toSet() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .testTag("home_screen")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val adaptivePosterWidth: Dp = remember(screenWidth) {
            when {
                screenWidth < 350.dp -> 104.dp
                screenWidth < 400.dp -> 114.dp
                screenWidth < 600.dp -> 122.dp
                else -> 142.dp
            }
        }
        // ~25% taller hero banner that scales gracefully across phone dimensions
        val adaptiveHeroHeight: Dp = remember(screenHeight) {
            (screenHeight * 0.66f).coerceIn(520.dp, 580.dp)
        }

        // Restrained Ambient Top Lighting Canvas fading smoothly into deep black #0A0A0C
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(640.dp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to animatedAmbientColor.copy(alpha = 0.36f),
                            0.55f to animatedAmbientColor.copy(alpha = 0.14f),
                            1.0f to ZorvenObsidian
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // 1. Top Navigation & Horizontal Category Shortcuts
            item(key = "top_header_and_shortcuts") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp)
                ) {
                    HomeTopBar(
                        activeProfile = activeProfile,
                        unreadNotificationCount = unreadNotificationCount,
                        onOpenNotifications = onOpenNotifications,
                        onOpenDownloads = onOpenDownloads,
                        onOpenProfileSwitcher = onOpenProfileSwitcher
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    HomeCategoryShortcutsRow(
                        selectedPill = selectedPill,
                        selectedCategoryGenre = selectedCategoryGenre,
                        onSelectPill = onSelectPill,
                        onClearFilter = { onSelectCategoryGenre(null) }
                    )
                }
            }

            if (isSkeletonLoading || filteredCatalog.isEmpty()) {
                item(key = "skeleton_loading") {
                    ShimmerCatalogPlaceholder()
                }
            } else {
                // 2. Taller, Immersive Featured Hero Section
                if (featuredHero != null) {
                    item(key = "featured_hero_billboard") {
                        FeaturedHeroBillboard(
                            hero = featuredHero,
                            heroHeight = adaptiveHeroHeight,
                            isInMyList = myListIds.contains(featuredHero.id),
                            heroCount = heroPool.size,
                            activeHeroIndex = if (heroPool.isEmpty()) 0 else heroCarouselIndex % heroPool.size,
                            onPlayClick = { onPlayMedia(featuredHero.id, null, null) },
                            onToggleMyList = { onToggleMyList(featuredHero) },
                            onOpenDetails = { onOpenDetails(featuredHero.id) },
                            onStepHero = onStepHero,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = SCREEN_HORIZONTAL_PADDING,
                                    end = SCREEN_HORIZONTAL_PADDING,
                                    top = 12.dp,
                                    bottom = 4.dp
                                )
                        )
                    }
                }

                // 3. Continue Watching Row — 16:9 Landscape Cards (ONLY shown when actual user playback history exists)
                val continueItems = watchProgressList.mapNotNull { progress ->
                    val media = allMedia.find { it.id == progress.mediaId }
                    if (media != null && progress.positionMs > 0L) media to progress else null
                }
                if (continueItems.isNotEmpty()) {
                    item(key = "row_continue_watching") {
                        Column(modifier = Modifier.padding(top = SECTION_VERTICAL_SPACING)) {
                            SectionRowHeading(
                                title = "Continue Watching"
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = SCREEN_HORIZONTAL_PADDING),
                                horizontalArrangement = Arrangement.spacedBy(CARD_HORIZONTAL_GAP)
                            ) {
                                items(continueItems, key = { "cw_${it.first.id}" }) { (media, progress) ->
                                    ContinueWatchingCard(
                                        media = media,
                                        progress = progress,
                                        onPlayClick = {
                                            onPlayMedia(media.id, progress.episodeId, progress.positionMs)
                                        },
                                        onInfoClick = { onOpenDetails(media.id) },
                                        onRemoveClick = { onRemoveContinueWatching(media.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. My List Row — ONLY shown when the user has genuinely saved titles
                val myListItems = allMedia.filter { myListIds.contains(it.id) }
                if (myListItems.isNotEmpty()) {
                    item(key = "row_my_list") {
                        StandardPosterRow(
                            title = "My List",
                            items = myListItems,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }

                // 5. Trending Now Row (2:3 Portrait Cards)
                val trendingList = filteredCatalog.filter { it.isTrending }.ifEmpty { filteredCatalog }
                item(key = "row_trending_now") {
                    StandardPosterRow(
                        title = "Trending Now",
                        items = trendingList,
                        posterWidth = adaptivePosterWidth,
                        onOpenDetails = onOpenDetails
                    )
                }

                // 6. Top 10 Ranked Section (Dedicated Ranked Collection with White-to-Light-Gray Gradient Numbers)
                val top10List = allMedia
                    .filter { it.top10Rank != null }
                    .sortedBy { it.top10Rank }
                if (top10List.isNotEmpty()) {
                    item(key = "row_top_10") {
                        Column(modifier = Modifier.padding(top = SECTION_VERTICAL_SPACING)) {
                            SectionRowHeading(title = "Top 10 on ZORVÉN Today")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = SCREEN_HORIZONTAL_PADDING),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                itemsIndexed(top10List, key = { _, item -> "top10_${item.id}" }) { index, media ->
                                    Top10PosterCard(
                                        rank = media.top10Rank ?: (index + 1),
                                        media = media,
                                        onClick = { onOpenDetails(media.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Special Landscape Module — Widescreen 4K Cinema Previews (16:9 Landscape Cards)
                val widescreenPreviews = filteredCatalog.filter {
                    it.qualityBadge.contains("4K") || it.qualityBadge.contains("Dolby")
                }.ifEmpty { filteredCatalog.take(6) }
                if (widescreenPreviews.isNotEmpty()) {
                    item(key = "row_widescreen_previews") {
                        Column(modifier = Modifier.padding(top = SECTION_VERTICAL_SPACING)) {
                            SectionRowHeading(title = "Cinematic Widescreen Previews • 4K HDR")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = SCREEN_HORIZONTAL_PADDING),
                                horizontalArrangement = Arrangement.spacedBy(CARD_HORIZONTAL_GAP)
                            ) {
                                items(widescreenPreviews, key = { "wide_${it.id}" }) { media ->
                                    LandscapePreviewCard(
                                        media = media,
                                        width = 264.dp,
                                        onPlayClick = { onPlayMedia(media.id, null, null) },
                                        onDetailsClick = { onOpenDetails(media.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Popular Movies (2:3 Portrait Cards)
                val popularMovies = allMedia.filter { it.type == "MOVIE" }
                if (popularMovies.isNotEmpty()) {
                    item(key = "row_popular_movies") {
                        StandardPosterRow(
                            title = "Popular Movies",
                            items = popularMovies,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }

                // 9. Popular TV Shows (2:3 Portrait Cards)
                val popularSeries = allMedia.filter { it.type == "SERIES" }
                if (popularSeries.isNotEmpty()) {
                    item(key = "row_popular_series") {
                        StandardPosterRow(
                            title = "Popular TV Shows",
                            items = popularSeries,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }

                // 10. New Releases (2:3 Portrait Cards)
                val newReleases = filteredCatalog.filter { it.isNewRelease }.ifEmpty { filteredCatalog }
                item(key = "row_new_releases") {
                    StandardPosterRow(
                        title = "New Releases",
                        items = newReleases,
                        posterWidth = adaptivePosterWidth,
                        onOpenDetails = onOpenDetails
                    )
                }

                // 11. Recommended for You (2:3 Portrait Cards)
                val recommended = filteredCatalog.filter { it.isRecommended }.ifEmpty { filteredCatalog.reversed() }
                item(key = "row_recommended") {
                    StandardPosterRow(
                        title = "Recommended for You",
                        items = recommended,
                        posterWidth = adaptivePosterWidth,
                        onOpenDetails = onOpenDetails
                    )
                }

                // 12. Action & Adventure
                val actionList = allMedia.filter { item ->
                    item.genres.any { it.contains("Action", ignoreCase = true) }
                }
                if (actionList.isNotEmpty()) {
                    item(key = "row_action") {
                        StandardPosterRow(
                            title = "Action & Adventure",
                            items = actionList,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }

                // 13. Thrillers & Noir
                val thrillerList = allMedia.filter { item ->
                    item.genres.any { it.contains("Thriller", ignoreCase = true) }
                }
                if (thrillerList.isNotEmpty()) {
                    item(key = "row_thriller") {
                        StandardPosterRow(
                            title = "Edge-of-Your-Seat Thrillers",
                            items = thrillerList,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }

                // 14. Critically Acclaimed Dramas
                val dramaList = allMedia.filter { item ->
                    item.genres.any { it.contains("Drama", ignoreCase = true) }
                }
                if (dramaList.isNotEmpty()) {
                    item(key = "row_drama") {
                        StandardPosterRow(
                            title = "Critically Acclaimed Dramas",
                            items = dramaList,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }

                // 15. Comedies
                val comedyList = allMedia.filter { item ->
                    item.genres.any { it.contains("Comedy", ignoreCase = true) }
                }
                if (comedyList.isNotEmpty()) {
                    item(key = "row_comedy") {
                        StandardPosterRow(
                            title = "Comedies & Late-Night Favorites",
                            items = comedyList,
                            posterWidth = adaptivePosterWidth,
                            onOpenDetails = onOpenDetails
                        )
                    }
                }
            }
        }

        // Full-Screen Categories Filter Overlay
        AnimatedVisibility(
            visible = showCategoriesModal,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180))
        ) {
            CategoriesPickerModal(
                selectedGenre = selectedCategoryGenre,
                onSelectGenre = onSelectCategoryGenre,
                onDismiss = onDismissCategoriesModal
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    activeProfile: ProfileEntity?,
    unreadNotificationCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenProfileSwitcher: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SCREEN_HORIZONTAL_PADDING, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Geometric SVG/Vector ZORVÉN Brand Logo
        ZorvenBrandWordmark(
            monogramSize = 26.dp,
            fontSize = 19.sp
        )

        // Right Actions: Downloads, Notifications, Profile Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onOpenDownloads,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("home_downloads_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Downloads",
                    tint = ZorvenTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box {
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("home_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = ZorvenTextPrimary,
                        modifier = Modifier.size(23.dp)
                    )
                }
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(16.dp)
                            .background(ZorvenCrimson, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadNotificationCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            if (activeProfile != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onOpenProfileSwitcher)
                        .testTag("home_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatarBadge(
                        profile = activeProfile,
                        size = 28.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryShortcutsRow(
    selectedPill: HomeFilterPill,
    selectedCategoryGenre: String?,
    onSelectPill: (HomeFilterPill) -> Unit,
    onClearFilter: () -> Unit
) {
    val pills = listOf(
        HomeFilterPill.TV_SHOWS,
        HomeFilterPill.MOVIES,
        HomeFilterPill.CATEGORIES,
        HomeFilterPill.NEW_AND_HOT
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = SCREEN_HORIZONTAL_PADDING, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedPill != HomeFilterPill.ALL || selectedCategoryGenre != null) {
            item(key = "clear_filter_chip") {
                Surface(
                    shape = CircleShape,
                    color = ZorvenSurfaceElevated,
                    modifier = Modifier
                        .size(34.dp)
                        .border(1.dp, ZorvenTextSecondary.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onClearFilter)
                        .testTag("clear_home_filter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear filter",
                            tint = ZorvenTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        items(pills, key = { it.name }) { pill ->
            val isSelected = selectedPill == pill
            val labelText = if (pill == HomeFilterPill.CATEGORIES && selectedCategoryGenre != null) {
                selectedCategoryGenre
            } else {
                pill.label
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.32f),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else ZorvenTextSecondary.copy(alpha = 0.32f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelectPill(pill) }
                    .testTag("home_pill_${pill.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = ZorvenTextPrimary,
                        maxLines = 1
                    )
                    if (pill == HomeFilterPill.CATEGORIES) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ZorvenTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroBillboard(
    hero: MediaItemEntity,
    heroHeight: Dp,
    isInMyList: Boolean,
    heroCount: Int,
    activeHeroIndex: Int,
    onPlayClick: () -> Unit,
    onToggleMyList: () -> Unit,
    onOpenDetails: () -> Unit,
    onStepHero: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle immersive cinema breathing scale on the hero background artwork
    val infiniteTransition = rememberInfiniteTransition(label = "hero_cinema_breath")
    val subtleZoom by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_zoom_scale"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 540.dp)
                .height(heroHeight)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenDetails)
                .testTag("hero_billboard_card"),
            color = ZorvenSurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Smooth Crossfade + subtle breathing motion on Featured Hero artwork
                Crossfade(
                    targetState = hero,
                    animationSpec = tween(durationMillis = 520),
                    label = "hero_artwork_crossfade"
                ) { currentHero ->
                    CinematicMediaImage(
                        imageUrl = currentHero.backdropUrl,
                        localResName = currentHero.localBackdropResName,
                        ambientHex = currentHero.ambientColorHex,
                        secondaryHex = currentHero.secondaryAmbientHex,
                        titleForFallback = currentHero.title,
                        showTitleOverlayOnPoster = false,
                        contentDescription = currentHero.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = subtleZoom
                                scaleY = subtleZoom
                            }
                    )
                }

                // Extended multi-stop bottom-to-black gradient that gives top artwork room to breathe
                // and transitions smoothly into the dark lower section
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.15f),
                                    0.46f to Color.Transparent,
                                    0.68f to Color(0xFF0A0A0C).copy(alpha = 0.65f),
                                    0.85f to Color(0xFF0A0A0C).copy(alpha = 0.92f),
                                    1.0f to Color(0xFF0A0A0C)
                                )
                            )
                        )
                )

                // Top-right Carousel Indicator Dots
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(heroCount.coerceAtMost(5)) { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == activeHeroIndex) 18.dp else 6.dp, 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (idx == activeHeroIndex) ZorvenCrimson
                                    else Color.White.copy(alpha = 0.35f)
                                )
                                .clickable { onStepHero(1) }
                        )
                    }
                }

                // Lower Hero Section: Ranking/Original Badge, Large Title, Metadata, Tagline & Dual CTAs
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ranking / Format Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hero.top10Rank != null && hero.top10Rank <= 10) {
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
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                )
                            }
                            Text(
                                text = "#${hero.top10Rank} in ${if (hero.type == "SERIES") "TV Shows" else "Movies"} Today",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary
                            )
                        } else {
                            Text(
                                text = "ZORVÉN PREMIERE • ${hero.qualityBadge}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.6.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ZorvenTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Title Typography
                    Text(
                        text = hero.title,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = BebasNeueFontFamily,
                            fontSize = 42.sp,
                            lineHeight = 44.sp,
                            letterSpacing = 1.4.sp
                        ),
                        color = ZorvenTextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Metadata & Genres row
                    val genreSummary = remember(hero.genres, hero.maturityRating, hero.qualityBadge) {
                        (hero.genres.take(3) + listOf(hero.maturityRating, hero.qualityBadge)).joinToString(" • ")
                    }
                    Text(
                        text = genreSummary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ZorvenTextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Short description / tagline
                    Text(
                        text = hero.subtitleTagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextSecondary.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Action Buttons: Play & My List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPlayClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("hero_play_button"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = ZorvenObsidian
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onToggleMyList,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("hero_mylist_button"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZorvenSurfaceHigh.copy(alpha = 0.90f),
                                contentColor = ZorvenTextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = if (isInMyList) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "My List",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionRowHeading(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = (-0.2).sp
        ),
        color = ZorvenTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(
            start = SCREEN_HORIZONTAL_PADDING,
            end = SCREEN_HORIZONTAL_PADDING,
            bottom = HEADING_BOTTOM_SPACING
        )
    )
}

@Composable
private fun StandardPosterRow(
    title: String,
    items: List<MediaItemEntity>,
    posterWidth: Dp,
    onOpenDetails: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = SECTION_VERTICAL_SPACING)) {
        SectionRowHeading(title = title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = SCREEN_HORIZONTAL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(CARD_HORIZONTAL_GAP)
        ) {
            items(items, key = { "${title}_${it.id}" }) { media ->
                PortraitPosterCard(
                    media = media,
                    width = posterWidth,
                    onClick = { onOpenDetails(media.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoriesPickerModal(
    selectedGenre: String?,
    onSelectGenre: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        "All Categories" to null,
        "Action & Adventure" to "Action",
        "Sci-Fi & Cyberpunk" to "Sci-Fi",
        "Thrillers & Noir" to "Thriller",
        "Acclaimed Dramas" to "Drama",
        "Comedies" to "Comedy",
        "Crime & Mystery" to "Mystery",
        "Historical Epics" to "History",
        "Kids & Family" to "Kids"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZorvenObsidian.copy(alpha = 0.96f))
            .statusBarsPadding()
            .testTag("categories_modal")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp),
            contentPadding = PaddingValues(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(categories) { (label, value) ->
                val isSelected = selectedGenre == value
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (isSelected) 22.sp else 19.sp
                    ),
                    color = if (isSelected) ZorvenTextPrimary else ZorvenTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGenre(value) }
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(54.dp)
                .clickable(onClick = onDismiss)
                .testTag("close_categories_modal_button"),
            shape = CircleShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close categories",
                    tint = ZorvenObsidian,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
