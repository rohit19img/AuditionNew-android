package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.img.audition.databinding.ActivityHomeBinding
import com.img.audition.databinding.ActivityPreviewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoCacheWork

@UnstableApi class PreviewActivity : AppCompatActivity() {

    val TAG = "PreviewActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPreviewBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@PreviewActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@PreviewActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val player = ExoPlayer.Builder(this@PreviewActivity).build()
        viewBinding.videoExoView.player = player

        val intent = intent.getBundleExtra(ConstValFile.Bundle)
        if (intent!=null){

            val videoUri = intent.getString(ConstValFile.VideoFilePath).toString()
            val mediaItem = MediaItem.fromUri(videoUri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }else{
            myApplication.printLogD("File Path Null",TAG)
        }


        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.sendToUploadBtn.setOnClickListener {

        }
    }
}