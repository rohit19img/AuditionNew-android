package com.img.audition.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import cn.pedant.SweetAlert.SweetAlertDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.img.audition.R
import com.img.audition.databinding.ActivityPreviewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.*
import kotlin.math.sqrt


@UnstableApi
class PreviewActivity : AppCompatActivity() {

    //For Movable text
    private var lastEvent: FloatArray? = null
    private var d = 0f
    private var newRot = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private  var yCoOrdinate = 0f

    private var textColor = -16777216
    private var hexColor = "#000000"
    private var x_Pos = 10
    private var y_Pos = 10
    private var textAlignCmd = "x=(w-text_w)/2:y=(h-text_h)/2"
    //end of Movable text

    private val TAG = "PreviewActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPreviewBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@PreviewActivity)
    }

    private lateinit var videoPlayer : ExoPlayer

    private var isFromContest = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        videoPlayer = ExoPlayer.Builder(this@PreviewActivity).build()
        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.music.setOnClickListener {
            videoPlayer.volume = 0F
            sendToMusicActivity()
        }


        viewBinding.addFilterBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.ColorFilter)
            sendToCompilerActivity(bundle)
        }

        viewBinding.colorPicker.setOnClickListener {
            colorPicker()
        }


    }

    private fun sendToCompilerActivity(bundle: Bundle) {
        val intent = Intent(this@PreviewActivity,CompilerActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    private fun sendToUploadVideoActivity() {
        val intent = Intent(this@PreviewActivity, UploadVideoActivity::class.java)
        startActivity(intent)
    }

    private fun sendToMusicActivity() {
        val intent = Intent(this@PreviewActivity,MusicActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        videoPlayer.pause()
        videoPlayer.stop()
        super.onPause()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val sweetAlertDialog = SweetAlertDialog(this@PreviewActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Discard Video"
        sweetAlertDialog.contentText = "Do you want discard the video"
        sweetAlertDialog.confirmText = "Yes"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            try {
                if (File(sessionManager.getCreateVideoPath().toString()).exists()){
                    File(sessionManager.getCreateVideoPath().toString()).delete()
                }
                if (File(sessionManager.getCreateDuetVideoUrl().toString()).exists()){
                    File(sessionManager.getCreateDuetVideoUrl().toString()).delete()
                }
                if (File(sessionManager.getTrimAudioPath().toString()).exists()){
                    File(sessionManager.getTrimAudioPath().toString()).delete()
                }
            }catch (e :java.lang.Exception){
                e.printStackTrace()
            }
            sessionManager.clearVideoSession()
            sendToMain()
        }
        sweetAlertDialog.cancelText = "No"
        sweetAlertDialog.setCancelClickListener {
            sweetAlertDialog.dismiss()
        }
        sweetAlertDialog.show()
    }

    fun sendToMain() {
        val intent = Intent(this@PreviewActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {

        when(sessionManager.getCreateVideoSpeedState()!!){
            ConstValFile.SlowVideo ->{
                viewBinding.normalVideo.visibility = View.VISIBLE
                viewBinding.fastVideo.visibility = View.VISIBLE
                viewBinding.slowVideo.visibility = View.GONE
            }
            ConstValFile.FastVideo ->{
                viewBinding.normalVideo.visibility = View.VISIBLE
                viewBinding.fastVideo.visibility = View.GONE
                viewBinding.slowVideo.visibility = View.VISIBLE
            }
            else ->{
                viewBinding.normalVideo.visibility = View.GONE
                viewBinding.fastVideo.visibility = View.VISIBLE
                viewBinding.slowVideo.visibility = View.VISIBLE
            }
        }
        viewBinding.slowVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.SlowVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.SlowVideo)
            sendToCompilerActivity(bundle)
        }

        viewBinding.fastVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.FastVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.FastVideo)
            sendToCompilerActivity(bundle)
        }

        viewBinding.normalVideo.setOnClickListener {
            sessionManager.setCreateVideoSpeedState(ConstValFile.NormalVideo)
            val bundle = Bundle()
            bundle.putString(ConstValFile.CompileTask,ConstValFile.NormalVideo)
            sendToCompilerActivity(bundle)
        }

        val movableText = TextView(this)

        movableText.textSize = 20F
        movableText.setPadding(10,10,10,10)

        viewBinding.saveTextOnSurface.setOnClickListener {
            when (viewBinding.textAlign.checkedRadioButtonId) {
                R.id.topLeft -> {
                    textAlignCmd = "x=10:y=10"
                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewBinding.rootLayout.removeView(movableText)
                    movableText.visibility = View.VISIBLE
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    viewBinding.rootLayout.removeView(movableText)
                    viewBinding.rootLayout.addView(movableText,params)
                }
                R.id.topRight -> {
                    textAlignCmd = "x=w-text_w-10:y=10"
                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewBinding.rootLayout.removeView(movableText)
                    movableText.visibility = View.VISIBLE
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    viewBinding.rootLayout.removeView(movableText)
                    viewBinding.rootLayout.addView(movableText,params)

                }
                R.id.bottomLeft -> {
                    textAlignCmd = "x=10:y=h-text_h-10"
                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewBinding.rootLayout.removeView(movableText)
                    movableText.visibility = View.VISIBLE
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    viewBinding.rootLayout.removeView(movableText)
                    viewBinding.rootLayout.addView(movableText,params)

                }
                R.id.bottomRight -> {
                    textAlignCmd = "x=w-text_w-10:y=h-text_h-10"
                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewBinding.rootLayout.removeView(movableText)
                    movableText.visibility = View.VISIBLE
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                    viewBinding.rootLayout.removeView(movableText)
                    viewBinding.rootLayout.addView(movableText,params)

                }
                else -> {
                    textAlignCmd = "x=(w-text_w)/2:y=(h-text_h)/2"
                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    viewBinding.rootLayout.removeView(movableText)
                    movableText.visibility = View.VISIBLE
                    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                    viewBinding.rootLayout.removeView(movableText)
                    viewBinding.rootLayout.addView(movableText,params)

                }
            }

            val text = viewBinding.videoEtText.text.trim()
            movableText.text = text
            movableText.setTextColor(textColor)
            viewBinding.saveTextInVideoBtn.visibility  = View.VISIBLE

            viewBinding.videoEtLyout.visibility = View.GONE

        }

        viewBinding.addTextBtn.setOnClickListener {
            movableText.visibility = View.GONE
            if (viewBinding.videoEtLyout.visibility == View.VISIBLE){
                viewBinding.videoEtLyout.visibility = View.GONE
            }else{
                viewBinding.videoEtLyout.visibility = View.VISIBLE
            }
        }

        movableText.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            val moveText = view as TextView
            moveText.bringToFront()
            viewTransformation(moveText, motionEvent)
            true
        })


        viewBinding.saveTextInVideoBtn.setOnClickListener {
            movableText.visibility  = View.GONE
            val textView = TextView(this@PreviewActivity)
            textView.text = movableText.text.toString()
            textView.setTextColor(android.graphics.Color.parseColor(hexColor))
            textView.textSize = movableText.textSize
            textView.isDrawingCacheEnabled = true
            textView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight);

            textView.buildDrawingCache(true)
            val bitmap: Bitmap = getTransparentBitmapCopy(textView.drawingCache)
            textView.isDrawingCacheEnabled = false



