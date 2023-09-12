package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.util.UnstableApi

import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.VideoData
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityCollectionBinding
import com.img.audition.databinding.ActivityCommanVideoPlayBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

@UnstableApi
class CollectionActivity : AppCompatActivity() {

    private var videoItemAdapter: VideoItemAdapter? = null
    private lateinit var videoData: ArrayList<VideoData>
    private val TAG = "CollectionActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCollectionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        viewBinding.shimmerVideoView.startShimmer()
        getSavedVideos()
    }

    private fun getSavedVideos() {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val getUserVideoReq = apiInterface.getSavedVideo(SessionManager(this@CollectionActivity).getToken())

        getUserVideoReq.enqueue( object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    videoData = response.body()!!.data
                    if (videoData.size>0){
                        videoItemAdapter = VideoItemAdapter(this@CollectionActivity,videoData)
                        viewBinding.userVideoRecycle.adapter = videoItemAdapter
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.shimmerVideoView.visibility = View.GONE
                        viewBinding.userVideoRecycle.visibility = View.VISIBLE

                    }else{
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.noVideo.visibility = View.VISIBLE
                    }
                }else{
                    Log.d(TAG, "Get Other User Video Response Failed ${response.code()}")
                }
            }
            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    override fun onStop() {
        try {
            videoData.clear()
            videoItemAdapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }

        super.onStop()
    }
}