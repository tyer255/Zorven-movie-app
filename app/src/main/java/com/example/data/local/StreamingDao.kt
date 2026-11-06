package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamingDao {
    // Media Catalog
    @Query("SELECT * FROM media_items")
    fun observeAllMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(items: List<MediaItemEntity>)

    @Query("SELECT * FROM media_items WHERE id = :mediaId LIMIT 1")
    suspend fun getMediaById(mediaId: String): MediaItemEntity?

    // Episodes
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun observeEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    suspend fun getEpisodesForSeriesOnce(seriesId: String): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEpisodes(episodes: List<EpisodeEntity>)

    // Clips
    @Query("SELECT * FROM clips")
    fun observeAllClips(): Flow<List<ClipItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllClips(clips: List<ClipItemEntity>)

    // Profiles
    @Query("SELECT * FROM profiles ORDER BY isKids ASC, name ASC")
    fun observeProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY isKids ASC, name ASC")
    suspend fun getProfilesOnce(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProfiles(profiles: List<ProfileEntity>)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: String)

    // My List
    @Query("SELECT * FROM my_list WHERE profileId = :profileId ORDER BY addedTimestamp DESC")
    fun observeMyList(profileId: String): Flow<List<MyListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToMyList(entry: MyListEntity)

    @Query("DELETE FROM my_list WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun removeFromMyList(profileId: String, mediaId: String)

    // Watch Progress
    @Query("SELECT * FROM watch_progress WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun observeWatchProgress(profileId: String): Flow<List<WatchProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchProgress(progress: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE profileId = :profileId AND mediaId = :mediaId")
    suspend fun removeWatchProgress(profileId: String, mediaId: String)

    // Downloads
    @Query("SELECT * FROM downloads WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun observeDownloads(profileId: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(download: DownloadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDownloads(downloads: List<DownloadEntity>)

    @Query("DELETE FROM downloads WHERE id = :downloadId")
    suspend fun deleteDownloadById(downloadId: String)

    @Query("DELETE FROM downloads WHERE profileId = :profileId")
    suspend fun clearDownloadsForProfile(profileId: String)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY isRead ASC, id ASC")
    fun observeNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // User Ratings
    @Query("SELECT * FROM user_ratings WHERE profileId = :profileId")
    fun observeUserRatings(profileId: String): Flow<List<UserRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserRating(rating: UserRatingEntity)
}
