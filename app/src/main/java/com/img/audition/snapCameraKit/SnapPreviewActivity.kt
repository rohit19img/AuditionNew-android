package com.img.audition.snapCameraKit

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import androidx.media3.ui.PlayerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.img.audition.R
import com.img.audition.databinding.ActivitySnapPreviewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.CompilerActivity
import com.img.audition.screens.HomeActivity
import com.img.audition.screens.MusicActivity
import com.img.audition.screens.UploadVideoActivity
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

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

    companion object {

        private const val BUNDLE_ARG_PLAYER_WINDOW_INDEX = "player_window_index"
        private const val BUNDLE_ARG_PLAYER_POSITION = "camera_facing_front"
        private const val BUNDLE_ARG_EXPORTED_MEDIA_URI = "exported_media_uri"

        @JvmStatic
        fun startUsing(
            activity: Activity,
            sharedTransitionView: View,
            file: File,
            mimeType: String
        ) {
            activity.runOnUiThread {
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    activity, sharedTransitionView, activity.getString(R.string.transition_shared_dummy)
                )
                activity.application.startActivity(
                    Intent(activity, SnapPreviewActivity::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(file), mimeType)
                        // Using this flag + application to launch new activity to avoid a bug (Android?)
                        // where multiple, rapid startActivity calls mess up the calling Activity (MainActivity)
                        // enter transition (it never finishes) when navigating back from this PreviewActivity.
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    options.toBundle()
                )
            }
        }
    }

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()
    private val mediaExportTask = AtomicReference<Future<*>>()

    private lateinit var videoPreview: PlayerView
    private lateinit var imagePreview: ImageView
    private lateinit var mediaUri: Uri
    private lateinit var mediaFile: File
    private lateinit var mediaMimeType: String

    private var player: ExoPlayer? = null
    private var playerWindowIndex: Int = 0
    private var playerPosition: Long = 0L
    private var exportedMediaUri: Uri? = null

    lateinit var videoPlayer : ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        videoPlayer = ExoPlayer.Builder(this@SnapPreviewActivity).build()


        viewBinding.exitButton.setOnClickListener {
            onBackPressed()
        }
        /* val type = intent.type
         if (intent.action == Intent.ACTION_VIEW && type != null) {
             intent.data?.let {
                 mediaUri = it
                 mediaFile = File(it.path!!)
                 mediaMimeType = type

                 Log.d("check 200", "onCreate: $it")
                 savedInstanceState?.let { state ->
                     playerWindowIndex = state.getInt(BUNDLE_ARG_PLAYER_WINDOW_INDEX, 0)
                     playerPosition = state.getLong(BUNDLE_ARG_PLAYER_POSITION, 0L)
                     exportedMediaUri = state.getString(BUNDLE_ARG_EXPORTED_MEDIA_URI)?.let(Uri::parse)
                 }

                 postponeEnterTransition()
                 setContentView(R.layout.activity_snap_preview)

                 videoPreview = findViewById(R.id.video_preview)
                 imagePreview = findViewById(R.id.image_preview)

                 findViewById<View>(R.id.exit_button).setOnClickListener {
                     onBackPressed()
                 }
                 findViewById<View>(R.id.export_button).setOnClickListener { view ->
                     view.isEnabled = false
                     mediaExportTask.getAndSet(
                         singleThreadExecutor.submit {
                             exportedMediaUri = if (exportedMediaUri != null) {
                                 exportedMediaUri
                             } else {
                                 generateContentUri(mediaFile)
                             }?.also { uri ->
                                 shareExternally(uri, mediaMimeType)
                             }
                             view.post {
                                 view.isEnabled = true
                             }
                         }
                     )?.cancel(true)
                 }
             } ?: finish()
         } else {
             finish()
         }*/


   }

   /* private fun setupMediaIfNeeded() {
        if (player == null && mediaMimeType == MIME_TYPE_VIDEO_MP4) {
            val exoPlayer = ExoPlayer.Builder(this)
                .build()

            videoPreview.player = exoPlayer

            val dataSourceFactory = DefaultDataSourceFactory(this, "camera-kit-sample")
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mediaUri))
            val mainHandler = Handler(Looper.getMainLooper())
            mediaSource.addEventListener(
                mainHandler,
                 object : MediaSourceEventListener {

                    override fun onLoadCompleted(
                        windowIndex: Int,
                        mediaPeriodId: MediaSource.MediaPeriodId?,
                        loadEventInfo: LoadEventInfo,
                        mediaLoadData: MediaLoadData
                    ) {
                        mediaSource.removeEventListener(this)
                        startPostponedEnterTransition()
                    }

                    override fun onLoadError(
                        windowIndex: Int,
                        mediaPeriodId: MediaSource.MediaPeriodId?,
                        loadEventInfo: LoadEventInfo,
                        mediaLoadData: MediaLoadData,
                        error: IOException,
                        wasCanceled: Boolean
                    ) {
                        mediaSource.removeEventListener(this)
                        finish()
                    }
                }
            )

            exoPlayer.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    videoPreview.setBackgroundColor(Color.BLACK)
                }
            })

            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            exoPlayer.playWhenReady = true
            exoPlayer.seekTo(playerWindowIndex, playerPosition)
            exoPlayer.prepare(mediaSource, false, false)

            player = exoPlayer
        } else if (mediaMimeType == MIME_TYPE_IMAGE_JPEG) {
            imagePreview.post {
                Glide.with(this)
                    .load(mediaUri)
                    .listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            finish()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            startPostponedEnterTransition()
                            imagePreview.setBackgroundColor(Color.BLACK)
                            return false
                        }
                    })
                    .run {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            fitCenter()
                        } else {
                            centerCrop()
                        }
                    }
                    .into(imagePreview)
            }
        }
    }

    private fun releaseMediaIfNeeded() {
        player?.let {
            playerWindowIndex = it.currentWindowIndex
            playerPosition = it.currentPosition
            it.release()

            videoPreview.player = null
            player = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        exportedMediaUri?.let {
            outState.putString(BUNDLE_ARG_EXPORTED_MEDIA_URI, it.toString())
        }
        outState.putInt(BUNDLE_ARG_PLAYER_WINDOW_INDEX, playerWindowIndex)
        outState.putLong(BUNDLE_ARG_PLAYER_POSITION, playerPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setupMediaIfNeeded()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            setupMediaIfNeeded()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releaseMediaIfNeeded()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            releaseMediaIfNeeded()
        }
    }

    override fun onBackPressed() {
        // Treating back press as user intent to cancel therefore temporary media file is deleted.
        singleThreadExecutor.execute {
            mediaFile.delete()
        }
        super.onBackPressed()
        // Disable slide-out exit animation to match seamless enter transition
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        singleThreadExecutor.shutdown()
        super.onDestroy()
    }*/

    private fun sendToMusicActivity() {
        val intent = Intent(this@SnapPreviewActivity, MusicActivity::class.java)
        startActivity(intent)
    }

    private fun sendToCompilerActivity(bundle: Bundle) {
        val intent = Intent(this@SnapPreviewActivity, CompilerActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }


    override fun onPause() {
        videoPlayer.pause()
        videoPlayer.stop()
        super.onPause()
    }

    override fun onStart() {
        super.onStart()

    }


    override fun onResume() {

        viewBinding.videoPreview.player = videoPlayer
        val videoUri = sessionManager.getCreateVideoPath()

        if (sessionManager.getIsFromTryAudio()){
            viewBinding.music.visibility = View.GONE
        }else{
            viewBinding.music.visibility = View.VISIBLE
        }

        Log.d("video url", "onResume: $videoUri")
        val videoSpeedState = sessionManager.getCreateVideoSpeedState()
        val videoDuration = sessionManager.getCreateVideoDuration()
//        myApplication.printLogD("videoUri : $videoUri videoState: $videoSpeedState videoDuration: $videoDuration",TAG)
        val isFromContest = sessionManager.getIsFromContest()
        myApplication.printLogD("${sessionManager.getContestEntryFee()} SnapPReview1", "contestCheck")
        myApplication.printLogD("${sessionManager.getIsFromContest()} SnapPReview2", " isFromContest")
        val mediaItem = MediaItem.fromUri(videoUri!!)
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

        when(sessionManager.getCreateVideoSpeedState()!!){
            ConstValFile.SlowVideo ->{
                viewBinding.normalVideo.visibility = View.GONE
                viewBinding.fastVideo.visibility = View.GONE
                viewBinding.slowVideo.visibility = View.GONE
            }
            ConstValFile.FastVideo ->{
                viewBinding.normalVideo.visibility = View.GONE
                viewBinding.fastVideo.visibility = View.GONE
                viewBinding.slowVideo.visibility = View.GONE
            }
            else ->{
                viewBinding.normalVideo.visibility = View.GONE
                viewBinding.fastVideo.visibility = View.GONE
                viewBinding.slowVideo.visibility = View.GONE
            }
        }

        viewBinding.fastVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.FastVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.FastVideo)
            sendToCompilerActivity(bundle)
        }



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
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        try {
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
}