//            val pngImage = encodeToBase64(bitmap)
            val pngImage = storeImage(bitmap)

            val textSizea = movableText.textSize.toInt()
            val bundle = Bundle()
            bundle.putString(ConstValFile.VideoText, pngImage)
            bundle.putString(ConstValFile.VideoTextSize,textSizea.toString())
            bundle.putString(ConstValFile.VideoTextColor,hexColor)
            bundle.putString(ConstValFile.TextAlignCmd,textAlignCmd)
            bundle.putString(ConstValFile.VideoTextXpos,x_Pos.toString())
            bundle.putString(ConstValFile.VideoTextYpos,y_Pos.toString())

            bundle.putString(ConstValFile.CompileTask,ConstValFile.AddText)
            sendToCompilerActivity(bundle)
        }






        movableText.visibility  = View.GONE
        viewBinding.videoExoView.player = videoPlayer
        val videoUri = sessionManager.getCreateVideoPath()
       isFromContest = sessionManager.getIsFromContest()
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
                    ExoPlayer.STATE_BUFFERING -> { }
                    ExoPlayer.STATE_READY -> {  }
                    else -> {}
                }
            }
        })

        viewBinding.sendToUploadBtn.setOnClickListener {
            videoPlayer.pause()
            videoPlayer.stop()
            sendToUploadVideoActivity()
        }

        super.onResume()
    }

    private fun colorPicker() {
        // Black Color -16777216
        // White Color -1
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose Color")
            .initialColor(-1)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setOnColorSelectedListener { selectedColor ->  viewBinding.videoEtText.setTextColor(selectedColor) }
            .setPositiveButton(
                "ok"
            ) { d, lastSelectedColor, allColors ->
                viewBinding.videoEtText.setTextColor(lastSelectedColor)
                Log.d("check", "onClick: lastSelectedColor : $lastSelectedColor")
                textColor = lastSelectedColor
                hexColor = String.format("#%06X", 0xFFFFFF and lastSelectedColor)
                Log.d("check", "onClick: lastSelectedColor : $hexColor")
            }
            .setNegativeButton(
                "cancel"
            ) { dialog, which -> dialog.dismiss() }
            .build()
            .show()
    }

    private fun viewTransformation(view: View, event: MotionEvent) {


        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY
                start[event.x] = event.y
                isOutSide = false
                mode =DRAG
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode =ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }
            MotionEvent.ACTION_UP -> {
                isZoomAndRotate = false
                if (mode ==DRAG) {
                    val x = event.x
                    val y = event.y
                }
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mode =NONE
                lastEvent = null
            }
            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                if (mode ==DRAG) {
                    isZoomAndRotate = false
                     x_Pos = (event.rawX + xCoOrdinate).toInt()
                    y_Pos = (event.rawY + yCoOrdinate).toInt()
                    view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate)
                        .setDuration(0).start()
                }
                if (mode ==ZOOM && event.pointerCount == 2) {
                    val newDist1: Float = spacing(event)
                    if (newDist1 > 10f) {
                        val scale = newDist1 / oldDist * view.scaleX
                        view.scaleX = scale
                        view.scaleY = scale
                    }
                    if (lastEvent != null) {
                        newRot = rotation(event)
                        view.rotation = (view.rotation + (newRot - d))
                    }
                }
                Log.d(
                    "check",
                    "viewTransformation: Position Text : X : $x_Pos // Y : $y_Pos"
                )
            }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toInt().toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    private fun getTransparentBitmapCopy(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val copy = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        copy.setPixels(pixels, 0, width, 0, 0, width, height)
        return copy
    }
    fun encodeToBase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        Log.e("LOOK", imageEncoded)
        return imageEncoded
    }

    private fun storeImage(image: Bitmap):String {
        val pictureFile = createFileAndFolder()
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: " + e.message)
        }
        return pictureFile
    }

    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.png"
        val appData = getExternalFilesDir(null)

        val createFile = File(appData,filename)
        if (!(createFile.exists())){
            try {
                createFile.createNewFile()
            }catch (i: IOException){
                i.printStackTrace()
            }
        }

        return createFile.absolutePath

    }
}