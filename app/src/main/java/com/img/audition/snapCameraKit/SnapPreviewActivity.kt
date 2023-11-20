package com.img.audition.snapCameraKit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.img.audition.R
import com.img.audition.databinding.ActivitySnapPreviewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.CompilerActivity
import com.img.audition.screens.HomeActivity
import com.img.audition.screens.MusicActivity
import com.img.audition.screens.VideoTrimActivity
import java.io.File

@UnstableApi
class SnapPreviewActivity : AppCompatActivity() {



    private val sessionManager by lazy {
        SessionManager(this@SnapPreviewActivity)
    }

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySnapPreviewBinding.inflate(layoutInflater)
    }

    val TAG = "SnapPreviewActivity"
    private val myApplication by lazy {
        MyApplication(this@SnapPreviewActivity)
    }

    lateinit var videoPlayer : ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)


        viewBinding.exitButton.setOnClickListener { onBackPressed() }

        viewBinding.slowVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.SlowVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask, ConstValFile.SlowVideo)
            sendToCompilerActivity(bundle)
        }

        viewBinding.normalVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.NormalVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.NormalVideo)
            sendToCompilerActivity(bundle)
        }

        viewBinding.videoSpeed.setOnClickListener {
            if (viewBinding.videoSpeedState.visibility == View.GONE){
                viewBinding.videoSpeedState.visibility  = View.VISIBLE
            }else{
                viewBinding.videoSpeedState.visibility  = View.GONE
            }
        }




        viewBinding.fastVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.FastVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.FastVideo)
            sendToCompilerActivity(bundle)
        }

        viewBinding.trimVideoBtn.setOnClickListener {
            videoPlayer.stop()
            videoPlayer.release()
            val intent = Intent(this,VideoTrimActivity::class.java)
            startActivity(intent)
        }

   }




    override fun onResume() {

        if (sessionManager.getIsFromTryAudio()){
            viewBinding.music.visibility = View.GONE
        }else{
            viewBinding.music.visibility = View.VISIBLE
        }
        videoPlayer = ExoPlayer.Builder(this@SnapPreviewActivity).build()

        viewBinding.videoPreview.player = videoPlayer
        val videoUri = sessionManager.getCreateVideoPath()

        Log.d("video url", "onResume: $videoUri")
        val mediaItem = MediaItem.Builder().setMimeType("video/*").setUri(videoUri!!).build()
        videoPlayer.setMediaItem(mediaItem)
        videoPlayer.prepare()
        videoPlayer.play()

        videoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_ENDED -> {
                        videoPlayer.seekTo(0)
                        videoPlayer.prepare()
                        videoPlayer.play()
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        Log.d("check 200", "STATE_BUFFERING: ")
                    }
                    ExoPlayer.STATE_READY -> {
                        Log.d("check 200", "STATE_READY: ")

                    }
                    else -> {
                        Log.d("check 200", "currentState: $playbackState")
                    }
                }
            }
        })


        viewBinding.sendToUploadBtn.setOnClickListener {
            videoPlayer.pause()
            videoPlayer.stop()
            sendToCompressAndUploadVideoActivity()
        }

        viewBinding.music.setOnClickListener {
            videoPlayer.volume = 0F
            sendToMusicActivity()
        }

        viewBinding.x5Speed.setOnClickListener {
            viewBinding.x5Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorYellow))
            viewBinding.x1Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorWhite))
            viewBinding.x2Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorWhite))
            sessionManager.setCreateVideoSpeedState(ConstValFile.SlowVideo)
            videoPlayer.setPlaybackSpeed(0.5f)
        }

        viewBinding.x1Speed.setOnClickListener {
            viewBinding.x1Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorYellow))
            viewBinding.x5Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorWhite))
            viewBinding.x2Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorWhite))
            sessionManager.setCreateVideoSpeedState(ConstValFile.NormalVideo)
            videoPlayer.setPlaybackSpeed(1f)
        }

        viewBinding.x2Speed.setOnClickListener {
            viewBinding.x2Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorYellow))
            viewBinding.x5Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorWhite))
            viewBinding.x1Speed.setTextColor(ContextCompat.getColor(this,R.color.textColorWhite))
            sessionManager.setCreateVideoSpeedState(ConstValFile.FastVideo)
            videoPlayer.setPlaybackSpeed(2f)
        }

        super.onResume()
    }

    private fun sendToCompressAndUploadVideoActivity() {
        val bundle = Bundle()
        bundle.putString(ConstValFile.CompileTask,ConstValFile.CompressVideo)
        val intent = Intent(this@SnapPreviewActivity,CompilerActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val sweetAlertDialog = SweetAlertDialog(this@SnapPreviewActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Discard Video"
        sweetAlertDialog.contentText = "Do you want discard the video"
        sweetAlertDialog.confirmText = "Yes"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            if (sessionManager.getIsVideoFromGallery()){
                 sessionManager.clearVideoSession()
                sendToMain()
            }else{
                if (File(sessionManager.getCreateVideoPath()!!).exists()){
                    File(sessionManager.getCreateVideoPath()!!).delete()
                    sessionManager.clearVideoSession()
                }
                sendToMain()
            }
            videoPlayer.stop()
            videoPlayer.release()
        }
        sweetAlertDialog.cancelText = "No"
        sweetAlertDialog.setCancelClickListener {
            sweetAlertDialog.dismiss()
        }
        sweetAlertDialog.show()
    }

    private fun sendToMain() {
        val intent = Intent(this@SnapPreviewActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        try {
            videoPlayer.release()
            if(File(sessionManager.getCreateVideoPath()!!).exists()){
                File(sessionManager.getCreateVideoPath()!!).delete()
            }
            if (File(sessionManager.getTrimAudioPath()!!).exists()){
                File(sessionManager.getTrimAudioPath()!!).delete()
            }
        }catch (e:Exception){
            myApplication.printLogE(e.toString(),TAG)
        }
        super.onDestroy()
    }

    override fun onStop() {
        videoPlayer.stop()
        videoPlayer.release()

        super.onStop()
    }


    private fun sendToMusicActivity() {
        val intent = Intent(this@SnapPreviewActivity, MusicActivity::class.java)
        sessionManager.setIsAppAudio(false)
        startActivity(intent)
    }

    private fun sendToCompilerActivity(bundle: Bundle) {
        val intent = Intent(this@SnapPreviewActivity, CompilerActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    /*
     val videoUri = sessionManager.getCreateVideoPath()

        val uri = Uri.parse(videoUri)
        viewBinding.videoView.setVideoURI(uri);
        val mediaController = MediaController(this)
        mediaController.setAnchorView(viewBinding.videoView);

        // sets the media player to the videoView
        mediaController.setMediaPlayer(viewBinding.videoView);

        // sets the media controller to the videoView
        viewBinding.videoView.setMediaController(mediaController);

        // starts the video
        viewBinding.videoView.start();
     */

    override fun onPause() {
        videoPlayer.pause()
        videoPlayer.stop()
        super.onPause()
    }

}