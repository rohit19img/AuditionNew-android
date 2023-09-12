package com.img.audition.snapCameraKit

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.img.audition.R
import com.img.audition.customView.LineProgressView
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.SessionManager
import com.img.audition.screens.CompilerActivity
import com.img.audition.screens.MusicActivity
import com.img.audition.screens.UserUploadedVideoActivity
import com.img.audition.videoWork.VideoCacheWork
import com.snap.camerakit.ImageProcessor
import com.snap.camerakit.Session
import com.snap.camerakit.lenses.LENS_GROUP_ID_BUNDLED
import com.snap.camerakit.outputFrom
import com.snap.camerakit.support.widget.CameraLayout
import com.snap.camerakit.support.widget.SnapButtonView
import com.snap.camerakit.versionFrom
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "SnapCameraActivity"
private val REQUEST_TAKE_GALLERY_VIDEO = 200
private val LENS_GROUPS = arrayOf(
    LENS_GROUP_ID_BUNDLED, // lens group for bundled lenses available in lenses-bundle artifact. , // lens group for lenses obtained using Push to Device functionality.
    *BuildConfig.LENS_GROUP_ID_TEST.split(',').toTypedArray() // temporary lens group for testing
)

@UnstableApi
class
SnapCameraActivity : AppCompatActivity(), MediaCapture.MediaCaptureCallback,
    SnapButtonView.OnCaptureRequestListener {

    private val myApplication by lazy {
        MyApplication(this@SnapCameraActivity)
    }
    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    private val sessionManager by lazy {
        SessionManager(this@SnapCameraActivity)
    }
    private val playerExo by lazy {
        ExoPlayer.Builder(this@SnapCameraActivity).build()
    }

    private var appSongUrl = ""
    private val TRACK = "Capture Track"

    private var timeRemainingInMillis: Long = 0
    private var totalTime: Long = 0
    private var isTimerRunning = false
    private var isTimerPaused = false
    private lateinit var countDownTimer: CountDownTimer
    private var isStartTime = false
    private var maxVideoDuration: Long = 25000
    private var minVideoDuration: Long = 5 * 1000
    private lateinit var mLineView: LineProgressView
    private var videoFilePath = ""
    private var isFromContest = false
    private var isFromDuet = false
    private var hashTag = ""

    //
    private lateinit var audioSource: AudioProcessorSource
    private val audioProcessorExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mediaCaptureExecutor: ExecutorService = Executors.newFixedThreadPool(2)
    private lateinit var cameraSession: Session
    private lateinit var cameraLayout: CameraLayout
    private lateinit var progressLayout: RelativeLayout
    private var recordingCloseable: Closeable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snap_camera)
        Log.d(TAG, "Using the CameraKit version: ${versionFrom(this)}")

        mLineView = findViewById(R.id.line_view)
        progressLayout = findViewById(R.id.progressLayout)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            audioSource = AudioProcessorSource(audioProcessorExecutor)
        }


        cameraLayout = findViewById<CameraLayout>(R.id.camera_layout).apply {
            // Setting custom audio processor source
            configureSession {
                audioProcessorSource(audioSource)
            }

            configureLensesCarousel {
                observedGroupIds = linkedSetOf(*LENS_GROUPS)
            }
        }

        cameraLayout.onError { error ->
            val message = when (error) {
                is CameraLayout.Failure.MissingPermissions -> getString(
                    R.string.required_permissions_not_granted, error.permissions.joinToString(", ")
                )

                is CameraLayout.Failure.DeviceNotSupported -> getString(R.string.camera_kit_unsupported)
                else -> throw error
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
        }

        cameraLayout.onSessionAvailable {
            cameraSession = it
            cameraLayout.captureButton.onCaptureRequestListener = this@SnapCameraActivity
        }

    }


    override fun onStart() {
        super.onStart()
        isFromContest = sessionManager.getIsFromContest()
        isFromDuet = bundle!!.getBoolean(ConstValFile.isFromDuet, false)
        hashTag = bundle!!.getString(ConstValFile.VideoHashTag, "")

        if (!(isFromDuet)) {
            sessionManager.clearDuetSession()
        }

        if (!(isFromContest)) {
            sessionManager.clearContestSession()
            findViewById<ImageView>(R.id.selectFromGallery).visibility = View.GONE
            myApplication.printLogD("$isFromContest onStart1", " isFromContest + $TAG")
        } else {
            findViewById<ImageView>(R.id.selectFromGallery).visibility = View.VISIBLE
            myApplication.printLogD("$isFromContest onStart2", " isFromContest + $TAG")
        }


        findViewById<ImageView>(R.id.selectFromGallery).setOnClickListener {
//            selectVideoFromDevice()
            selectVideoFromProfile()
        }

        findViewById<ImageView>(R.id.backPressIC).setOnClickListener {
            onBackPressed()
        }

    }

    private fun selectVideoFromProfile() {
        val intent = Intent(this@SnapCameraActivity, UserUploadedVideoActivity::class.java)
        startActivity(intent)
    }

    private fun selectVideoFromDevice() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            REQUEST_TAKE_GALLERY_VIDEO
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val videoUri: Uri? = data!!.data

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this@SnapCameraActivity, videoUri)
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                myApplication.printLogD("selectVideoDuration : $time", TAG)
                val selectVideoDuration = time!!.toLong()

                if (selectVideoDuration <= maxVideoDuration) {
                    sendToVideoPreview(videoUri.toString(), selectVideoDuration)
                    sessionManager.setIsVideoFromGallery(true)
                } else {
                    myApplication.showToast("Please select ${maxVideoDuration / 1000} second video")
                }
            }
        }
    }


    private fun stopTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel()
            timeRemainingInMillis = 0
            isTimerRunning = false
            isTimerPaused = false
            isStartTime = false
            totalTime = 0
