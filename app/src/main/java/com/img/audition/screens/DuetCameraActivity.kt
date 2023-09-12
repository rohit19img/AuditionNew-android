package com.img.audition.screens

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.customView.LineProgressView
import com.img.audition.databinding.ActivityDuetCameraBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.snapCameraKit.*
import com.img.audition.snapCameraKit.LensesAdapter
import com.img.audition.snapCameraKit.MediaCapture
import com.snap.camerakit.*
import com.snap.camerakit.common.Consumer
import com.snap.camerakit.lenses.LensesComponent
import com.snap.camerakit.lenses.whenHasSome
import com.snap.camerakit.support.camerax.CameraXImageProcessorSource
import com.snap.camerakit.support.permissions.HeadlessFragmentPermissionRequester
import com.snap.camerakit.support.widget.SnapButtonView
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@UnstableApi
class DuetCameraActivity : AppCompatActivity(),MediaCapture.MediaCaptureCallback,SnapButtonView.OnCaptureRequestListener{

    private val TAG = "DuetCameraActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityDuetCameraBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@DuetCameraActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    lateinit var videoPlayer : ExoPlayer
    private var timeRemainingInMillis: Long = 0
    private var totalTime: Long = 0
    private var isTimerRunning = false
    private var isTimerPaused = false
    private lateinit var countDownTimer: CountDownTimer
    private var isStartTime = false
    private var maxVideoDuration: Long = 15500
    private var minVideoDuration: Long = 5 * 1000

    private var videoFilePath = ""
    private var duetVideoUrl =""
    private var duetWithId = ""

    private lateinit var mLineView: LineProgressView

    //snapCamera
    private var recordingCloseable: Closeable? = null
    private lateinit var cameraKitSession: Session
    private lateinit var imageProcessorSource: CameraXImageProcessorSource

    private lateinit var rootContainer: MotionLayout
    private lateinit var liveCameraContainer: ViewGroup
    private lateinit var previewGestureHandler: View
    private lateinit var selectedLensContainer: ViewGroup
    private lateinit var selectedLensNameView: TextView
    private lateinit var selectedLensIcon: ImageView

    private lateinit var lensesAdapter: LensesAdapter
    private lateinit var lensesListContainer: LinearLayout

    private var isCameraFacingFront = true
    private val processorExecutor = Executors.newSingleThreadExecutor()
    private var permissionRequest: Closeable? = null
    private var lensRepositorySubscription: Closeable? = null
    lateinit var  audioSource: AudioProcessorSource
    private val audioProcessorExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mediaCaptureExecutor: ExecutorService = Executors.newFixedThreadPool(2)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        videoPlayer = ExoPlayer.Builder(this@DuetCameraActivity).build()

        findViewById<ImageView>(R.id.backPressIC).setOnClickListener {
            onBackPressed()
        }

