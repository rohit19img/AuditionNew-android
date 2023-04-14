package com.img.audition.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.img.audition.databinding.ActivityPreviewBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.File
import kotlin.math.sqrt


class PreviewActivity : AppCompatActivity() {

    //For Movable text
    var lastEvent: FloatArray? = null
    var d = 0f
    var newRot = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    var oldDist = 1f
    private var xCoOrdinate = 0f
    private  var yCoOrdinate = 0f

    //end of Movable text

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

    lateinit var videoPlayer : ExoPlayer

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

        viewBinding.addTextBtn.setOnClickListener {
            if (viewBinding.videoEtLyout.visibility == View.VISIBLE){
                viewBinding.videoEtLyout.visibility = View.GONE
            }else{
                viewBinding.videoEtLyout.visibility = View.VISIBLE
            }
        }

        viewBinding.colorPicker.setOnClickListener {
            colorPicker()
        }

        viewBinding.textSizeBtn.setOnClickListener {
            if (viewBinding.textSizePicker.visibility == View.VISIBLE){
                viewBinding.textSizePicker.visibility = View.GONE
            }else{
                viewBinding.textSizePicker.visibility = View.VISIBLE
            }
        }

        viewBinding.textSizePicker.maxValue = 44
        viewBinding.textSizePicker.minValue = 10
        viewBinding.textSizePicker.value = 16
        viewBinding.textSizePicker.setOnValueChangedListener { _, _, newVal ->
            viewBinding.videoEtText.textSize = newVal.toFloat()
            val videoTextSize = newVal
        }

        viewBinding.movableText.setOnTouchListener(OnTouchListener { view, motionEvent ->
            val moveText = view as TextView
            moveText.bringToFront()
            viewTransformation(moveText, motionEvent)
            true
        })
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
            if (File(sessionManager.getCreateVideoPath()!!).exists()){
                File(sessionManager.getCreateVideoPath()!!).delete()
                sessionManager.clearVideoSession()
            }
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

    override fun onResume() {

        viewBinding.videoExoView.player = videoPlayer
        val videoUri = sessionManager.getCreateVideoPath()
        val videoSpeedState = sessionManager.getCreateVideoSpeedState()
        val videoDuration = sessionManager.getCreateVideoDuration()
        myApplication.printLogD("videoUri : $videoUri videoState: $videoSpeedState videoDuration: $videoDuration",TAG)
        isFromContest = sessionManager.getIsFromContest()
        myApplication.printLogD("$isFromContest onCreate", " isFromContest $TAG")
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
                val txtColor = lastSelectedColor
                val hexColor = String.format("#%06X", 0xFFFFFF and lastSelectedColor)
                Log.d("check", "onClick: lastSelectedColor : $hexColor")
            }
            .setNegativeButton(
                "cancel"
            ) { dialog, which -> dialog.dismiss() }
            .build()
            .show()
    }

    private fun viewTransformation(view: View, event: MotionEvent) {
        var x_Pos = 0
        var y_Pos = 0
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
}