//            updateCountdownTextView()
        }
    }

    private fun startTimer(remainingTime: Long) {
        isStartTime = true
        countDownTimer = object : CountDownTimer(remainingTime, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingInMillis = millisUntilFinished
                updateCountdownTextView()
                if (totalTime >= maxVideoDuration) {
                    isTimerRunning = false
                    onFinish()
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                myApplication.printLogD("Countdown finished", "check time")
            }
        }.start()

        isTimerRunning = true
    }


    private fun resumeTimer() {
        startTimer(timeRemainingInMillis)
        isTimerPaused = false
    }

    private fun updateCountdownTextView() {
        val minutes = (maxVideoDuration / 1000) / 60
        val seconds = (maxVideoDuration / 1000) % 60
        totalTime = maxVideoDuration - timeRemainingInMillis
        val elapsedMinutes = (totalTime / 1000) / 60
        val elapsedSeconds = (totalTime / 1000) % 60
        val timeElapsedFormatted =
            String.format(Locale.getDefault(), "%02d:%02d", elapsedMinutes, elapsedSeconds)
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        myApplication.printLogD("$timeElapsedFormatted / $timeLeftFormatted", "check time")
        myApplication.printLogD("Time remaining: ${minutes}:${seconds}", "check time")
        myApplication.printLogD("Time mills: $timeRemainingInMillis", "check time")
        myApplication.printLogD("Time mills: $totalTime", "check time")

        findViewById<TextView>(R.id.videoDuration).text = timeElapsedFormatted

        mLineView.setLoadingProgress(totalTime * 1.0f / maxVideoDuration)

    }

    override fun onSaved(file: File) {
        myApplication.printLogD("onSaved Call", "check 200")
        if (totalTime <= minVideoDuration) {
            recordingCloseable?.close()
            if (file.exists())
                file.delete()
            runOnUiThread {
                myApplication.showToast("Video must be ${minVideoDuration / 1000} second and more..")
                stopTimer()
            }
        } else {
            stopTimer()
            var selectVideoDuration = 0L
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this@SnapCameraActivity, Uri.fromFile(file))
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()
                myApplication.printLogD("selectVideoDuration : $time", TAG)
                selectVideoDuration = time!!.toLong()
            } catch (e: java.lang.Exception) {
                myApplication.printLogE(e.toString(), TRACK)
            }
            sendToVideoPreview(file.absolutePath, selectVideoDuration)
        }
    }

    override fun onError(e: Exception) {
        myApplication.printLogE(e.toString(), TRACK)
        finish()
    }

    private fun createFileAndFolder(): String {
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
        val appData = getExternalFilesDir(null)
        myApplication.printLogD(appData!!.absolutePath, TAG)

        val createFile = File(appData, filename)
        if (!(createFile.exists())) {
            try {
                createFile.createNewFile()
                myApplication.printLogD(createFile.absolutePath, TAG)
            } catch (i: IOException) {
                myApplication.printLogE(i.toString(), TAG)
            }
        }
        return createFile.absolutePath

    }

    private fun createAudioFile(): String {
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.aac"
        val appData = getExternalFilesDir(null)
        myApplication.printLogD(appData!!.absolutePath, TAG)

        val createFile = File(appData, filename)
        if (!(createFile.exists())) {
            try {
                createFile.createNewFile()
                myApplication.printLogD(createFile.absolutePath, TAG)
            } catch (i: IOException) {
                myApplication.printLogE(i.toString(), TAG)
            }
        }
        return createFile.absolutePath

    }

    override fun onDestroy() {
        if (videoFilePath.isNotEmpty() && File(videoFilePath).exists()) {
            File(videoFilePath).delete()
        }
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (cameraLayout.dispatchKeyEvent(event)) {
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        cameraLayout.captureButton.progressDuration = maxVideoDuration

        progressLayout.visibility = View.GONE
        val audioExoPlayer = findViewById<PlayerView>(R.id.audioPlayerView)
        val durationHint = findViewById<TextView>(R.id.durationHint)
        if (sessionManager.getIsFromTryAudio()) {
            durationHint.visibility = View.VISIBLE
        } else {
            durationHint.visibility = View.GONE
        }

        if (sessionManager.getIsAppAudio()) {
            val filePath = bundle!!.getString(ConstValFile.AppAudio).toString()
            val file = File(filePath)
            appSongUrl = file.toURI().toString()
            myApplication.printLogD(appSongUrl!!, "AppAudio")
            val mediaItem = MediaItem.Builder().setUri(appSongUrl).build()
            audioExoPlayer.player = playerExo
            playerExo.setMediaItem(mediaItem)
            playerExo.prepare()
            playerExo.play()
        }

        if (sessionManager.getIsFromTryAudio()) {
            findViewById<ImageButton>(R.id.music).visibility = View.GONE
            val songUrl = APITags.ADMINBASEURL + sessionManager.getVideoSongUrl()
            maxVideoDuration = sessionManager.getAudioDuration().toLong()
            minVideoDuration = sessionManager.getAudioDuration().toLong() - 1000
            durationHint.text = "Record up to ${minVideoDuration / 1000} seconds"

            cameraLayout.captureButton.progressDuration = maxVideoDuration
            myApplication.printLogD("songUrl :$songUrl", "songUrl")
            val mediaSource by lazy {
                ProgressiveMediaSource.Factory(
                    CacheDataSource.Factory()
                        .setCache(VideoCacheWork.simpleCache)
                        .setUpstreamDataSourceFactory(
                            DefaultHttpDataSource.Factory()
                                .setUserAgent("ExoPlayer")
                        )
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                )
            }

            val mediaItem = MediaItem.fromUri(songUrl)
            val audioMediaSource = mediaSource.createMediaSource(mediaItem)
            audioExoPlayer.player = playerExo
            playerExo.setMediaSource(audioMediaSource)
            playerExo.prepare()
            playerExo.play()
        }

        findViewById<ImageButton>(R.id.music).setOnClickListener {
            sendToMusicActivity()
        }

        super.onResume()
    }

    private fun sendToMusicActivity() {
        val intent = Intent(this@SnapCameraActivity, MusicActivity::class.java)
        sessionManager.setIsAppAudio(true)
        startActivity(intent)
    }

    private fun sendToVideoPreview(videoUri: String, videoDuration: Long) {
        myApplication.printLogD("sendToVideoPreview Call", "TrimAudio")
        recordingCloseable?.close()
        cameraSession.close()

        mediaCaptureExecutor.shutdown()
        audioProcessorExecutor.shutdown()
        if (sessionManager.getVideoSongUrl().toString().isNotEmpty()) {
            if (sessionManager.getIsFromTryAudio() || sessionManager.getIsAppAudio()) {
                sessionManager.setCreateVideoSession(videoUri, "", videoDuration)
                myApplication.printLogD(
                    "sendToVideoPreview audioURl ${sessionManager.getVideoSongUrl()}",
                    "audioUrl"
                )
                runOnUiThread {
                    progressLayout.visibility = View.VISIBLE
                    try {
                        if (playerExo.isPlaying) {
                            playerExo.stop()
                            playerExo.release()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (sessionManager.getIsFromTryAudio()) {
                    TrimAudio(
                        APITags.ADMINBASEURL + sessionManager.getVideoSongUrl().toString(),
                        0,
                        videoDuration
                    )
                } else {
                    TrimAudio(appSongUrl, 0, videoDuration)
                }

            }
        } else {
            progressLayout.visibility = View.GONE
            myApplication.printLogD("selectVideoDuration : $videoUri", TAG)
            sessionManager.setCreateVideoSession(videoUri, "", videoDuration)
            sessionManager.setVideoHashTag(hashTag)
            Log.d("hashTrack", "SnapCameraActivity: $hashTag")
            myApplication.printLogD(
                isFromContest.toString() + "sendToVideoPreview 1",
                "isFromContest"
            )
            myApplication.printLogD(
                sessionManager.getIsFromContest().toString() + "sendToVideoPreview 2",
                "isFromContest"
            )
            myApplication.printLogD(
                sessionManager.getContestEntryFee().toString() + "sendToVideoPreview 2",
                "contestCheck"
            )
            val intent = Intent(this@SnapCameraActivity, SnapPreviewActivity::class.java)
            startActivity(intent)
        }

    }

    private fun TrimAudio(audioFile: String, audioTrimFromSec: Long, createVideoDuration: Long) {
        val trimFilePath = createAudioFile()

        val endPosition = audioTrimFromSec + createVideoDuration
        val firstPosition = audioTrimFromSec / 1000
        myApplication.printLogD("VideoLength ${createVideoDuration / 1000}", "TrimAudio")
        myApplication.printLogD("firstPosition $firstPosition", "TrimAudio")
        myApplication.printLogD("endPosition ${endPosition / 1000}", "TrimAudio")
        myApplication.printLogD("trimFilePath $trimFilePath", "TrimAudio")

        val cmd =
            "-y -i $audioFile -ss $firstPosition -t ${createVideoDuration / 1000} -acodec copy -preset veryfast -threads 6 $trimFilePath"

        EpEditor.execCmd(cmd, 0, object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("TrimAudio Complete", "TrimAudio")
                sessionManager.setCreateAudioSession(trimFilePath)
                val bundle = Bundle()
                bundle.putString(ConstValFile.CompileTask, ConstValFile.TaskMuxing)
                startActivity(
                    Intent(this@SnapCameraActivity, CompilerActivity::class.java)
                        .putExtra(ConstValFile.Bundle, bundle)
                )

            }

            override fun onFailure() {
                myApplication.printLogD("TrimAudio : onFailure", "TrimAudio")
            }

            override fun onProgress(progress: Float) {
                myApplication.printLogD("TrimAudio onProgress : $progress", "TrimAudio")
            }
        })

        /*   FFmpegKit.executeAsync(cmd,
               { session ->
                   val state = session.state
                   val returnCode = session.returnCode
                   if (ReturnCode.isSuccess(returnCode)){
                       myApplication.printLogD("TrimAudio Complete","TrimAudio")
                       sessionManager.setCreateAudioSession(trimFilePath)
                       val bundle = Bundle()
                       bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
                       startActivity(Intent(this@SnapCameraActivity,CompilerActivity::class.java)
                           .putExtra(ConstValFile.Bundle,bundle))
                   }
                   // CALLED WHEN SESSION IS EXECUTED
                   Log.i("TrimAudio", String.format("FFmpeg process exited with state %s and rc %s.%s",
                       state, returnCode,  session.failStackTrace))
               },
               {
                   myApplication.printLogD("log : $it","TrimAudio")
               })
           {
               myApplication.printLogD("statistics : $it","TrimAudio")
           }*/
    }

    override fun onStart(captureType: SnapButtonView.CaptureType) {

        myApplication.printLogD(captureType.toString(), "CaptureType")

        if (videoFilePath.isNotEmpty()) {
            if (File(videoFilePath).exists()) {
                File(videoFilePath).delete()
            }
        }

        if (captureType == SnapButtonView.CaptureType.CONTINUOUS) {
            myApplication.printLogI("onStart Camera", TRACK)
            if (isStartTime) {
                resumeTimer()
            } else {
                startTimer(maxVideoDuration)
            }
            if (sessionManager.getIsFromTryAudio() || sessionManager.getIsAppAudio()) {
                playerExo.seekTo(0)
                playerExo.prepare()
                playerExo.play()
            }
            if (videoFilePath.isEmpty()) {
                videoFilePath = ""
                videoFilePath = createFileAndFolder()
                myApplication.printLogD(videoFilePath, "videoFileCreate")
            }
            if (recordingCloseable == null) {
                // Create capture class that starts encoding upon initialization
                val outputCloseable: Closeable
                val captureCloseable =
                    MediaCapture(
                        this@SnapCameraActivity,
                        File(videoFilePath),
                        audioSource,
                        mediaCaptureExecutor
                    ).also {
                        // Get encoding surface and connect it as image processor output
                        // Retain closeable for disconnecting output when done
                        outputCloseable = cameraSession.processor.connectOutput(
                            outputFrom(it.surface, ImageProcessor.Output.Purpose.RECORDING)
                        )
                    }
                recordingCloseable = Closeable {
                    outputCloseable.close()
                    captureCloseable.close()
                }
            } else {
                recordingCloseable?.close()
            }
        }

    }

    override fun onEnd(captureType: SnapButtonView.CaptureType) {
        myApplication.printLogD("onEnd Call", "check 200")
        if (sessionManager.getIsFromTryAudio() || sessionManager.getIsAppAudio()) {
            playerExo.seekTo(0)
            playerExo.pause()
        }
        when (captureType) {
            // Only showing support for video recording in this sample
            SnapButtonView.CaptureType.CONTINUOUS -> {
                recordingCloseable?.close()
                recordingCloseable = null
            }

            else -> {}
        }
    }


    override fun onBackPressed() {
        recordingCloseable?.close()
        cameraSession.close()
        mediaCaptureExecutor.shutdown()
        audioProcessorExecutor.shutdown()

        if (playerExo.isPlaying) {
            playerExo.stop()
            playerExo.release()
        }
        try {
            if (File(sessionManager.getCreateVideoPath().toString()).exists()) {
                File(sessionManager.getCreateVideoPath().toString()).delete()
            }
            if (File(sessionManager.getCreateDuetVideoUrl().toString()).exists()) {
                File(sessionManager.getCreateDuetVideoUrl().toString()).delete()
            }
            if (File(sessionManager.getTrimAudioPath().toString()).exists()) {
                File(sessionManager.getTrimAudioPath().toString()).delete()
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        finish()
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
    }

}
