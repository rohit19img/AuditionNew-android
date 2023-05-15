package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityHashtagVideoBinding
import com.img.audition.databinding.ActivityPostLocationVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostLocationVideoActivity : AppCompatActivity() {

    val TAG = "PostLocationVideoActivity"

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPostLocationVideoBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@PostLocationVideoActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@PostLocationVideoActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        viewBinding.shimmerVideoView.startShimmer()
    }

    override fun onResume() {
        super.onResume()
        val postLocation = bundle!!.getString(ConstValFile.PostLocation)
        myApplication.printLogD("${sessionManager.getVideoHashTag().toString()} HaAc","videoHashTag")
        viewBinding.postLocation.text = postLocation.toString()
        getPostLocationVideo(postLocation!!)
    }


    private fun getPostLocationVideo(postLocation:String) {
        val userVideoReq = apiInterface.getPostLocationVideo(sessionManager.getToken(),postLocation)
        userVideoReq.enqueue( object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val videoData = response.body()!!.data
                    if (videoData.size>0) {
                        val videoItemAdapter = VideoItemAdapter(this@PostLocationVideoActivity, videoData)
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
                    myApplication.printLogE("Get Other User Self Video Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Self Video onFailure ${t.toString()}",TAG)
            }
        })
    }
}