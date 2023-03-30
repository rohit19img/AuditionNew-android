package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.img.audition.databinding.ActivityPreviewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager

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


            viewBinding.backPressIC.setOnClickListener {
                onBackPressed()
            }

            viewBinding.sendToUploadBtn.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(ConstValFile.VideoFilePath,videoUri)
                sendToUploadVideoActivity(bundle)
            }
        }else{
            myApplication.printLogD("File Path Null",TAG)
        }


    }

    private fun sendToUploadVideoActivity(bundle: Bundle) {
        val intent = Intent(this@PreviewActivity,UploadVideoActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }


}