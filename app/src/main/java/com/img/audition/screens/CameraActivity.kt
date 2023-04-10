package com.img.audition.screens

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.util.Consumer
import androidx.media3.common.util.UnstableApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.img.audition.R
import com.img.audition.cameraX.getNameString
import com.img.audition.customView.RecordButton
import com.img.audition.databinding.ActivityCameraBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.MusicListFragment
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


@UnstableApi class CameraActivity : AppCompatActivity(), RecordButton.OnGestureListener{

    private var maxVideoDuration:Long = 15 * 1000
    private var minVideoDuration:Long = 5 * 1000
    private val curreentVideoDuration = mutableListOf<Int>()

    private var videoFilePath = ""

    private val sessionManager by lazy {
        SessionManager(this@CameraActivity)
    }
    private var isFromContest = false

    var dir = File(File(Environment.getExternalStorageDirectory(), "Audition"), "Audition")

    companion object {
        private const val MUSIC_TAG = "music_tag"

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

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    var mode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        askForPermissionsAndroidUpperVerssion();
        viewBinding.switchCamera.setOnClickListener {
            if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA)
                lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
            else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA)
                lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA

            val t: Thread = object : Thread() {
                override fun run() {
                    startCamera(mode)
                }
            }
            t.start()
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        viewBinding.flash.setOnClickListener {
            mode = !mode
            if (mode){
                viewBinding.flash.setImageDrawable(ContextCompat.getDrawable(this@CameraActivity,R.drawable.ic_flash_on))
            }else{
                viewBinding.flash.setImageDrawable(ContextCompat.getDrawable(this@CameraActivity,R.drawable.ic_flash_off))
            }
            val t: Thread = object : Thread() {
                override fun run() {
                    startCamera(mode)
                }
            }
            t.start()
        }

        viewBinding.music.setOnClickListener {
            showMusicSheet()
        }


        viewBinding.done.visibility = View.INVISIBLE
        if (allPermissionsGranted()) {
            val t: Thread = object : Thread() {
                override fun run() {
                    startCamera(mode)
                }
            }
            t.start()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        viewBinding.recordButton.setOnGestureListener(this)



    }

    private fun sendToVideoPreview(videoUri:String) {
        sessionManager.setCreateVideoSession(videoUri)
        val intent = Intent(this@CameraActivity,PreviewActivity::class.java)
        startActivity(intent)
    }

    override fun onDown() {
        viewBinding.showPreviewBtn.visibility = View.GONE
        var duration = 0
        curreentVideoDuration.forEach {
            duration = it
        }
        myApplication.printLogD("onDown: duration : $duration",TAG)
        if (!this::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize)
        {
            val t: Thread = object : Thread() {
                override fun run() {
                    startRecording()
                }
            }
            t.start()

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

    private fun startCamera(mode:Boolean) {


        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                /*.setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(viewBinding.previewView.display.rotation)*/
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)




            val extensionsManager = ExtensionsManager.getInstanceAsync(this,cameraProvider).get()
            val cameraSelector = lensFacing


            try {

                if (extensionsManager.isExtensionAvailable(cameraSelector,ExtensionMode.BOKEH)){
                    cameraProvider.unbindAll()
                    val bokehCameraSelector = extensionsManager
                        .getExtensionEnabledCameraSelector(
                            cameraSelector,
                            ExtensionMode.FACE_RETOUCH
                        )
                    val cam = cameraProvider.bindToLifecycle(
                        this, bokehCameraSelector, preview,videoCapture)
                    if ( cam.cameraInfo.hasFlashUnit() ) {
                        cam.cameraControl.enableTorch(mode); // or false
                    }
                }else{
                    cameraProvider.unbindAll()
                    val cam = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview,videoCapture)
                    if ( cam.cameraInfo.hasFlashUnit() ) {
                        cam.cameraControl.enableTorch(mode); // or false
                    }
                }// Unbind use cases before rebinding

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
                startCamera(mode)
            } else {
                myApplication.showToast("Please Allow permission..")
            }
        }
    }

    private fun startRecording() {


       /* val name = "audition-recording-" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"*/

       /* val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }*/

       /* val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver,MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()*/
        val videoCapture = this.videoCapture ?: return
        val fileOutputOptions = FileOutputOptions.Builder(File(createFileAndFolder())).build()


        currentRecording = videoCapture.output

            .prepareRecording(this, fileOutputOptions)
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

        val t: Thread = object : Thread() {
            override fun run() {
                updateUI(event)
            }
        }
        t.start()


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

        runOnUiThread {
            viewBinding.done.setOnClickListener {
                viewBinding.done.isSelected = false
                currentRecording?.stop()
                viewBinding.done.visibility = View.GONE
                viewBinding.showPreviewBtn.visibility = View.VISIBLE
            }

            viewBinding.showPreviewBtn.setOnClickListener{
                if (event is VideoRecordEvent.Finalize){
                    videoFilePath = event.outputResults.outputUri.toString()
                    sendToVideoPreview(videoFilePath)
                    myApplication.printLogD(videoFilePath,"VideoFilePath")
//                myApplication.showToast(videoFilePath)
                }
            }
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
                        myApplication.showToast("Video Max Duration Reached")
                    }
                }
            }
            viewBinding.lineView.setLoadingProgress(time * 1.0f / maxVideoDuration)
        }
    }

    private fun showMusicSheet() {
        var musicFragment = supportFragmentManager.findFragmentByTag(MUSIC_TAG)
        if (musicFragment == null) {
            musicFragment = MusicListFragment.newInstance()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(viewBinding.frameContainer.id,musicFragment)
            transaction.commit()
        }
        val behavior = BottomSheetBehavior.from(viewBinding.frameContainer)
        if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun closeBottomSheet() {
        val behavior = BottomSheetBehavior.from(viewBinding.frameContainer)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }


    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
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

    override fun onStart() {
        super.onStart()
        viewBinding.done.visibility = View.GONE
        viewBinding.showPreviewBtn.visibility = View.GONE
        maxVideoDuration  = 15 * 1000
        minVideoDuration = 5 * 1000
        viewBinding.lineView.setLoadingProgress(0F)
        viewBinding.lineView.deleteProgress()
        isFromContest = bundle!!.getBoolean(ConstValFile.IsFromContest,false)
        myApplication.printLogD("$isFromContest onStart"," isFromContest + $TAG")
        if (!(isFromContest)){
            sessionManager.clearContestSession()
        }
    }


    private fun askForPermissionsAndroidUpperVerssion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                return
            }
            createDir()
        }
    }

    fun createDir() {
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }
}