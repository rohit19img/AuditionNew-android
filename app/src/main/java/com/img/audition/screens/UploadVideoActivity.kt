package com.img.audition.screens

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.File
import java.io.IOException


@UnstableApi
class UploadVideoActivity : AppCompatActivity() {

    val TAG = "UploadVideoActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityUploadVideoBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@UploadVideoActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@UploadVideoActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    private var orignalPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (bundle != null) {
            val videoUri = bundle!!.getString(ConstValFile.VideoFilePath).toString()
            myApplication.printLogD(videoUri, "videoUri")
            orignalPath = getOriginalPathFromUri(this@UploadVideoActivity, Uri.parse(videoUri))
            myApplication.printLogD(orignalPath, "videoPath")
            Glide.with(this@UploadVideoActivity).load(orignalPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(viewBinding.videoThumbnail)
        } else {
            myApplication.printLogD("File Path Null", TAG)
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.uploadVideoBtn.setOnClickListener {
            if (!(sessionManager.isUserLoggedIn())) {
                sendToLoginActivity()
            } else {
                val outputPath = createFileAndFolder()

            }
        }
    }


    private fun sendToLoginActivity() {
        val intent = Intent(this@UploadVideoActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this@UploadVideoActivity)
        dialogBuilder.setTitle("Discard Video")
        dialogBuilder.setMessage("Do you want discard the video")
            .setCancelable(false)
            .setPositiveButton("Discard", DialogInterface.OnClickListener { _, _ ->
                sendToMain()
            })
            .setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.show()
    }

    fun sendToMain() {
        val intent = Intent(this@UploadVideoActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun getOriginalPathFromUri(context: Context, uri: Uri): String {
        var filePath = ""
        val scheme = uri.scheme
        if (scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
                cursor.close()
            }
        } else if (scheme == ContentResolver.SCHEME_FILE) {
            filePath = uri.path!!
        }
        return filePath
    }



    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
        val appData = Environment.getExternalStorageDirectory()
        myApplication.printLogD(appData.absolutePath,TAG)

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

}
