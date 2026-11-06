package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitleTagline: String,
    val type: String, // "MOVIE" or "SERIES"
    val releaseYear: Int,
    val maturityRating: String, // "TV-MA", "R", "PG-13", "TV-14", "PG", "TV-Y7"
    val durationOrSeasons: String, // e.g., "2h 18m" or "3 Seasons"
    val seasonCount: Int, // 0 for movies, >=1 for series
    val qualityBadge: String, // "4K HDR", "Dolby Vision", "HD"
    val audioBadge: String, // "Spatial Audio", "Dolby Atmos", "5.1 Surround"
    val matchScore: Int, // e.g., 98
    val top10Rank: Int?, // 1..10 or null
    val synopsis: String,
    val genresCsv: String, // e.g., "Sci-Fi,Thriller,Action"
    val castCsv: String,
    val creatorsCsv: String,
    val posterUrl: String,
    val backdropUrl: String,
    val localBackdropResName: String?, // Optional local generated drawable resource name
    val ambientColorHex: String, // Dominant cinema ambient color
    val secondaryAmbientHex: String, // Secondary gradient color
    val defaultStreamUrl: String,
    val apiEndpointSlug: String, // Used by custom Streaming Server API e.g. "/streams/solaris-protocol"
    val isTrending: Boolean,
    val isPopular: Boolean,
    val isNewRelease: Boolean,
    val isRecommended: Boolean
) {
    val genres: List<String>
        get() = genresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val castList: List<String>
        get() = castCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val creatorsList: List<String>
        get() = creatorsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val synopsis: String,
    val durationMinutes: Int,
    val thumbnailUrl: String,
    val streamUrl: String,
    val apiEndpointSlug: String
)

@Entity(tableName = "clips")
data class ClipItemEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val clipTitle: String,
    val movieTitle: String,
    val caption: String,
    val tagsCsv: String,
    val durationSeconds: Int,
    val videoUrl: String,
    val posterUrl: String,
    val ambientColorHex: String,
    val likesCount: String
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarColorHex: String,
    val avatarAccentHex: String,
    val avatarInitials: String,
    val isKids: Boolean,
    val maturityRestriction: String, // "All Maturity Ratings", "PG-13 & Below", "Kids (TV-Y7)"
    val pinCode: String?, // null or 4-digit PIN
    val autoplayNextEpisode: Boolean = true,
    val autoplayPreviews: Boolean = true
)

@Entity(tableName = "my_list", primaryKeys = ["profileId", "mediaId"])
data class MyListEntity(
    val profileId: String,
    val mediaId: String,
    val addedTimestamp: Long
)

@Entity(tableName = "watch_progress", primaryKeys = ["profileId", "mediaId"])
data class WatchProgressEntity(
    val profileId: String,
    val mediaId: String,
    val episodeId: String?,
    val episodeLabel: String?, // e.g., "S1:E3 \"The Silent Orbit\""
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0.02f, 0.98f) else 0f

    val remainingMinutesText: String
        get() {
            val remainingMin = ((durationMs - positionMs).coerceAtLeast(0L) / 60_000L).toInt()
            return if (remainingMin > 0) "${remainingMin}m left" else "1m left"
        }
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val mediaId: String,
    val episodeId: String?,
    val title: String,
    val subtitle: String, // e.g., "1h 58m • 1.4 GB" or "S1:E1 • 52m • 640 MB"
    val posterUrl: String,
    val ambientColorHex: String,
    val status: String, // "COMPLETED", "DOWNLOADING", "PAUSED", "ERROR"
    val progressPercent: Int, // 0..100
    val sizeMb: Int,
    val quality: String, // "Standard (720p)" or "High (1080p)"
    val createdAt: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val categoryTag: String, // "NEW ARRIVAL", "TOP 10 TODAY", "NEW SEASON", "COMING SOON"
    val title: String,
    val message: String,
    val dateLabel: String,
    val backdropUrl: String,
    val ambientColorHex: String,
    val isRead: Boolean
)

@Entity(tableName = "user_ratings", primaryKeys = ["profileId", "mediaId"])
data class UserRatingEntity(
    val profileId: String,
    val mediaId: String,
    val rating: Int // 1 = Thumbs Up, 2 = Double Thumbs Up, 0 = Not For Me
)