        if (!supported(this)) {
            Toast.makeText(this, "Camera Kit not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            audioSource =  AudioProcessorSource(audioProcessorExecutor)
        }

        mLineView = findViewById(R.id.line_view)
        liveCameraContainer = findViewById(R.id.camera_preview_container)
        selectedLensContainer = findViewById(R.id.selected_lens_container)
        selectedLensIcon = findViewById(R.id.selected_lens_icon)
        selectedLensNameView = findViewById(R.id.selected_lens_name)
        rootContainer = findViewById<MotionLayout>(R.id.root_container).apply {
            doOnLayout { view ->
                val dimensionRatio = "W,${view.width}:${view.height}"
                rootContainer.getConstraintSet(R.id.lenses_selector_collapsed).apply {
                    setDimensionRatio(R.id.camera_preview_container, dimensionRatio)
                }
                rootContainer.getConstraintSet(R.id.lenses_selector_expanded).apply {
                    setDimensionRatio(R.id.camera_preview_container, dimensionRatio)
                }
            }
        }
        lensesListContainer = findViewById(R.id.lenses_list_container)


        val tapGestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(event: MotionEvent): Boolean {
                    flipCamera()
                    return super.onDoubleTap(event)
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    rootContainer.transitionToStart()
                    return super.onSingleTapConfirmed(e)
                }
            }
        )

        previewGestureHandler = findViewById<View>(R.id.preview_gesture_handler).apply {
            setOnTouchListener { _, event ->
                tapGestureDetector.onTouchEvent(event)
                when (event.action) {
                    MotionEvent.ACTION_UP -> performClick()
                }
                true
            }
        }

        // App can either use Camera Kit's CameraXImageProcessorSource (which is part of the :support-camerax
        // dependency) or their own input/output and later attach it to the Camera Kit session.
        imageProcessorSource = CameraXImageProcessorSource(
            context = this,
            lifecycleOwner = this,
            executorService = processorExecutor,
            videoOutputDirectory = cacheDir
        )

        // App can either use Camera Kit's CameraLayout for easy integration or define a custom
        // layout like in this case. For custom layout, app needs to just attach the view to Camera
        // Kit Session. Also, App Id and API token can be passed dynamically through Session APIs like in this
        // case (recommended) or it can be hardcoded in AndroidManifest.xml file.
        cameraKitSession = Session(this) {
            apiToken(BuildConfig.CAMERA_KIT_API_TOKEN)
            applicationId(BuildConfig.APPLICATION_ID)
            if (audioSource != null) {
                audioProcessorSource(audioSource)
            }
            imageProcessorSource(imageProcessorSource)
            attachTo(findViewById(R.id.camera_kit_stub))
            safeRenderAreaProcessorSource(SafeRenderAreaProcessorSource(this@DuetCameraActivity))
            configureLenses {
                // When CameraKit is configured to manage its own views by providing a view stub,
                // lenses touch handling might consume all events due to the fact that it needs to perform gesture
                // detection internally. If application needs to handle gestures on top of it then LensesComponent
                // provides a way to dispatch all touch events unhandled by active lens back.
                this.dispatchTouchEventsTo(previewGestureHandler)
            }
        }

        getOsPermissions()

        // This block demonstrates how to query the repository to get all Lenses from a Camera Kit
        // group. You can query from multiple groups or pre-fetch all Lenses before even user opens
        // the Camera Kit integration. Camera Kit APIs are thread safe - so it's safe to call them
        // from here.
        lensRepositorySubscription = cameraKitSession.lenses.repository.observe(
            LensesComponent.Repository.QueryCriteria.Available(setOf(BuildConfig.LENS_GROUP_ID_TEST))
        ) { result ->
            result.whenHasSome { lenses ->
                runOnUiThread {
                    lensesAdapter.submitList(lenses)
                }
            }
        }

        findViewById<SnapButtonView>(R.id.capture_button).apply {
            this.onCaptureRequestListener = this@DuetCameraActivity
        }

        findViewById<ImageButton>(R.id.camera_flip_button).apply {
            setOnClickListener {
                flipCamera()
            }
        }

        findViewById<ImageButton>(R.id.button_cancel_effect).apply {
            setOnClickListener {
                clearLenses()
            }
        }

        findViewById<RecyclerView>(R.id.lenses_list).apply {
            lensesAdapter = LensesAdapter { selectedLens ->
                applyLens(selectedLens)
            }
            layoutManager = GridLayoutManager(this@DuetCameraActivity, 3)
            adapter = lensesAdapter
        }
    }


    private fun applyLens(lens: LensesComponent.Lens) {
        val usingCorrectCamera =
            isCameraFacingFront.xor(lens.facingPreference != LensesComponent.Lens.Facing.FRONT)
        if (!usingCorrectCamera) flipCamera()
        cameraKitSession.lenses.processor.apply(lens) { success ->
            if (success) {
                runOnUiThread {
                    lensesAdapter.select(lens)
                    selectedLensContainer
                        .animate()
                        .alpha(1.0f)
                        .withStartAction {
                            selectedLensContainer.visibility = View.VISIBLE
                        }
                        .start()
                    selectedLensNameView.text = lens.name
                    Glide.with(selectedLensIcon).load(
                        lens.icons.find {
                            it is LensesComponent.Lens.Media.Image.Webp
                        }?.uri
                    ).into(selectedLensIcon)
                }
            }
        }
    }

    private fun clearLenses() {
        cameraKitSession.lenses.processor.clear { success ->
            if (success) {
                runOnUiThread {
                    lensesAdapter.deselect()
                    selectedLensContainer
                        .animate()
                        .alpha(0.0f)
                        .withEndAction {
                            selectedLensContainer.visibility = View.INVISIBLE
                        }
                        .start()
                }
            }
        }
    }

    private fun flipCamera() {
        runOnUiThread {
            imageProcessorSource.startPreview(!isCameraFacingFront)
            isCameraFacingFront = !isCameraFacingFront
        }
    }

    override fun onDestroy() {
        permissionRequest?.close()
        lensRepositorySubscription?.close()
        recordingCloseable?.close()
        cameraKitSession.close()
        processorExecutor.shutdown()
        mediaCaptureExecutor.shutdown()
        audioProcessorExecutor.shutdown()
        if(videoFilePath.isNotEmpty() && File(videoFilePath).exists()){
            File(videoFilePath).delete()
        }
        super.onDestroy()
    }

    private fun getOsPermissions() {
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        // HeadlessFragmentPermissionRequester is part of the :support-permissions, which allows you to easily handle
        // permission requests
        permissionRequest =
            HeadlessFragmentPermissionRequester(this, requiredPermissions.toSet()) { permissions ->
                if (requiredPermissions.mapNotNull(permissions::get).all { it }) {
                    imageProcessorSource.startPreview(isCameraFacingFront)
                } else {
                    Log.e(TAG, "Permissions denied: $permissions")
                }
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
                    onFinish()
                }
            }

            override fun onFinish() {
                isTimerRunning = false
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

    private fun updateCountdownTextView() {
        val minutes = (maxVideoDuration / 1000) / 60
        val seconds = (maxVideoDuration / 1000) % 60
        totalTime = maxVideoDuration - timeRemainingInMillis
        val elapsedMinutes = (totalTime / 1000) / 60
        val elapsedSeconds = (totalTime / 1000) % 60
        val timeElapsedFormatted = String.format(Locale.getDefault(), "%02d:%02d", elapsedMinutes, elapsedSeconds)
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        findViewById<TextView>(R.id.videoDuration).text = timeElapsedFormatted

        mLineView.setLoadingProgress(totalTime * 1.0f / maxVideoDuration)

    }


    private fun createFileAndFolder(): String {
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
        val appData = getExternalFilesDir(null)

        val createFile = File(appData, filename)
        if (!(createFile.exists())) {
            try {
                createFile.createNewFile()
            } catch (i: IOException) {
                i.printStackTrace()
            }
        }
        return createFile.absolutePath

    }

    private fun sendToVideoMerge(createVideoUrl: String,videoDuration: Long) {
        sessionManager.setDuetVideoSession(createVideoUrl,duetVideoUrl,duetWithId,true)
        sessionManager.setCreateVideoDuration(videoDuration)
        permissionRequest?.close()
        lensRepositorySubscription?.close()
        recordingCloseable?.close()
        cameraKitSession.close()
        processorExecutor.shutdown()
        mediaCaptureExecutor.shutdown()
        audioProcessorExecutor.shutdown()
        runOnUiThread {
            viewBinding.progressLoading.visibility  = View.VISIBLE
            viewBinding.progressText.visibility  = View.VISIBLE
        }
        DownloadDuetVideo().execute(sessionManager.getDuetVideoUrl()!!)
    }


    override fun onResume() {
        super.onResume()
        duetVideoUrl = bundle!!.getString(ConstValFile.DuetVideoUrl).toString()
        duetWithId = "#duet with ${bundle!!.getString(ConstValFile.AuditionID)}"
        viewBinding.videoPreview.player = videoPlayer
        val mediaItem = MediaItem.fromUri(duetVideoUrl)
        videoPlayer.setMediaItem(mediaItem)
        videoPlayer.prepare()

        videoPlayer.addListener(@UnstableApi object : Player.Listener {
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
                        viewBinding.progressLoading.visibility = View.GONE
                        viewBinding.progressText.visibility = View.GONE
                        maxVideoDuration = videoPlayer.contentDuration
                        minVideoDuration = videoPlayer.contentDuration
                        viewBinding.captureButton.progressDuration = maxVideoDuration

                    }
                    else -> {
                        Log.d("check 200", "currentState: $playbackState")
                    }
                }
            }
        })
    }


    override fun onPause() {
        super.onPause()
        videoPlayer.pause()
    }


    /**
     * Simple implementation of a [Source] for a [SafeRenderAreaProcessor] that calculates a safe render area [Rect]
     * that is between the top and selected lens container present in the [DuetCameraActivity].
     */
    private class SafeRenderAreaProcessorSource(DuetCameraActivity: DuetCameraActivity) : Source<SafeRenderAreaProcessor> {

        private val DuetCameraActivityReference = WeakReference(DuetCameraActivity)

        override fun attach(processor: SafeRenderAreaProcessor): Closeable {

            return processor.connectInput(object : SafeRenderAreaProcessor.Input {

                override fun subscribeTo(onSafeRenderAreaAvailable: Consumer<Rect>): Closeable {
                    val activity = DuetCameraActivityReference.get()
                    if (activity == null) {
                        return Closeable { }
                    } else {
                        fun updateSafeRenderRegionIfNecessary() {
                            val safeRenderRect = Rect()
                            if (activity.liveCameraContainer.getGlobalVisibleRect(safeRenderRect)) {
                                val tmpRect = Rect()
                                activity.window.decorView.getWindowVisibleDisplayFrame(tmpRect)
                                val statusBarHeight = tmpRect.top
                                // Make the zone's top to start below the status bar.
                                safeRenderRect.top = statusBarHeight
                                // Make the zone's bottom to start above selected lens container,
                                // anything under or below it should not be considered safe to render to.
                                if (activity.selectedLensContainer.getGlobalVisibleRect(tmpRect)) {
                                    safeRenderRect.bottom = tmpRect.top - statusBarHeight
                                }
                                onSafeRenderAreaAvailable.accept(safeRenderRect)
                            }
                        }
                        // The processor might subscribe to the input when views are laid out already so we can attempt
                        // to calculate the safe render area already
                        activity.runOnUiThread {
                            updateSafeRenderRegionIfNecessary()
                        }
                        // Otherwise we start listening for layout changes to update the safe render rect continuously
                        val onLayoutChangeListener =
                            View.OnLayoutChangeListener {
                                    _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                                if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                                    updateSafeRenderRegionIfNecessary()
                                }
                            }

                        val transitionListener = object : TransitionAdapter() {

                            override fun onTransitionChange(
                                motionLayout: MotionLayout?,
                                startId: Int,
                                endId: Int,
                                progress: Float
                            ) {
                                updateSafeRenderRegionIfNecessary()
                            }
                        }

                        activity.rootContainer.addOnLayoutChangeListener(onLayoutChangeListener)
                        activity.rootContainer.addTransitionListener(transitionListener)
                        return Closeable {
                            activity.rootContainer.removeOnLayoutChangeListener(onLayoutChangeListener)
                            activity.rootContainer.removeTransitionListener(transitionListener)
                        }
                    }
                }
            })
        }
    }

    override fun onStart(captureType: SnapButtonView.CaptureType) {

        if (videoFilePath.isNotEmpty()){
            if (File(videoFilePath).exists()){
                File(videoFilePath).delete()
            }
        }
        if (captureType == SnapButtonView.CaptureType.CONTINUOUS) {

            videoPlayer.seekTo(0)
            videoPlayer.prepare()
            videoPlayer.play()


            if (isStartTime){
                resumeTimer()
            }else{
                startTimer(maxVideoDuration)
            }

            if (videoFilePath.isEmpty()){
                videoFilePath = ""
                videoFilePath = createFileAndFolder()
            }else{

            }
            if (recordingCloseable == null) {
                // Create capture class that starts encoding upon initialization
                val outputCloseable: Closeable
                val captureCloseable =
                    MediaCapture(this@DuetCameraActivity, File(videoFilePath), audioSource, mediaCaptureExecutor).also {
                        // Get encoding surface and connect it as image processor output
                        // Retain closeable for disconnecting output when done
                        outputCloseable = cameraKitSession.processor.connectOutput(
                            outputFrom(it.surface, ImageProcessor.Output.Purpose.RECORDING)
                        )
                    }
                recordingCloseable = Closeable {
                    outputCloseable.close()
                    captureCloseable.surface.release()
                    captureCloseable.close()
                }
            }else{
                recordingCloseable?.close()
            }
        }

    }

    override fun onEnd(captureType: SnapButtonView.CaptureType) {

        videoPlayer.seekTo(0)
        videoPlayer.pause()
        when (captureType) {
            // Only showing support for video recording in this sample
            SnapButtonView.CaptureType.CONTINUOUS -> {
                recordingCloseable?.close()
                recordingCloseable = null
            }
            else -> {}
        }
    }

    override fun onSaved(file: File) {
        Thread.sleep(100)
        if (totalTime <= minVideoDuration-1000){
            recordingCloseable?.close()

            if (file.exists())
                file.delete()
            runOnUiThread {
                Toast.makeText(this,"Video must be ${minVideoDuration/1000} second and more..",Toast.LENGTH_SHORT).show()
                stopTimer()
            }
        }else{
            var selectVideoDuration = 0L
            try {
                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(this@DuetCameraActivity, Uri.fromFile(file))
                val  time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()

                selectVideoDuration = time!!.toLong()
            }catch (e:java.lang.Exception){
                e.printStackTrace()            }
            sendToVideoMerge(file.absolutePath,selectVideoDuration)
        }
    }

    override fun onError(e: Exception) {
        e.printStackTrace()
    }


    override fun onBackPressed() {
        if(videoFilePath.isNotEmpty() && File(videoFilePath).exists()){
            File(videoFilePath).delete()
        }
        videoPlayer.stop()
        videoPlayer.release()
        permissionRequest?.close()
        lensRepositorySubscription?.close()
        recordingCloseable?.close()
        cameraKitSession.close()
        processorExecutor.shutdown()
        mediaCaptureExecutor.shutdown()
        audioProcessorExecutor.shutdown()
        super.onBackPressed()
    }


    private inner class DownloadDuetVideo : AsyncTask<String, Void, String>() {
        @SuppressLint("Range")
        override fun doInBackground(vararg urls: String?): String {
            val url: String = urls[0].toString()
            val downloadFilePath = createFileAndFolder()
            val request = DownloadManager.Request(Uri.parse(url))
                .setDestinationUri(Uri.fromFile(File(downloadFilePath)))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
            request.allowScanningByMediaScanner()
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            var downloading = true

            while (downloading) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId)
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        return cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        val reason: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                        // Handle the download failure
                        downloading = false
                        return ""
                    }
                }
                cursor.close()
            }
            return ""
        }

        override fun onPostExecute(path: String?) {
            if (path != null) {
                val downloadFilePath = path
                sessionManager.setDuetVideoUrl(downloadFilePath)
                /* val outputPath1 =  createFileAndFolder()
                 val inputPath = sessionManager.getCreateDuetVideoUrl()
                 val prepareFirstVideoCmd = "-y -i $inputPath -preset ultrafast -vf scale=480:840 $outputPath1"
                 prepareFirstVideo(prepareFirstVideoCmd,outputPath1)*/

                val outputPath1 = sessionManager.getCreateDuetVideoUrl()
                val outputPath2 = downloadFilePath
                val finalPath =  createFileAndFolder()
                val finalCmd = "-y -i $outputPath1 -i $outputPath2 -filter_complex [0:v]scale=480:640[p1];[1:v]scale=480:640[p2];[p1][p2]hstack -c:v libx264 -crf 30 -preset ultrafast -threads 8 $finalPath"
                funCompiler(finalCmd,finalPath)

            } else {
                Log.e(TAG, "video Download Failed : ")
            }
        }

        fun getImageAbsolutePath(context: Context, fileName: String): String {
            val assetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val file = File(context.cacheDir, fileName)

            try {
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file.absolutePath
        }
    }

    fun funCompiler(cmd: String, outputPath: String){

        EpEditor.execCmd(cmd,0, object : OnEditorListener {
            override fun onSuccess() {
                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(this@DuetCameraActivity, Uri.parse(outputPath));
                val  time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()
                val selectVideoDuration = time!!.toLong()
                sessionManager.setCreateVideoPath(outputPath)
                sessionManager.setCreateVideoDuration(selectVideoDuration)
                val bundle = Bundle()
                bundle.putString(ConstValFile.CompileTask,ConstValFile.MergeVideo)
                val intent = Intent(this@DuetCameraActivity, CompilerActivity::class.java)
                intent.putExtra(ConstValFile.Bundle,bundle)
                startActivity(intent)
            }
            override fun onFailure() {
//                myApplication.showToast("Failed, Try Again..")
//                Toast.makeText(this@CompilerActivity,"Something went wrong,Try Again..", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@DuetCameraActivity,SnapPreviewActivity::class.java))
                finish()
            }
            override fun onProgress(progress: Float) {}
        })
    }
}