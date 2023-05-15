package com.img.audition.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.ActivitySharedVideoPlayBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager

@UnstableApi
class SharedVideoPlayActivity : AppCompatActivity() {

    val TAG = "SharedVideoPlayActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySharedVideoPlayBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val uri = intent.data
        if (uri != null) {
            if (uri.pathSegments != null && uri.pathSegments.size != 0) {
                val parameters = uri.pathSegments
                when (parameters[0]) {
                    "video" -> {
                        val videoData = parameters[1] as ArrayList<VideoData>
                        val adapter = VideoAdapter(this@SharedVideoPlayActivity, videoData)
                        viewBinding.videoCycle.adapter = adapter
                    }
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri: Uri? = intent.data
        if (uri != null) {
            if (uri.pathSegments != null && uri.getPathSegments().size !== 0) {
                val parameters: List<String> = uri.pathSegments
                when (parameters[0]) {
                    "video" -> {
                        val videoData = parameters[1] as ArrayList<VideoData>
                        val adapter = VideoAdapter(this@SharedVideoPlayActivity, videoData)
                        viewBinding.videoCycle.adapter = adapter
                    }
                }
            }

        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@SharedVideoPlayActivity, SplashActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}