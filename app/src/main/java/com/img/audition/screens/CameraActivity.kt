package com.img.audition.screens

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.util.Consumer
import androidx.media3.common.util.UnstableApi
import com.img.audition.cameraX.getNameString
import com.img.audition.customView.RecordButton
import com.img.audition.databinding.ActivityCameraBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@UnstableApi class CameraActivity : AppCompatActivity(), RecordButton.OnGestureListener{

    private var maxVideoDuration:Long = 15 * 1000
    private var minVideoDuration:Long = 10 * 1000
    private val curreentVideoDuration = mutableListOf<Int>()

    private var videoFilePath = ""

    companion object {
        val TAG  = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private lateinit var recordingState: VideoRecordEvent
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCameraBinding.inflate(layoutInflater)
    }
    private val myApplication by lazy {
        MyApplication(this@CameraActivity)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val contestIntent = intent.getBundleExtra(ConstValFile.Bundle)
        if (contestIntent!=null){
            val contestID =  contestIntent.getString(ConstValFile.ContestID)
            val contestType = contestIntent.getString(ConstValFile.TYPE_IMAGE)
        }



        viewBinding.done.visibility = View.INVISIBLE
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        viewBinding.recordButton.setOnGestureListener(this)


    }

    private fun sendToVideoPreview(videoUri:String) {
        val bundle = Bundle()
        bundle.putString(ConstValFile.VideoFilePath,videoUri)
        val intent = Intent(this@CameraActivity,PreviewActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)

    }

    override fun onDown() {
        var duration = 0
        curreentVideoDuration.forEach { 
            duration = it
        }
        myApplication.printLogD("onDown: duration : $duration",TAG)
        if (!this::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize)
        {
            startRecording()
        }else{
            myApplication.printLogD("onDown: $recordingState",TAG)

            when(recordingState){
                is VideoRecordEvent.Pause -> currentRecording?.resume()
            }
        }
    }

    override fun onUp() {
        currentRecording?.pause()
    }

    override fun onClick() {}

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()



            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
                }
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,videoCapture)

            } catch(exc: Exception) {
                myApplication.printLogE("Use case binding failed ${exc.toString()}",TAG)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                myApplication.showToast("Please Allow permission..")
            }
        }
    }

    private fun startRecording() {
        val name = "audition-recording-" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        val videoCapture = this.videoCapture ?: return


        currentRecording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply { if (PermissionChecker.checkSelfPermission(this@CameraActivity,
                    Manifest.permission.RECORD_AUDIO) ==
                PermissionChecker.PERMISSION_GRANTED)
            {
                withAudioEnabled()
            } }
            .start(ContextCompat.getMainExecutor(this), captureListener)
    }

    private val captureListener = Consumer<VideoRecordEvent>{ event ->
        if (event !is VideoRecordEvent.Status){
            recordingState = event
        }
        updateUI(event)

    }

    fun updateUI(event: VideoRecordEvent){
        val state = if (event is VideoRecordEvent.Status) recordingState.getNameString()
        else event.getNameString()
        myApplication.printLogD("updateUI: $state",TAG)

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = TimeUnit.MICROSECONDS.toSeconds(stats.recordedDurationNanos)
        var text = "${state}: recorded ${size}KB, in ${time}second"
        if(event is VideoRecordEvent.Finalize){
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"

        }

        viewBinding.done.setOnClickListener {
            viewBinding.done.isSelected = false
            currentRecording?.stop()

            if (event is VideoRecordEvent.Finalize){
                videoFilePath = event.outputResults.outputUri.toString()
            }
            sendToVideoPreview(videoFilePath)
            myApplication.showToast(videoFilePath)
        }

        myApplication.printLogD("updateUI: $size KB $time seconds",TAG)

        runOnUiThread {
            myApplication.printLogD("updateUI: $time time , $maxVideoDuration videoDuration",TAG)
            if (time>=minVideoDuration){
                viewBinding.done.visibility = View.VISIBLE
                minVideoDuration = 20 * 1000
            }
            if (time>=maxVideoDuration){
                if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {

                }else{
                    val recording = currentRecording
                    if (recording != null) {
                        recording.stop()
                        currentRecording = null
                    }
                }
                myApplication.showToast("Video Max Duration Reached")
            }
            viewBinding.lineView.setLoadingProgress(time * 1.0f / maxVideoDuration)
        }
    }


}