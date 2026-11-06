package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CatalogSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name and catalog seed integrity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Zorven", appName)

        val mediaItems = CatalogSeedData.initialMediaItems()
        val episodes = CatalogSeedData.initialEpisodes()
        val profiles = CatalogSeedData.initialProfiles()
        val clips = CatalogSeedData.initialClips()

        assertTrue("Catalog should have movies and series", mediaItems.size >= 12)
        assertTrue("Multi-season episodes should be seeded", episodes.size >= 30)
        assertTrue("Profiles should include adult and kids profiles", profiles.size >= 4)
        assertTrue("Vertical clips should be seeded", clips.isNotEmpty())
    }
}
