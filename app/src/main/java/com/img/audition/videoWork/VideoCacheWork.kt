package com.img.audition.videoWork

import android.app.Application
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor

import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.img.audition.globalAccess.ConstValFile
import io.socket.client.IO
import io.socket.client.Socket
import java.io.File
import java.net.URISyntaxException

class VideoCacheWork : Application(), CameraXConfig.Provider {
    companion object{
        lateinit var simpleCache: SimpleCache
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var standaloneDatabaseProvider: StandaloneDatabaseProvider
        private const val exoCacheSize: Long = 20 * 1024 * 1024 // Setting cache size to be ~ 20 MB
        public var mSocket: Socket? = null
    }



    override fun onCreate() {
        super.onCreate()

        try {
            mSocket = IO.socket(ConstValFile.SOCKET_URL)
        } catch (e : URISyntaxException) {
            throw java.lang.RuntimeException(e)
        }

        mSocket!!.connect()

        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoCacheSize)
        standaloneDatabaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(File(this.cacheDir, "videoCache"), leastRecentlyUsedCacheEvictor, standaloneDatabaseProvider)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }


}