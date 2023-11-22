package com.img.audition.screens

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.CursorLoader
import androidx.media3.common.util.UnstableApi
import com.img.audition.databinding.ActivityVideoTrimBinding
import com.img.audition.network.SessionManager
import com.img.audition.snapCameraKit.SnapPreviewActivity
import life.knowledge4.videotrimmer.interfaces.OnK4LVideoListener
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener
import java.io.File
import java.io.IOException

@UnstableApi
class VideoTrimActivity : AppCompatActivity(), OnTrimVideoListener {

    private val TAG = "VideoTrimActivity"
    private val binding by lazy {
        ActivityVideoTrimBinding.inflate(layoutInflater)
    }
    private val session by lazy {
        SessionManager(this@VideoTrimActivity)
    }
    lateinit var progressDialog: ProgressDialog

    private var maxVideoDuration: Long = 35000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog =  ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)

        binding.videoTrimmer.apply {
            setVideoURI(Uri.parse(session.getCreateVideoPath()))
            setDestinationPath(createFileAndFolder())
            setVideoInformationVisibility(true)
            setOnTrimVideoListener(this@VideoTrimActivity)
            setMaxDuration(35)
        }
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
                Log.e(TAG, "createFileAndFolder: $i")
            }
        }

        return createFile.absolutePath

    }

    override fun onTrimStarted() {
        progressDialog.show()
        Log.d(TAG, "onTrimStarted: ")
    }

    override fun getResult(uri: Uri?) {
        progressDialog.dismiss()
        Log.d(TAG, "getResult: $uri")

        val videoUri: Uri? = uri
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@VideoTrimActivity, videoUri)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val selectVideoDuration = time!!.toLong()

        Log.i("videoUri","Path : "+videoUri!!.path.toString())
        Log.i("videoUri","Time : $time")

        if (selectVideoDuration <= maxVideoDuration) {
            session.setCreateVideoPath(uri.path)
            val intent = Intent(this,SnapPreviewActivity::class.java)
            startActivity(intent)
        } else {
            runOnUiThread {
                Toast.makeText(this@VideoTrimActivity,"Please select ${maxVideoDuration / 1000} second video",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun cancelAction() {
        Log.d(TAG, "cancelAction: ")
        finish()
    }

    override fun onError(message: String?) {
        progressDialog.dismiss()
        Log.e(TAG, "onError: $message")
    }



}
