package com.img.audition.videoWork

import android.app.Application
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.img.audition.globalAccess.ConstValFile
import io.socket.client.IO
import io.socket.client.Socket
import java.io.File
import java.net.URISyntaxException
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.lang.RuntimeException


@UnstableApi
class VideoCacheWork : Application() {
    companion object{
        lateinit var simpleCache: SimpleCache
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var standaloneDatabaseProvider: StandaloneDatabaseProvider
        private const val exoCacheSize: Long = 30 * 1024 * 1024 // Setting cache size to be ~ 30 MB
        var mSocket: Socket? = null
    }


    override fun onCreate() {
        super.onCreate()

        try {
            mSocket = IO.socket(ConstValFile.SOCKET_URL)
        } catch (e : URISyntaxException) {
            throw RuntimeException(e)
        }

        mSocket!!.connect()

        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoCacheSize)
        standaloneDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(File(this.cacheDir, "videoCache"), leastRecentlyUsedCacheEvictor, standaloneDatabaseProvider)



    }

}