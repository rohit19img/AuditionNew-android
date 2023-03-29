package com.img.audition.screens

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.LogCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService


@UnstableApi class UploadVideoActivity : AppCompatActivity() {

    val TAG = "PreviewActivity"
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

    private var orignalPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (bundle!=null){
            orignalPath = bundle!!.getString(ConstValFile.VideoFilePath).toString()
            Glide.with(this@UploadVideoActivity).load(orignalPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(viewBinding.videoThumbnail)
        }else{
            myApplication.printLogD("File Path Null",TAG)
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.uploadVideoBtn.setOnClickListener {
            if(!(sessionManager.isUserLoggedIn())){
                sendToLoginActivity()
            }else{
                compressVideo()
                myApplication.showToast(orignalPath)
            }
        }
    }

    private fun sendToLoginActivity() {
        val intent = Intent(this@UploadVideoActivity,LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this@UploadVideoActivity)
        dialogBuilder.setTitle("Discard Video")
        dialogBuilder.setMessage("Do you want discard the video")
            .setCancelable(false)
            .setPositiveButton("Discard", DialogInterface.OnClickListener {
                    _, _ -> sendToMain()
            })
            .setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.show()
//        super.onBackPressed()
    }

    fun sendToMain(){
        val intent = Intent(this@UploadVideoActivity,HomeActivity::class.java)
        intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun getNewFilePath():String{
        val fileRandomName  = System.currentTimeMillis() / 1000
        val fileName =  fileRandomName.toString()+ConstValFile.VideoFileExt
        val newPath = File(getPackageLocation()!!,fileName)
        myApplication.printLogD(newPath.absolutePath,"New Path")
        if (!(newPath.exists())){
            try {
                newPath.createNewFile()
            } catch (e: IOException) {
                myApplication.printLogE(e.toString(),TAG)
            }
        }
        return newPath.absolutePath
    }


    private fun compressVideo() {
        val inputPath = orignalPath
        val outputPath = getNewFilePath()
        val compressCMD = "-y -i $inputPath -vcodec libx264 -crf 22 $outputPath"

        FFmpegKit.executeAsync(compressCMD
        ) { session ->
            val state = session!!.state
            val returnCode = session.returnCode
            // CALLED WHEN SESSION IS EXECUTED
            if (returnCode.isValueSuccess.equals(session.returnCode)){
                myApplication.showToast("Compress Completed..")
            }else if (returnCode.isValueCancel.equals(session.returnCode)){
                myApplication.printLogD("Process Cancel",TAG)
            }else if (returnCode.isValueError.equals(session.returnCode)){
                myApplication.printLogE("$state $returnCode",TAG)
            }
            myApplication.printLogD(
                java.lang.String.format(
                    "FFmpeg process exited with state %s and returnCode %s.%s",
                    state,
                    returnCode,
                    session!!.failStackTrace
                ), TAG
            )
        }
    }

    fun getPackageLocation(): String? {
        val m = packageManager
        var packageLocation = packageName
        try {
            val p: PackageInfo = m.getPackageInfo(packageLocation, 0)
            packageLocation = p.applicationInfo.dataDir
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("check", "Error Package name not found ", e)
        }
        return packageLocation
    }
}