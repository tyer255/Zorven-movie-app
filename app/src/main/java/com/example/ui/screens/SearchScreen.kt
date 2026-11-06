package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaItemEntity
import com.example.ui.SearchFilterType
import com.example.ui.components.CinematicMediaImage
import com.example.ui.components.PortraitPosterCard
import com.example.ui.components.ShimmerCatalogPlaceholder
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.ZorvenBorderSubtle
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenObsidian
import com.example.ui.theme.ZorvenSurface
import com.example.ui.theme.ZorvenSurfaceElevated
import com.example.ui.theme.ZorvenSurfaceHigh
import com.example.ui.theme.ZorvenTextMuted
import com.example.ui.theme.ZorvenTextPrimary
import com.example.ui.theme.ZorvenTextSecondary

private data class CuratedGenreCollection(
    val title: String,
    val subtitle: String,
    val filterType: SearchFilterType,
    val backdropUrl: String,
    val localResName: String?,
    val ambientHex: String
)

@Composable
fun SearchScreen(
    allMedia: List<MediaItemEntity>,
    query: String,
    selectedFilter: SearchFilterType,
    recentSearches: List<String>,
    isLoading: Boolean,
    hasSearchError: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (SearchFilterType) -> Unit,
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onToggleErrorSimulation: (Boolean) -> Unit,
    onOpenDetails: (String) -> Unit,
    onPlayMedia: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isInputFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isInputFocused) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(180),
        label = "search_focus_border"
    )

    val filteredResults = remember(allMedia, query, selectedFilter) {
        val base = when (selectedFilter) {
            SearchFilterType.ALL -> allMedia
            SearchFilterType.MOVIES -> allMedia.filter { it.type == "MOVIE" }
            SearchFilterType.SERIES -> allMedia.filter { it.type == "SERIES" }
            SearchFilterType.SCI_FI -> allMedia.filter { m -> m.genres.any { it.contains("Sci-Fi", true) } }
            SearchFilterType.THRILLER -> allMedia.filter { m -> m.genres.any { it.contains("Thriller", true) } }
            SearchFilterType.ACTION -> allMedia.filter { m -> m.genres.any { it.contains("Action", true) } }
            SearchFilterType.DRAMA -> allMedia.filter { m -> m.genres.any { it.contains("Drama", true) } }
            SearchFilterType.COMEDY -> allMedia.filter { m -> m.genres.any { it.contains("Comedy", true) } }
        }

        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            base
        } else {
            base.filter { item ->
                item.title.contains(cleanQuery, ignoreCase = true) ||
                    item.genresCsv.contains(cleanQuery, ignoreCase = true) ||
                    item.castCsv.contains(cleanQuery, ignoreCase = true) ||
                    item.creatorsCsv.contains(cleanQuery, ignoreCase = true) ||
                    item.synopsis.contains(cleanQuery, ignoreCase = true)
            }
        }
    }

    // Live autocomplete rows while typing
    val liveAutocompleteSuggestions = remember(allMedia, query) {
        val clean = query.trim()
        if (clean.isEmpty()) emptyList()
        else {
            val matchedTitles = allMedia
                .filter {
                    it.title.contains(clean, ignoreCase = true) ||
                        it.castCsv.contains(clean, ignoreCase = true) ||
                        it.genresCsv.contains(clean, ignoreCase = true)
                }
                .map { it.title to it.id }
            matchedTitles.take(4)
        }
    }

    val genreCollections = remember {
        listOf(
            CuratedGenreCollection(
                title = "Sci-Fi & Beyond",
                subtitle = "4K Dolby Atmos",
                filterType = SearchFilterType.SCI_FI,
                backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80",
                localResName = "img_hero_solaris_protocol_1793905910619",
                ambientHex = "#8A561E"
            ),
            CuratedGenreCollection(
                title = "Noir & Thrillers",
                subtitle = "Suspense & Crime",
                filterType = SearchFilterType.THRILLER,
                backdropUrl = "https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=800&q=80",
                localResName = "img_hero_velvet_syndicate_1793905911834",
                ambientHex = "#7A1624"
            ),
            CuratedGenreCollection(
                title = "Prestige Series",
                subtitle = "Multi-Season",
                filterType = SearchFilterType.SERIES,
                backdropUrl = "https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=800&q=80",
                localResName = "img_hero_crimson_dynasty_1793905912431",
                ambientHex = "#7D2418"
            ),
            CuratedGenreCollection(
                title = "Action & Speed",
                subtitle = "High Adrenaline",
                filterType = SearchFilterType.ACTION,
                backdropUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                localResName = null,
                ambientHex = "#1B4B66"
            ),
            CuratedGenreCollection(
                title = "Acclaimed Drama",
                subtitle = "Award Winners",
                filterType = SearchFilterType.DRAMA,
                backdropUrl = "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=800&q=80",
                localResName = null,
                ambientHex = "#2E3A4E"
            ),
            CuratedGenreCollection(
                title = "Comedies",
                subtitle = "Late-Night & Heists",
                filterType = SearchFilterType.COMEDY,
                backdropUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80",
                localResName = null,
                ambientHex = "#6E3B16"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZorvenObsidian)
            .statusBarsPadding()
            .imePadding()
            .testTag("search_screen")
    ) {
        // 1. Sleek Studio Search Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = Color(0xFF16161D),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = if (isInputFocused || query.isNotEmpty()) ZorvenTextPrimary else ZorvenTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search movies, series, genres, cast...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZorvenTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = ZorvenTextPrimary,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isInputFocused = it.isFocused }
                                .testTag("search_input_field")
                        )
                    }

                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150))
                    ) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = ZorvenTextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // Clean "Cancel" action when focused or filtered
            AnimatedVisibility(
                visible = isInputFocused || query.isNotEmpty() || selectedFilter != SearchFilterType.ALL
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = ZorvenTextSecondary,
                    modifier = Modifier
                        .clickable {
                            onQueryChange("")
                            onSelectFilter(SearchFilterType.ALL)
                            focusManager.clearFocus()
                        }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }

        // 2. Refined High-Contrast Filter Pills (White active state, dark charcoal inactive state)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SearchFilterType.entries, key = { it.name }) { filterType ->
                val selected = selectedFilter == filterType
                Surface(
                    color = if (selected) Color.White else Color(0xFF15151C),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onSelectFilter(filterType) }
                        .testTag("search_filter_${filterType.name.lowercase()}")
                ) {
                    Text(
                        text = filterType.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) ZorvenObsidian else ZorvenTextSecondary,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Main Search Body
        if (hasSearchError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                        tint = ZorvenCrimson,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Unable to Load Search Results",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZorvenTextPrimary
                    )
                    Text(
                        text = "Please verify your network connection and try again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZorvenTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { onToggleErrorSimulation(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = ZorvenObsidian
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (isLoading) {
            ShimmerCatalogPlaceholder()
        } else if (query.isBlank() && selectedFilter == SearchFilterType.ALL) {
            // IDLE DISCOVERY STATE:
            // A. Genuine Recent Searches (only if user has actually searched)
            // B. Visual "Browse by Collection" 16:9 Cards
            // C. Flagship "Recommended TV Shows & Movies" Editorial List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // A. Genuine Recent Searches (Strictly only when user has searched)
                if (recentSearches.isNotEmpty()) {
                    item(key = "genuine_recent_searches") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ZorvenTextSecondary
                                    )
                                )
                                TextButton(
                                    onClick = onClearRecentSearches,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ZorvenTextMuted
                                    )
                                }
                            }

                            recentSearches.take(4).forEach { term ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onQueryChange(term) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = ZorvenTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = term,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = ZorvenTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRemoveRecentSearch(term) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove $term",
                                            tint = ZorvenTextMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                        }
                    }
                }

                // B. Visual "Browse by Collection" Widescreen Cards
                item(key = "browse_collections_section") {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 18.dp)) {
                        Text(
                            text = "Browse by Collection",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = ZorvenTextPrimary,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(genreCollections, key = { it.title }) { collection ->
                                SearchCollectionTile(
                                    collection = collection,
                                    onClick = { onSelectFilter(collection.filterType) }
                                )
                            }
                        }
                    }
                }

                // C. Flagship "Recommended TV Shows & Movies" Editorial List
                item(key = "recommended_list_header") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp)
                    ) {
                        Text(
                            text = "Recommended TV Shows & Movies",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = ZorvenTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Most searched titles and new premieres on ZORVÉN",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZorvenTextSecondary
                        )
                    }
                }

                items(allMedia, key = { "suggested_${it.id}" }) { media ->
                    SearchEditorialMediaRow(
                        media = media,
                        onOpenDetails = { onOpenDetails(media.id) },
                        onPlay = { onPlayMedia(media.id) }
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else if (filteredResults.isEmpty()) {
            // Empty Search State + Popular Fallback Grid so the screen always feels alive and helpful
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                item(key = "empty_search_notice") {
                    Surface(
                        color = Color(0xFF121218),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No exact matches for \"$query\"",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZorvenTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Try searching for another title, actor, director, or browse our most popular releases below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZorvenTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    onQueryChange("")
                                    onSelectFilter(SearchFilterType.ALL)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = ZorvenObsidian
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Clear Search", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item(key = "fallback_popular_header") {
                    Text(
                        text = "Popular on ZORVÉN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = ZorvenTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                val fallbackRows = allMedia.chunked(3)
                items(fallbackRows, key = { r -> "fb_" + r.joinToString("_") { it.id } }) { rowMedia ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowMedia.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                PortraitPosterCard(
                                    media = item,
                                    width = 160.dp,
                                    onClick = { onOpenDetails(item.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        repeat(3 - rowMedia.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // ACTIVE SEARCH / FILTER RESULTS STATE
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // 1. Live Autocomplete Query Rows while typing
                if (liveAutocompleteSuggestions.isNotEmpty() && query.isNotBlank()) {
                    item(key = "autocomplete_list") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            liveAutocompleteSuggestions.forEach { (matchedTitle, matchedId) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            focusManager.clearFocus()
                                            onOpenDetails(matchedId)
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = ZorvenTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = matchedTitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = ZorvenTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.NorthWest,
                                        contentDescription = "Select $matchedTitle",
                                        tint = ZorvenTextMuted,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { onQueryChange(matchedTitle) }
                                    )
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // 2. Top Results Poster Grid (2:3 Portrait Cards with 12dp spacing)
                item(key = "grid_heading") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (query.isNotBlank()) "Top Results" else "${selectedFilter.label} Collection",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = ZorvenTextPrimary
                        )
                        Text(
                            text = "${filteredResults.size} titles",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZorvenTextSecondary
                        )
                    }
                }

                val rows = filteredResults.chunked(3)
                items(rows, key = { r -> "grid_row_" + r.joinToString("_") { it.id } }) { rowMedia ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowMedia.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                PortraitPosterCard(
                                    media = item,
                                    width = 160.dp,
                                    onClick = { onOpenDetails(item.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        repeat(3 - rowMedia.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 3. Detailed Editorial List Below Grid
                item(key = "detailed_matches_heading") {
                    Text(
                        text = "Detailed Overview & Direct Play",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = ZorvenTextPrimary,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                }

                items(filteredResults, key = { "top_match_${it.id}" }) { media ->
                    SearchEditorialMediaRow(
                        media = media,
                        onOpenDetails = { onOpenDetails(media.id) },
                        onPlay = { onPlayMedia(media.id) }
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual 16:9 Widescreen Collection Tile for "Browse by Collection" in idle Search state.
 */
@Composable
private fun SearchCollectionTile(
    collection: CuratedGenreCollection,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(164.dp)
            .height(92.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = ZorvenSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CinematicMediaImage(
                imageUrl = collection.backdropUrl,
                localResName = collection.localResName,
                ambientHex = collection.ambientHex,
                titleForFallback = collection.title,
                showTitleOverlayOnPoster = false,
                contentDescription = collection.title,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.84f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = ZorvenTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = collection.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = ZorvenTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Flagship Netflix / Apple TV+ style Editorial Recommendation & Search Row:
 * - 138dp 16:9 widescreen thumbnail with quality badge
 * - Crisp title, year/type/duration metadata, and genre tags
 * - Refined circular Play button on the right
 */
@Composable
private fun SearchEditorialMediaRow(
    media: MediaItemEntity,
    onOpenDetails: () -> Unit,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("search_result_${media.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 16:9 Widescreen Thumbnail with subtle badge
        Box(
            modifier = Modifier
                .width(138.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
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
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    )
            )

            Surface(
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = media.qualityBadge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                )
            }
        }

        // Title & Metadata Hierarchy
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = ZorvenTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${media.releaseYear} • ${if (media.type == "SERIES") "Series" else "Movie"} • ${media.durationOrSeasons} • ${media.maturityRating}",
                style = MaterialTheme.typography.labelSmall,
                color = ZorvenTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = media.genres.take(2).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = ZorvenTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Refined Studio Circular Play Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.32f), CircleShape)
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play ${media.title}",
                tint = ZorvenTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
