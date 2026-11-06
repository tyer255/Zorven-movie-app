package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ClipItemEntity
import com.example.data.local.DownloadEntity
import com.example.data.local.EpisodeEntity
import com.example.data.local.MediaItemEntity
import com.example.data.local.MyListEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.UserRatingEntity
import com.example.data.local.WatchProgressEntity
import com.example.data.local.ZorvenDatabase
import com.example.data.remote.StreamServerNode
import com.example.data.repository.StreamingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PrimaryTab {
    HOME,
    CLIPS,
    SEARCH,
    MY_ZORVEN
}

sealed interface AppScreen {
    data object Splash : AppScreen
    data class ProfileGate(val isManageMode: Boolean = false) : AppScreen
    data object MainTabs : AppScreen
    data class Details(val mediaId: String) : AppScreen
    data class VideoPlayer(
        val mediaId: String,
        val episodeId: String? = null,
        val startPositionMs: Long = 0L
    ) : AppScreen
    data object Downloads : AppScreen
    data object Notifications : AppScreen
    data object Settings : AppScreen
}

enum class HomeFilterPill(val label: String) {
    ALL("All"),
    TV_SHOWS("TV Shows"),
    MOVIES("Movies"),
    CATEGORIES("Categories"),
    NEW_AND_HOT("New & Hot")
}

enum class SearchFilterType(val label: String) {
    ALL("All"),
    MOVIES("Movies"),
    SERIES("TV Shows"),
    SCI_FI("Sci-Fi"),
    THRILLER("Thriller"),
    ACTION("Action"),
    DRAMA("Drama"),
    COMEDY("Comedy")
}

data class AppSettingsUiState(
    val language: String = "English (United States)",
    val notificationsEnabled: Boolean = true,
    val newArrivalsAlerts: Boolean = true,
    val videoPlaybackQuality: String = "Auto (Up to 4K HDR)",
    val mobileDataUsage: String = "Automatic",
    val wifiOnlyDownloads: Boolean = true,
    val downloadQuality: String = "High (1080p Full HD)",
    val downloadLocation: String = "Internal Storage (48.4 GB Free)",
    val smartDownloadsEnabled: Boolean = true,
    val deleteWatchedDownloads: Boolean = false,
    val subtitleSize: String = "Medium",
    val subtitleBackgroundStyle: String = "Semi-Transparent Cinema Box",
    val preferredAudioLanguage: String = "Original (Dolby Atmos / 5.1)",
    val autoplayNextEpisode: Boolean = true,
    val autoplayPreviews: Boolean = true,
    val reducedMotion: Boolean = false,
    val customStreamApiUrl: String = "",
    val apiStatusMessage: String = "Connected • 4 Server Nodes Ready"
)

