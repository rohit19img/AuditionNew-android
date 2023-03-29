package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.img.audition.R
import com.img.audition.databinding.ActivityPreviewBinding
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager

class UploadVideoActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }
}