package com.img.audition.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.ActivitySharedVideoPlayBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoItemPlayPause
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import java.io.File

@UnstableApi
class SharedVideoPlayActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySharedVideoPlayBinding.inflate(layoutInflater)
    }
    private val myApplication by lazy {
        MyApplication(this@SharedVideoPlayActivity)
    }
    private lateinit var videoItemPlayPause: VideoItemPlayPause
    private lateinit var mainViewModel: MainViewModel
    var videoAdapter: VideoAdapter? = null
    private var videoList : ArrayList<VideoData> = ArrayList()
    private val sessionManager by lazy {
        SessionManager(this@SharedVideoPlayActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(this,
            ViewModelFactory(sessionManager.getToken(),apiInterface)
        )[MainViewModel::class.java]

        viewBinding.showVideoShimmer.visibility = View.VISIBLE
        viewBinding.showVideoShimmer.startShimmer()

        val uri: Uri? = intent.data
        if (uri != null) {
            if (uri.pathSegments != null && uri.getPathSegments().size !== 0) {
                val parameters: List<String> = uri.pathSegments
                when (parameters[0]) {
                    "video" -> {
                        val videoID = parameters[1] as String
                        Log.d("videoID ", "onCreate: From Deep Link : $videoID")
                        /*val adapter = VideoAdapter(this@SharedVideoPlayActivity, videoData)
                        viewBinding.videoCycle.adapter = adapter*/

                        getVideoByVideoID(videoID)
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
                        val videoID = parameters[1] as String
                        Log.d("videoID ", "onNewIntent:  From Deep Link : $videoID")
//                        getVideoByVideoID(videoID)
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

    private fun getVideoByVideoID(videoID:String){

        Log.d("check100", "getVideoByVideoID: ")
        mainViewModel.getVideoByID(videoID)
            .observe(this@SharedVideoPlayActivity){
                it.let {videoResponse->
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            if (videoResponse.data?.success!!){

                                val videoData = videoResponse.data.data
                                Log.d("check100", "getVideoByVideoID: $videoData")
                                videoAdapter = VideoAdapter(this@SharedVideoPlayActivity, videoData)
                                viewBinding.showVideoShimmer.visibility = View.GONE
                                viewBinding.showVideoShimmer.stopShimmer()
                                viewBinding.showVideoShimmer.hideShimmer()
                                viewBinding.videoCycle.adapter = videoAdapter
                                videoItemPlayPause = videoAdapter!!.onActivityStateChanged()
                            }
                        }
                        Status.LOADING ->{
                            Log.d("check100", "getDeepLinkVideo: ${videoResponse.status}")
                        }
                        else->{
                            if (videoResponse.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this@SharedVideoPlayActivity, SplashActivity::class.java))
                                finishAffinity()
                            }
                            Log.d("check100", "getDeepLinkVideo: ${videoResponse.message}")
                        }
                    }
                }
            }

    }

    override fun onPause() {
        try {
            val cPos = viewBinding.videoCycle.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewBinding.videoCycle.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),"check100")
        }
        super.onPause()
    }

    override fun onStop() {
        try {
            val cPos = viewBinding.videoCycle.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewBinding.videoCycle.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onStop(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),"check100")
        }
        super.onStop()

    }

    override fun onResume() {
        try {
            val cPos = viewBinding.videoCycle.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewBinding.videoCycle.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onResume(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),"check100")
        }
        super.onResume()
    }
}