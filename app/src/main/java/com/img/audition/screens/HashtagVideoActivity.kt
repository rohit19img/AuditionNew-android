package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.VideoData
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityEditProfileBinding
import com.img.audition.databinding.ActivityHashtagVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.snapCameraKit.SnapCameraActivity
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

@UnstableApi
class HashtagVideoActivity : AppCompatActivity() {

    private  var videoItemAdapter: VideoItemAdapter? = null
    private lateinit var videoData: ArrayList<VideoData>
    private val TAG = "HashtagVideoActivity"

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityHashtagVideoBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@HashtagVideoActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]


    }




    override fun onStart() {
        super.onStart()
        viewBinding.shimmerVideoView.startShimmer()
    }

    override fun onResume() {
        super.onResume()
        val hashTag = bundle!!.getString(ConstValFile.VideoHashTag)
        viewBinding.hashtagname.text = hashTag.toString()
        getHashTagVideo(hashTag!!)

        viewBinding.createVideo.setOnClickListener {
            sendForCreateVideo(hashTag)
        }
    }

    private fun sendForCreateVideo(hashTag: String) {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest,false)
        bundle.putBoolean(ConstValFile.isFromDuet, false)
        bundle.putString(ConstValFile.VideoHashTag,hashTag)
        sessionManager.setVideoHashTag(hashTag)
        val intent = Intent(this@HashtagVideoActivity, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    private fun getHashTagVideo(hashTag:String){

        mainViewModel.getHashTagVideo(hashTag)
            .observe(this){
                it.let {videoResponse->
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            if (videoResponse.data?.success!!){
                                 videoData = videoResponse.data.data
                                if (videoData.size>0) {
                                     videoItemAdapter = VideoItemAdapter(this@HashtagVideoActivity, videoData)
                                    viewBinding.userVideoRecycle.adapter = videoItemAdapter
                                    viewBinding.shimmerVideoView.stopShimmer()
                                    viewBinding.shimmerVideoView.hideShimmer()
                                    viewBinding.shimmerVideoView.visibility = View.GONE
                                    viewBinding.userVideoRecycle.visibility = View.VISIBLE
                                }else{
                                    viewBinding.shimmerVideoView.stopShimmer()
                                    viewBinding.shimmerVideoView.hideShimmer()
                                    viewBinding.shimmerVideoView.visibility = View.GONE
                                    viewBinding.noVideo.visibility = View.VISIBLE
                                }
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TAG, videoResponse.status.toString())
                        }
                        else->{
                            Log.d(TAG, videoResponse.status.toString())
                        }
                    }
                }
            }
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