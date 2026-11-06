package com.example

import com.example.data.local.CatalogSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun verifyCatalogAndNoFakeUserActivitySeeds() {
        val mediaItems = CatalogSeedData.initialMediaItems()
        val episodes = CatalogSeedData.initialEpisodes()
        val profiles = CatalogSeedData.initialProfiles()
        val clips = CatalogSeedData.initialClips()

        assertTrue(mediaItems.size >= 14)
        assertTrue(episodes.size >= 30)
        assertEquals(3, profiles.size)
        assertTrue(clips.isNotEmpty())
        assertTrue("No fake watch progress should be seeded", CatalogSeedData.initialWatchProgress().isEmpty())
        assertTrue("No fake My List items should be seeded", CatalogSeedData.initialMyList().isEmpty())
        assertTrue("No fake downloads should be seeded", CatalogSeedData.initialDownloads().isEmpty())
    }
}
