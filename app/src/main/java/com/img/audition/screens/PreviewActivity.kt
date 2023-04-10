package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.img.audition.databinding.ActivityPreviewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager

@UnstableApi
class PreviewActivity : AppCompatActivity() {

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

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    lateinit var player :ExoPlayer

    private var isFromContest = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        player  = ExoPlayer.Builder(this@PreviewActivity).build()
        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

    }

    private fun sendToUploadVideoActivity() {
        val intent = Intent(this@PreviewActivity, UploadVideoActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()


        viewBinding.videoExoView.player = player

        val videoUri = sessionManager.getCreateVideoSession()
        isFromContest = sessionManager.getIsFromContest()
        myApplication.printLogD("$isFromContest onCreate", " isFromContest $TAG")
        val mediaItem = MediaItem.fromUri(videoUri!!)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()


        player.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_ENDED -> {
                        player.seekTo(0)
                        player.prepare()
                        player.play()
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        myApplication.printLogD("STATE_BUFFERING", TAG)
                    }
                    ExoPlayer.STATE_READY -> {
                        myApplication.printLogD("STATE_READY", TAG)
                    }
                    else -> {
                        myApplication.printLogD(playbackState.toString(), "currentState")
                    }
                }
            }
        })

        viewBinding.sendToUploadBtn.setOnClickListener {
            player.pause()
            player.stop()
            player.release()
            sendToUploadVideoActivity()
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
        player.stop()
        player.release()
    }



}