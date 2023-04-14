package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext

import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
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

class CollectionActivity : AppCompatActivity() {

    val TAG = "CommanVideoPlayActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCollectionBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@CollectionActivity)
    }
    private val myApplication by lazy {
        MyApplication(this@CollectionActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
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
        val getUserVideoReq = apiInterface.getSavedVideo(sessionManager.getToken())

        getUserVideoReq.enqueue( object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val videoData = response.body()!!.data
                    if (videoData.size>0){
                        val videoItemAdapter = VideoItemAdapter(this@CollectionActivity,videoData)
                        viewBinding.userVideoRecycle.adapter = videoItemAdapter
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.shimmerVideoView.visibility = View.GONE
                        viewBinding.userVideoRecycle.visibility = View.VISIBLE

                    }else{
                        myApplication.printLogD("No Video Data",TAG)
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.noVideo.visibility = View.VISIBLE
                    }
                }else{
                    myApplication.printLogE("Get Other User Video Response Failed ${response.code()}",TAG)
                }
            }
            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Video onFailure ${t.toString()}",TAG)
            }

        })
    }
}