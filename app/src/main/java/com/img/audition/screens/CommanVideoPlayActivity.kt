package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.VideoAdapter
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.VideoData
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityCommanVideoPlayBinding
import com.img.audition.databinding.ActivityOtherUserProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoItemPlayPause
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi class CommanVideoPlayActivity : AppCompatActivity() {
    private lateinit var videoData: java.util.ArrayList<VideoData>
    private var videoAdapter: VideoAdapter? = null
    private lateinit var videoList: java.util.ArrayList<VideoData>
    private val TAG = "CommanVideoPlayActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCommanVideoPlayBinding.inflate(layoutInflater)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    private lateinit var videoItemPlayPause: VideoItemPlayPause

    private lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(SessionManager(this@CommanVideoPlayActivity).getToken(),apiInterface))[MainViewModel::class.java]

        if (bundle!=null){
            if (bundle!!.getBoolean(ConstValFile.IsFromContest)){
                val contestID = bundle!!.getString(ConstValFile.ContestID).toString()
                val userID = bundle!!.getString(ConstValFile.USER_ID).toString()
                getContestVideo(userID,contestID)
            }else{
                viewBinding.videoViewpager2.offscreenPageLimit = 2
                videoList = bundle!!.getSerializable(ConstValFile.VideoList) as ArrayList<VideoData>
                val videoPos = bundle!!.getInt(ConstValFile.VideoPosition)
                videoAdapter = VideoAdapter(this@CommanVideoPlayActivity,videoList)
                viewBinding.videoViewpager2.adapter = videoAdapter
                viewBinding.videoViewpager2.currentItem = videoPos

                videoItemPlayPause = videoAdapter!!.onActivityStateChanged()
            }
        }
        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        Log.d(TRACK, "onStop: ")
        try {
            val cPos = viewBinding.videoViewpager2.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewBinding.videoViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:Exception){
           e.printStackTrace()
        }

        try {
            videoAdapter = null
            videoData.clear()
            videoList.clear()
        }catch (e:Exception){
            e.printStackTrace()
        }
        super.onBackPressed()
    }

    override fun onPause() {
        Log.d(TRACK, "onStop: ")
        try {
            val cPos = viewBinding.videoViewpager2.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewBinding.videoViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:Exception){
           e.printStackTrace()
        }
        super.onPause()
    }

    override fun onResume() {
        try {
            val cPos =  viewBinding.videoViewpager2.currentItem
            val holder: VideoAdapter.VideoViewHolder = ( viewBinding.videoViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onResume(holder,cPos)
        }catch (e:Exception){
            e.printStackTrace()
        }
        Log.d(TRACK, "onResume: ")
        super.onResume()
    }


     override fun onStop() {
         Log.d(TRACK, "onStop: ")
         try {
             val cPos = viewBinding.videoViewpager2.currentItem
             val holder: VideoAdapter.VideoViewHolder = (viewBinding.videoViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
             videoItemPlayPause.onStop(holder,cPos)
         }catch (e:Exception){
             e.printStackTrace()
         }


         super.onStop()
     }

    private fun getContestVideo(userID: String,contestID:String){
        Log.i("token",userID)
        mainViewModel.getContestVideo(userID,contestID)
            .observe(this){
                it.let {videoResponse->
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            if (videoResponse.data?.success!!){
                                videoData = videoResponse.data.data
                                if (videoData.size>0){
                                    videoAdapter = VideoAdapter(this@CommanVideoPlayActivity,videoData)
                                    viewBinding.videoViewpager2.adapter = videoAdapter
                                    videoItemPlayPause = videoAdapter!!.onActivityStateChanged()
                                }else{
                                   Toast.makeText(this,videoResponse.data.message!!,Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        Status.LOADING ->{
                            Log.e(TAG, "onResponse: ${videoResponse.status.toString()}")
                        }
                        else->{
                            Toast.makeText(this,videoResponse.data!!.message!!,Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                    }
                }
            }
    }

}