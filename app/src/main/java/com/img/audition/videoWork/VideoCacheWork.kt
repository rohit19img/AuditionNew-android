package com.img.audition.videoWork

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.img.audition.Constants
import io.socket.client.IO
import io.socket.client.Socket
import java.io.File
import java.net.URISyntaxException

@UnstableApi class VideoCacheWork : Application() {
    companion object{
        lateinit var simpleCache: SimpleCache
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var standaloneDatabaseProvider: StandaloneDatabaseProvider
        private const val exoCacheSize: Long = 100 * 1024 * 1024 // Setting cache size to be ~ 100 MB
        public var mSocket: Socket? = null
    }


    override fun onCreate() {
        super.onCreate()

        try {
            mSocket = IO.socket(Constants.SOCKET_URL)
        } catch (e : URISyntaxException) {
            throw java.lang.RuntimeException(e)
        }

        mSocket!!.connect()

        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoCacheSize)
        standaloneDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(File(this.cacheDir, "videoCache"), leastRecentlyUsedCacheEvictor, standaloneDatabaseProvider)
    }
}