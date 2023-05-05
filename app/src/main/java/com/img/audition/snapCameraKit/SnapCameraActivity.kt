package com.img.audition.snapCameraKit

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.SnackbarDuration
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import com.img.audition.network.SessionManager
import com.img.audition.screens.CompilerActivity
import com.img.audition.videoWork.VideoCacheWork
import com.masoudss.lib.utils.uriToFile
import com.snap.camerakit.*
import com.snap.camerakit.extension.auth.loginkit.LoginKitAuthTokenProvider
import com.snap.camerakit.extension.lens.p2d.service.LensPushToDeviceService
import com.snap.camerakit.extension.lens.p2d.service.configurePushToDevice
import com.snap.camerakit.lenses.*
import com.snap.camerakit.support.widget.CameraLayout
import com.snap.camerakit.support.widget.LensesCarouselView
import com.snap.camerakit.support.widget.SnapButtonView
import com.snap.camerakit.support.widget.arCoreSupportedAndInstalled
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "SnapCameraActivity"
private const val BUNDLE_ARG_USE_CUSTOM_LENSES_CAROUSEL = "use_custom_lenses_carousel"
private const val BUNDLE_ARG_MUTE_AUDIO = "mute_audio"
private const val BUNDLE_ARG_ENABLE_DIAGNOSTICS = "enable_diagnostics"
private const val REQUEST_TAKE_GALLERY_VIDEO = 200
private val LENS_GROUPS = arrayOf(
    LENS_GROUP_ID_BUNDLED, // lens group for bundled lenses available in lenses-bundle artifact.
    LensPushToDeviceService.LENS_GROUP_ID, // lens group for lenses obtained using Push to Device functionality.
    *BuildConfig.LENS_GROUP_ID_TEST.split(',').toTypedArray() // temporary lens group for testing
)
@UnstableApi
class SnapCameraActivity : AppCompatActivity(),MediaCapture.MediaCaptureCallback,SnapButtonView.OnCaptureRequestListener {

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

    private val TRACK = "Capture Track"

    private var timeRemainingInMillis: Long = 0
    private var totalTime: Long = 0
    private var isTimerRunning = false
    private var isTimerPaused = false
    private lateinit var countDownTimer: CountDownTimer
    private var isStartTime = false
    private var maxVideoDuration: Long = 15500
    private var minVideoDuration: Long = 5 * 1000

    private lateinit var mLineView: LineProgressView

    private var recordingCloseable: Closeable? = null

    private var videoFilePath = ""

    private var isFromContest = false
    private var hashTag = ""

    private lateinit var cameraSession:Session
    private lateinit var cameraLayout: CameraLayout
    private lateinit var progressLayout: RelativeLayout
    private val closeOnDestroy = mutableListOf<Closeable>()
    private var useCustomLensesCarouselView = true
    private var muteAudio = false
    private var enableDiagnostics = false

    lateinit var  audioSource:AudioProcessorSource
    private val audioProcessorExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mediaCaptureExecutor: ExecutorService = Executors.newFixedThreadPool(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snap_camera)
        Log.d(TAG, "Using the CameraKit version: ${versionFrom(this)}")

