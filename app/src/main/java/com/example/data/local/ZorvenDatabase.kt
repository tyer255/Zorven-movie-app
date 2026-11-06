package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MediaItemEntity::class,
        EpisodeEntity::class,
        ClipItemEntity::class,
        ProfileEntity::class,
        MyListEntity::class,
        WatchProgressEntity::class,
        DownloadEntity::class,
        NotificationEntity::class,
        UserRatingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ZorvenDatabase : RoomDatabase() {
    abstract fun streamingDao(): StreamingDao

    companion object {
        @Volatile
        private var INSTANCE: ZorvenDatabase? = null

        fun getInstance(context: Context): ZorvenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZorvenDatabase::class.java,
                    "zorven_streaming.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
