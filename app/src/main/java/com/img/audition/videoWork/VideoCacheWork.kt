package com.img.audition.videoWork

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi class VideoCacheWork : Application() {
    companion object{
        lateinit var simpleCache: SimpleCache
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var standaloneDatabaseProvider: StandaloneDatabaseProvider
        private const val exoCacheSize: Long = 100 * 1024 * 1024 // Setting cache size to be ~ 100 MB
    }

    override fun onCreate() {
        super.onCreate()
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoCacheSize)
        standaloneDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(File(this.cacheDir, "videoCache"), leastRecentlyUsedCacheEvictor, standaloneDatabaseProvider)
    }
}