        mLineView = findViewById(R.id.line_view)
        progressLayout = findViewById(R.id.progressLayout)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            audioSource =  AudioProcessorSource(audioProcessorExecutor)
        }

        cameraLayout = findViewById<CameraLayout>(R.id.camera_layout).apply {
            // Setting custom audio processor source
            configureSession {
                if (audioSource != null) {
                    audioProcessorSource(audioSource)
                }
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
        isFromContest = bundle!!.getBoolean(ConstValFile.IsFromContest, false)
        hashTag = bundle!!.getString(ConstValFile.VideoHashTag, "")
        if (!(isFromContest)) {
            sessionManager.clearContestSession()
            findViewById<ImageView>(R.id.selectFromGallery).visibility = View.GONE
            myApplication.printLogD("$isFromContest onStart1", " isFromContest + $TAG")
        } else {
            findViewById<ImageView>(R.id.selectFromGallery).visibility = View.VISIBLE
            myApplication.printLogD("$isFromContest onStart2", " isFromContest + $TAG")
        }

        findViewById<ImageView>(R.id.selectFromGallery).setOnClickListener {
            selectVideoFromDevice()
        }

        findViewById<ImageView>(R.id.backPressIC).setOnClickListener {
            onBackPressed()
        }


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val videoUri: Uri? = data!!.data

                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(this@SnapCameraActivity, videoUri);
                val  time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()
                myApplication.printLogD("selectVideoDuration : $time" ,TAG)
                val selectVideoDuration = time!!.toLong()

                if (selectVideoDuration<=15000){
                    sendToVideoPreview(videoUri.toString(),selectVideoDuration)
                    sessionManager.setIsVideoFromGallery(true)
                }else{
                    myApplication.showToast("Please select 15 second video")
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

                if (totalTime>=maxVideoDuration){
                    isTimerRunning = false
                    myApplication.printLogD("Countdown finished 1","check time")
                    this@SnapCameraActivity.onEnd(SnapButtonView.CaptureType.CONTINUOUS)
                    onFinish()
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                myApplication.printLogD("Countdown finished 2","check time")
                this@SnapCameraActivity.onEnd(SnapButtonView.CaptureType.CONTINUOUS)
            }
        }.start()

        isTimerRunning = true
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerPaused = true

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
        val timeElapsedFormatted = String.format(Locale.getDefault(), "%02d:%02d", elapsedMinutes, elapsedSeconds)
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        myApplication.printLogD("$timeElapsedFormatted / $timeLeftFormatted","check time")
        myApplication.printLogD("Time remaining: ${minutes}:${seconds}","check time")
        myApplication.printLogD("Time mills: $timeRemainingInMillis","check time")
        myApplication.printLogD("Time mills: $totalTime","check time")

        findViewById<TextView>(R.id.videoDuration).text = timeElapsedFormatted

        mLineView.setLoadingProgress(totalTime * 1.0f / maxVideoDuration)

    }

    override fun onSaved(file: File) {
        Thread.sleep(100)
        if (totalTime <= minVideoDuration-1000){
            recordingCloseable?.close()

            if (file.exists())
                    file.delete()
            runOnUiThread {
                myApplication.showToast("Video must be ${minVideoDuration/1000} second and more..")
                stopTimer()
            }
        }else{
            var selectVideoDuration = 0L
            try {
                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(this@SnapCameraActivity, Uri.fromFile(file))
                val  time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()
                myApplication.printLogD("selectVideoDuration : $time" ,TAG)
                selectVideoDuration = time!!.toLong()
            }catch (e:java.lang.Exception){
                myApplication.printLogE(e.toString(),TRACK)
            }
            sendToVideoPreview(file.absolutePath,selectVideoDuration)
        }
    }

    override fun onError(e: Exception) {
        myApplication.printLogE(e.toString(),TRACK)
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
        myApplication.printLogD(appData!!.absolutePath,TAG)

        val createFile = File(appData,filename)
        if (!(createFile.exists())){
            try {
                createFile.createNewFile()
                myApplication.printLogD(createFile.absolutePath,TAG)
            }catch (i: IOException){
                myApplication.printLogE(i.toString(),TAG)
            }
        }

        return createFile.absolutePath

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_ARG_USE_CUSTOM_LENSES_CAROUSEL, useCustomLensesCarouselView)
        outState.putBoolean(BUNDLE_ARG_MUTE_AUDIO, muteAudio)
        outState.putBoolean(BUNDLE_ARG_ENABLE_DIAGNOSTICS, enableDiagnostics)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if(videoFilePath.isNotEmpty() && File(videoFilePath).exists()){
            File(videoFilePath).delete()
        }
        closeOnDestroy.forEach { it.close() }
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (cameraLayout.dispatchKeyEvent(event)) {
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    override fun onResume() {
        progressLayout.visibility = View.GONE
        val audioExoPlayer = findViewById<PlayerView>(R.id.audioPlayerView)
        val durationHint = findViewById<TextView>(R.id.durationHint)
        if (sessionManager.getIsFromTryAudio()){
            durationHint.visibility = View.VISIBLE
        }else{
            durationHint.visibility = View.GONE
        }
        if (sessionManager.getIsFromTryAudio()){
            val songUrl = sessionManager.getVideoSongUrl()
            durationHint.text = "Record up to ${sessionManager.getAudioDuration()/1000} seconds"
            maxVideoDuration = sessionManager.getAudioDuration().toLong()
            minVideoDuration = sessionManager.getAudioDuration().toLong()
            cameraLayout.captureButton.progressDuration = maxVideoDuration
            myApplication.printLogD("songUrl :$songUrl","songUrl")
             val mediaSource by lazy {
                ProgressiveMediaSource.Factory(
                    CacheDataSource.Factory()
                        .setCache(VideoCacheWork.simpleCache)
                        .setUpstreamDataSourceFactory(
                            DefaultHttpDataSource.Factory()
                                .setUserAgent("ExoPlayer"))
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR))
            }

            val mediaItem = MediaItem.fromUri(songUrl!!)
            val audioMediaSource = mediaSource.createMediaSource(mediaItem)
            audioExoPlayer.player = playerExo
            playerExo.setMediaSource(audioMediaSource)
            playerExo.prepare()

            val duration = playerExo.duration
            myApplication.printLogD("audioDuration $duration","audioDuration")
        }

        super.onResume()
    }

    private fun sendToVideoPreview(videoUri: String,videoDuration: Long) {
        myApplication.printLogD("sendToVideoPreview Call","TrimAudio")

        if (sessionManager.getIsFromTryAudio()){
            sessionManager.setCreateVideoSession(videoUri,"",videoDuration)
            myApplication.printLogD("sendToVideoPreview audioURl ${sessionManager.getVideoSongUrl()}","audioUrl")
            runOnUiThread{
                progressLayout.visibility = View.VISIBLE
            }
            TrimAudio(ConstValFile.BASEURL+sessionManager.getVideoSongUrl().toString(),0,videoDuration)

        }else{
            progressLayout.visibility = View.GONE
            myApplication.printLogD("selectVideoDuration : $videoUri" ,TAG)
            sessionManager.setCreateVideoSession(videoUri,"",videoDuration)
            sessionManager.setVideoHashTag(hashTag)
            myApplication.printLogD(isFromContest.toString() + "sendToVideoPreview 1","isFromContest")
            myApplication.printLogD(sessionManager.getIsFromContest().toString() + "sendToVideoPreview 2","isFromContest")
            myApplication.printLogD(sessionManager.getContestEntryFee().toString() + "sendToVideoPreview 2","contestCheck")
            val intent = Intent(this@SnapCameraActivity,SnapPreviewActivity::class.java)
            startActivity(intent)
        }

    }

    private fun TrimAudio(audioFile: String, audioTrimFromSec: Long, createVideoDuration: Long) {
        val trimFilePath = createAudioFile()

        val endPosition = audioTrimFromSec+createVideoDuration
        val firstPosition = audioTrimFromSec/1000
        myApplication.printLogD("VideoLength ${createVideoDuration/1000}","TrimAudio")
        myApplication.printLogD("firstPosition $firstPosition","TrimAudio")
        myApplication.printLogD("endPosition ${endPosition/1000}","TrimAudio")
        myApplication.printLogD("trimFilePath $trimFilePath","TrimAudio")


        val cmd =
            "-y -i $audioFile -ss $firstPosition -t ${createVideoDuration/1000} -acodec copy -preset veryfast -threads 6 $trimFilePath"

        EpEditor.execCmd(cmd,0,object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("TrimAudio Complete","TrimAudio")
                sessionManager.setCreateAudioSession(trimFilePath)
                val bundle = Bundle()
                bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
                startActivity(Intent(this@SnapCameraActivity,CompilerActivity::class.java)
                    .putExtra(ConstValFile.Bundle,bundle))

            }
            override fun onFailure() {
                myApplication.printLogD("TrimAudio : onFailure","TrimAudio")
            }
            override fun onProgress(progress: Float) {
                myApplication.printLogD("TrimAudio onProgress : $progress","TrimAudio")
            }
        })

        /* FFmpegKit.executeAsync(cmd,
             { session ->
                 val state = session.state
                 val returnCode = session.returnCode
                 if (ReturnCode.isSuccess(returnCode)){
                     myApplication.printLogD("TrimAudio Complete","TrimAudio")
                     sessionManager.setCreateAudioSession(trimFilePath)
                     val bundle = Bundle()
                     bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
                     contextFromActivity.startActivity(Intent(contextFromActivity.applicationContext,CompilerActivity::class.java)
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


        myApplication.printLogD(captureType.toString(),"CaptureType")

        if (captureType == SnapButtonView.CaptureType.CONTINUOUS) {
            myApplication.printLogI("onStart Camera",TRACK)
            if (isStartTime){
                resumeTimer()
            }else{
                startTimer(maxVideoDuration)
            }
            if (sessionManager.getIsFromTryAudio()){
                playerExo.seekTo(0)
                playerExo.prepare()
                playerExo.play()
            }
            if (videoFilePath.isEmpty()){
                videoFilePath = ""
                videoFilePath = createFileAndFolder()
                myApplication.printLogD(videoFilePath,"videoFileCreate")
            }
            if (recordingCloseable == null) {
                // Create capture class that starts encoding upon initialization
                val outputCloseable: Closeable
                val captureCloseable =
                    MediaCapture(this@SnapCameraActivity, File(videoFilePath), audioSource, mediaCaptureExecutor).also {
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
            }else{
                recordingCloseable?.close()
            }
        }

    }

    override fun onEnd(captureType: SnapButtonView.CaptureType) {
        if (sessionManager.getIsFromTryAudio()){
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


}
