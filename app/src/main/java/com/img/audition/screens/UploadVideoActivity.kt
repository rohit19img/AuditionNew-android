package com.img.audition.screens

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager


@UnstableApi class UploadVideoActivity : AppCompatActivity() {

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

}