data class PlayerSessionUiState(
    val mediaItem: MediaItemEntity? = null,
    val currentEpisode: EpisodeEntity? = null,
    val allEpisodesForSeries: List<EpisodeEntity> = emptyList(),
    val servers: List<StreamServerNode> = emptyList(),
    val selectedServer: StreamServerNode? = null,
    val selectedQuality: String = "Auto (4K / 1080p)",
    val selectedAudioTrack: String = "English (Dolby Atmos)",
    val selectedSubtitleTrack: String = "English [CC]",
    val isLoadingServers: Boolean = false,
    val hasPlaybackError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class StreamingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ZorvenDatabase.getInstance(application)
    val repository = StreamingRepository(database.streamingDao())

    // Navigation Backstack
    private val _screenStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Splash))
    val currentScreen: StateFlow<AppScreen> = _screenStack
        .combine(MutableStateFlow(Unit)) { stack, _ -> stack.lastOrNull() ?: AppScreen.MainTabs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppScreen.Splash)

    private val _selectedTab = MutableStateFlow(PrimaryTab.HOME)
    val selectedTab: StateFlow<PrimaryTab> = _selectedTab.asStateFlow()

    // Active Profile
    private val _activeProfileId = MutableStateFlow("profile_primary")
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    val profiles: StateFlow<List<ProfileEntity>> = repository.profiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<ProfileEntity?> = combine(profiles, _activeProfileId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Media Catalog
    val allMedia: StateFlow<List<MediaItemEntity>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClips: StateFlow<List<ClipItemEntity>> = repository.allClips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile-scoped reactive flows
    val myListEntries: StateFlow<List<MyListEntity>> = _activeProfileId
        .flatMapLatest { repository.observeMyList(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchProgressList: StateFlow<List<WatchProgressEntity>> = _activeProfileId
        .flatMapLatest { repository.observeWatchProgress(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadsList: StateFlow<List<DownloadEntity>> = _activeProfileId
        .flatMapLatest { repository.observeDownloads(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRatings: StateFlow<List<UserRatingEntity>> = _activeProfileId
        .flatMapLatest { repository.observeUserRatings(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Home Screen State
    private val _selectedHomePill = MutableStateFlow(HomeFilterPill.ALL)
    val selectedHomePill: StateFlow<HomeFilterPill> = _selectedHomePill.asStateFlow()

    private val _selectedCategoryGenre = MutableStateFlow<String?>(null)
    val selectedCategoryGenre: StateFlow<String?> = _selectedCategoryGenre.asStateFlow()

    private val _showCategoriesModal = MutableStateFlow(false)
    val showCategoriesModal: StateFlow<Boolean> = _showCategoriesModal.asStateFlow()

    private val _heroCarouselIndex = MutableStateFlow(0)
    val heroCarouselIndex: StateFlow<Int> = _heroCarouselIndex.asStateFlow()

    private val _isHomeSkeletonLoading = MutableStateFlow(false)
    val isHomeSkeletonLoading: StateFlow<Boolean> = _isHomeSkeletonLoading.asStateFlow()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilterType.ALL)
    val searchFilter: StateFlow<SearchFilterType> = _searchFilter.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _simulateSearchError = MutableStateFlow(false)
    val simulateSearchError: StateFlow<Boolean> = _simulateSearchError.asStateFlow()

    // Start empty — only populate when the user genuinely performs a search
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private var searchDebounceJob: Job? = null

    // Details Page State
    private val _selectedSeasonBySeries = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedSeasonBySeries: StateFlow<Map<String, Int>> = _selectedSeasonBySeries.asStateFlow()

    private val _episodesBySeries = MutableStateFlow<Map<String, List<EpisodeEntity>>>(emptyMap())
    val episodesBySeries: StateFlow<Map<String, List<EpisodeEntity>>> = _episodesBySeries.asStateFlow()

    // Player Session State
    private val _playerSession = MutableStateFlow(PlayerSessionUiState())
    val playerSession: StateFlow<PlayerSessionUiState> = _playerSession.asStateFlow()

    // Settings State
    private val _settingsState = MutableStateFlow(
        AppSettingsUiState(customStreamApiUrl = repository.getConfiguredApiBaseUrl())
    )
    val settingsState: StateFlow<AppSettingsUiState> = _settingsState.asStateFlow()

    // Toast / Snackbar Feedback Banner
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val activeDownloadJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        // Auto-advance Featured Hero every 9 seconds with smooth crossfade
        viewModelScope.launch {
            while (true) {
                delay(9000L)
                _heroCarouselIndex.update { it + 1 }
            }
        }
    }

    fun showFeedback(message: String) {
        viewModelScope.launch {
            _feedbackMessage.value = message
            delay(2600L)
            if (_feedbackMessage.value == message) {
                _feedbackMessage.value = null
            }
        }
    }

    // Navigation Actions
    fun onSplashFinished() {
        _screenStack.value = listOf(AppScreen.MainTabs)
    }

    fun replaySplashAnimation() {
        _screenStack.update { it + AppScreen.Splash }
    }

    fun openProfileGate(manageMode: Boolean = false) {
        _screenStack.update { it + AppScreen.ProfileGate(isManageMode = manageMode) }
    }

    fun selectProfile(profile: ProfileEntity) {
        _activeProfileId.value = profile.id
        _selectedHomePill.value = HomeFilterPill.ALL
        _selectedCategoryGenre.value = null
        _screenStack.value = listOf(AppScreen.MainTabs)
        showFeedback("Switched profile to ${profile.name}")
    }

    fun selectPrimaryTab(tab: PrimaryTab) {
        _selectedTab.value = tab
        _screenStack.value = listOf(AppScreen.MainTabs)
    }

    fun openDetails(mediaId: String) {
        viewModelScope.launch {
            val eps = repository.getEpisodesOnce(mediaId)
            if (eps.isNotEmpty()) {
                _episodesBySeries.update { it + (mediaId to eps) }
                if (!_selectedSeasonBySeries.value.containsKey(mediaId)) {
                    _selectedSeasonBySeries.update { it + (mediaId to 1) }
                }
            }
        }
        _screenStack.update { it + AppScreen.Details(mediaId) }
    }

    fun openVideoPlayer(mediaId: String, episodeId: String? = null, startPositionMs: Long? = null) {
        viewModelScope.launch {
            val media = allMedia.value.find { it.id == mediaId } ?: repository.getEpisodesOnce(mediaId).let { null }
            val resolvedMedia = media ?: database.streamingDao().getMediaById(mediaId) ?: return@launch
            val seriesEpisodes = if (resolvedMedia.type == "SERIES") {
                repository.getEpisodesOnce(mediaId)
            } else {
                emptyList()
            }
            val existingProgress = watchProgressList.value.find { it.mediaId == mediaId }
            val resolvedEpisode = if (resolvedMedia.type == "SERIES") {
                val targetEpId = episodeId ?: existingProgress?.episodeId
                seriesEpisodes.find { it.id == targetEpId } ?: seriesEpisodes.firstOrNull()
            } else null

            val initialPosition = startPositionMs ?: existingProgress?.positionMs ?: 0L

            _playerSession.value = PlayerSessionUiState(
                mediaItem = resolvedMedia,
                currentEpisode = resolvedEpisode,
                allEpisodesForSeries = seriesEpisodes,
                isLoadingServers = true,
                hasPlaybackError = false
            )
            _screenStack.update {
                it + AppScreen.VideoPlayer(
                    mediaId = mediaId,
                    episodeId = resolvedEpisode?.id,
                    startPositionMs = initialPosition
                )
            }

            val servers = repository.resolveStreamServers(resolvedMedia, resolvedEpisode)
            val primaryServer = servers.firstOrNull()
            _playerSession.update {
                it.copy(
                    servers = servers,
                    selectedServer = primaryServer,
                    selectedQuality = primaryServer?.qualityOptions?.firstOrNull() ?: "Auto (4K / 1080p)",
                    selectedAudioTrack = primaryServer?.audioTracks?.firstOrNull() ?: "English (Dolby Atmos)",
                    selectedSubtitleTrack = "English [CC]",
                    isLoadingServers = false
                )
            }
        }
    }

    fun switchEpisodeInPlayer(episode: EpisodeEntity) {
        val currentMedia = _playerSession.value.mediaItem ?: return
        viewModelScope.launch {
            _playerSession.update {
                it.copy(
                    currentEpisode = episode,
                    isLoadingServers = true,
                    hasPlaybackError = false
                )
            }
            val servers = repository.resolveStreamServers(currentMedia, episode)
            _playerSession.update {
                it.copy(
                    servers = servers,
                    selectedServer = servers.firstOrNull(),
                    isLoadingServers = false
                )
            }
            showFeedback("Playing S${episode.seasonNumber}:E${episode.episodeNumber} \"${episode.title}\"")
        }
    }

    fun selectStreamServer(server: StreamServerNode) {
        _playerSession.update {
            it.copy(
                selectedServer = server,
                selectedQuality = server.qualityOptions.firstOrNull() ?: it.selectedQuality,
                hasPlaybackError = false
            )
        }
        showFeedback("Switched to ${server.serverName}")
    }

    fun selectPlayerQuality(quality: String) {
        _playerSession.update { it.copy(selectedQuality = quality) }
        showFeedback("Video quality set to $quality")
    }

    fun selectPlayerAudioAndSubtitle(audio: String, subtitle: String) {
        _playerSession.update {
            it.copy(selectedAudioTrack = audio, selectedSubtitleTrack = subtitle)
        }
        showFeedback("Audio: $audio • Subtitles: $subtitle")
    }

    fun toggleSimulatedPlaybackError(hasError: Boolean) {
        _playerSession.update { it.copy(hasPlaybackError = hasError) }
    }

    fun openDownloadsScreen() {
        _screenStack.update { it + AppScreen.Downloads }
    }

    fun openNotificationsScreen() {
        _screenStack.update { it + AppScreen.Notifications }
    }

    fun openSettingsScreen() {
        _screenStack.update { it + AppScreen.Settings }
    }

    fun navigateBack(): Boolean {
        val currentStack = _screenStack.value
        if (currentStack.size > 1) {
            _screenStack.value = currentStack.dropLast(1)
            return true
        }
        if (_selectedTab.value != PrimaryTab.HOME) {
            _selectedTab.value = PrimaryTab.HOME
            return true
        }
        if (_selectedHomePill.value != HomeFilterPill.ALL || _selectedCategoryGenre.value != null) {
            _selectedHomePill.value = HomeFilterPill.ALL
            _selectedCategoryGenre.value = null
            return true
        }
        return false
    }

    // Home Filtering
    fun selectHomePill(pill: HomeFilterPill) {
        if (pill == HomeFilterPill.CATEGORIES) {
            _showCategoriesModal.value = true
            return
        }
        viewModelScope.launch {
            _isHomeSkeletonLoading.value = true
            _selectedHomePill.value = pill
            if (pill != HomeFilterPill.CATEGORIES) {
                _selectedCategoryGenre.value = null
            }
            delay(220L)
            _isHomeSkeletonLoading.value = false
        }
    }

    fun selectCategoryFromModal(genre: String?) {
        _showCategoriesModal.value = false
        viewModelScope.launch {
            _isHomeSkeletonLoading.value = true
            _selectedCategoryGenre.value = genre
            _selectedHomePill.value = if (genre == null) HomeFilterPill.ALL else HomeFilterPill.CATEGORIES
            delay(220L)
            _isHomeSkeletonLoading.value = false
        }
    }

    fun dismissCategoriesModal() {
        _showCategoriesModal.value = false
    }

    fun stepHeroIndex(delta: Int) {
        _heroCarouselIndex.update { (it + delta).coerceAtLeast(0) }
    }

    // Search Actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _simulateSearchError.value = false
        searchDebounceJob?.cancel()
        if (query.isBlank()) {
            _isSearchLoading.value = false
            return
        }
        searchDebounceJob = viewModelScope.launch {
            _isSearchLoading.value = true
            delay(240L)
            _isSearchLoading.value = false
            if (query.trim().length >= 3) {
                val trimmed = query.trim()
                _recentSearches.update { existing ->
                    listOf(trimmed) + existing.filterNot { it.equals(trimmed, ignoreCase = true) }.take(5)
                }
            }
        }
    }

    fun selectSearchFilter(filter: SearchFilterType) {
        _searchFilter.value = filter
    }

    fun removeRecentSearch(term: String) {
        _recentSearches.update { list -> list.filterNot { it.equals(term, ignoreCase = true) } }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun toggleSearchErrorSimulation(enable: Boolean) {
        _simulateSearchError.value = enable
    }

    // Details & Season Selection
    fun selectSeasonForSeries(seriesId: String, seasonNumber: Int) {
        _selectedSeasonBySeries.update { it + (seriesId to seasonNumber) }
    }

    // Library Actions (My List, Watch Progress, Ratings, Downloads)
    fun toggleMyList(mediaItem: MediaItemEntity) {
        val profileId = _activeProfileId.value
        val inList = myListEntries.value.any { it.mediaId == mediaItem.id }
        viewModelScope.launch {
            repository.toggleMyList(profileId, mediaItem.id, inList)
            showFeedback(
                if (inList) "Removed \"${mediaItem.title}\" from My List"
                else "Added \"${mediaItem.title}\" to My List"
            )
        }
    }

    fun setRating(mediaItem: MediaItemEntity, rating: Int) {
        val profileId = _activeProfileId.value
        viewModelScope.launch {
            repository.setRating(profileId, mediaItem.id, rating)
            val label = when (rating) {
                2 -> "Loved \"${mediaItem.title}\"!"
                1 -> "Rated \"${mediaItem.title}\" Thumbs Up"
                else -> "Marked \"${mediaItem.title}\" Not For Me"
            }
            showFeedback(label)
        }
    }

    fun recordPlaybackPosition(
        mediaId: String,
        episodeId: String?,
        episodeLabel: String?,
        positionMs: Long,
        durationMs: Long
    ) {
        val profileId = _activeProfileId.value
        viewModelScope.launch {
            repository.saveWatchProgress(
                profileId = profileId,
                mediaId = mediaId,
                episodeId = episodeId,
                episodeLabel = episodeLabel,
                positionMs = positionMs,
                durationMs = durationMs.coerceAtLeast(60_000L)
            )
        }
    }

    fun removeFromContinueWatching(mediaId: String) {
        val profileId = _activeProfileId.value
        viewModelScope.launch {
            repository.removeWatchProgress(profileId, mediaId)
            showFeedback("Removed from Continue Watching")
        }
    }

    fun startOrToggleDownload(mediaItem: MediaItemEntity, episode: EpisodeEntity? = null) {
        val profileId = _activeProfileId.value
        val downloadId = if (episode != null) "dl_${episode.id}" else "dl_${mediaItem.id}"
        val existing = downloadsList.value.find { it.id == downloadId }

        if (existing != null && existing.status == "COMPLETED") {
            showFeedback("\"${mediaItem.title}\" is already downloaded")
            return
        }

        if (existing != null && existing.status == "DOWNLOADING") {
            activeDownloadJobs[downloadId]?.cancel()
            viewModelScope.launch {
                repository.upsertDownload(existing.copy(status = "PAUSED"))
                showFeedback("Download paused")
            }
            return
        }

        val quality = _settingsState.value.downloadQuality
        val sizeMb = if (episode != null) 580 else if (quality.contains("1080p")) 1760 else 920
        val subtitle = if (episode != null) {
            "S${episode.seasonNumber}:E${episode.episodeNumber} \"${episode.title}\" • ${episode.durationMinutes}m • $sizeMb MB"
        } else {
            "${mediaItem.durationOrSeasons} • ${sizeMb} MB • $quality"
        }

        val startPercent = existing?.progressPercent?.coerceIn(5, 95) ?: 8
        val initialEntity = DownloadEntity(
            id = downloadId,
            profileId = profileId,
            mediaId = mediaItem.id,
            episodeId = episode?.id,
            title = mediaItem.title,
            subtitle = subtitle,
            posterUrl = mediaItem.posterUrl,
            ambientColorHex = mediaItem.ambientColorHex,
            status = "DOWNLOADING",
            progressPercent = startPercent,
            sizeMb = sizeMb,
            quality = quality,
            createdAt = System.currentTimeMillis()
        )

        activeDownloadJobs[downloadId]?.cancel()
        activeDownloadJobs[downloadId] = viewModelScope.launch {
            repository.upsertDownload(initialEntity)
            showFeedback("Downloading \"${mediaItem.title}\"...")
            var progress = startPercent
            while (progress < 100) {
                delay(320L)
                progress = (progress + 16).coerceAtMost(100)
                val status = if (progress >= 100) "COMPLETED" else "DOWNLOADING"
                repository.upsertDownload(
                    initialEntity.copy(
                        status = status,
                        progressPercent = progress
                    )
                )
            }
            showFeedback("Download complete: \"${mediaItem.title}\"")
        }
    }

    fun triggerSimulatedFailedDownload(mediaItem: MediaItemEntity) {
        val profileId = _activeProfileId.value
        val downloadId = "dl_${mediaItem.id}"
        activeDownloadJobs[downloadId]?.cancel()
        viewModelScope.launch {
            repository.upsertDownload(
                DownloadEntity(
                    id = downloadId,
                    profileId = profileId,
                    mediaId = mediaItem.id,
                    episodeId = null,
                    title = mediaItem.title,
                    subtitle = "Network interrupted • Tap Retry to resume",
                    posterUrl = mediaItem.posterUrl,
                    ambientColorHex = mediaItem.ambientColorHex,
                    status = "ERROR",
                    progressPercent = 42,
                    sizeMb = 1420,
                    quality = _settingsState.value.downloadQuality,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteDownload(downloadId: String) {
        activeDownloadJobs[downloadId]?.cancel()
        viewModelScope.launch {
            repository.deleteDownload(downloadId)
            showFeedback("Download removed from device")
        }
    }

    fun clearAllDownloads() {
        val profileId = _activeProfileId.value
        activeDownloadJobs.values.forEach { it.cancel() }
        activeDownloadJobs.clear()
        viewModelScope.launch {
            repository.clearDownloads(profileId)
            showFeedback("All downloads removed")
        }
    }

    // Notifications Actions
    fun onNotificationClicked(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.markNotificationRead(notification.id)
        }
        openDetails(notification.mediaId)
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            showFeedback("All notifications marked as read")
        }
    }

    // Profile CRUD Actions
    fun saveProfile(
        id: String?,
        name: String,
        avatarColorHex: String,
        avatarAccentHex: String,
        isKids: Boolean,
        maturityRestriction: String,
        pinCode: String?
    ) {
        val cleanName = name.trim().ifEmpty { "Viewer" }
        val initials = cleanName.take(2).uppercase()
        val resolvedId = id ?: "profile_${System.currentTimeMillis()}"
        val profile = ProfileEntity(
            id = resolvedId,
            name = cleanName,
            avatarColorHex = avatarColorHex,
            avatarAccentHex = avatarAccentHex,
            avatarInitials = initials,
            isKids = isKids,
            maturityRestriction = if (isKids) "Kids (TV-Y7)" else maturityRestriction,
            pinCode = pinCode?.takeIf { it.length == 4 }
        )
        viewModelScope.launch {
            repository.upsertProfile(profile)
            showFeedback("Saved profile \"${profile.name}\"")
        }
    }

    fun deleteProfile(profileId: String) {
        if (profiles.value.size <= 1) {
            showFeedback("At least one profile must remain active")
            return
        }
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            if (_activeProfileId.value == profileId) {
                val remaining = profiles.value.firstOrNull { it.id != profileId }
                if (remaining != null) {
                    _activeProfileId.value = remaining.id
                }
            }
            showFeedback("Profile deleted")
        }
    }

    // Settings Mutations
    fun updateSettings(transform: (AppSettingsUiState) -> AppSettingsUiState) {
        _settingsState.update(transform)
    }

    fun updateCustomStreamServerEndpoint(newUrl: String) {
        repository.updateCustomApiEndpoint(newUrl)
        _settingsState.update {
            it.copy(
                customStreamApiUrl = newUrl.trim(),
                apiStatusMessage = "Custom API Endpoint Saved • 4 Server Nodes Ready"
            )
        }
        showFeedback("Streaming Server API endpoint updated")
    }
}
