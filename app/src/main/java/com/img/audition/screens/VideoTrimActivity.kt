package com.img.audition.screens

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
        session.setCreateVideoPath(uri?.path)
        progressDialog.dismiss()
        Log.d(TAG, "getResult: $uri")
        val intent = Intent(this,SnapPreviewActivity::class.java)
        startActivity(intent)
    }

    override fun cancelAction() {
        Log.d(TAG, "cancelAction: ")
        val intent = Intent(this,SnapPreviewActivity::class.java)
        startActivity(intent)
    }

    override fun onError(message: String?) {
        progressDialog.dismiss()
        Log.e(TAG, "onError: $message")
    }

}
