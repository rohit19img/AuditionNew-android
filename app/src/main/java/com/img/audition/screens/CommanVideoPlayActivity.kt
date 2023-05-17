package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.RecyclerView
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi class CommanVideoPlayActivity : AppCompatActivity() {
    val TAG = "CommanVideoPlayActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCommanVideoPlayBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@CommanVideoPlayActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@CommanVideoPlayActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    lateinit var videoItemPlayPause: VideoItemPlayPause

     private val apiInterface by lazy{
         RetrofitClient.getInstance().create(ApiInterface::class.java)
     }


     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (bundle!=null){
            if (bundle!!.getBoolean(ConstValFile.IsFromContest)){
                val contestID = bundle!!.getString(ConstValFile.ContestID).toString()
                val userID = bundle!!.getString(ConstValFile.USER_ID).toString()
                getContestVideo(userID,contestID)
            }else{
                viewBinding.videoViewpager2.offscreenPageLimit = 2
                val videoList = bundle!!.getSerializable(ConstValFile.VideoList) as ArrayList<VideoData>
                val videoPos = bundle!!.getInt(ConstValFile.VideoPosition)
                for (da in videoList){
                    myApplication.printLogD(da.usersLike.toString(),"like common play")
                    myApplication.printLogD(da.auditionId.toString(),"userid Data common play")
                    myApplication.printLogD(da.caption.toString(),"caption Data common play")
                }
                val videoAdapter = VideoAdapter(this@CommanVideoPlayActivity,videoList)
                viewBinding.videoViewpager2.adapter = videoAdapter
                viewBinding.videoViewpager2.currentItem = videoPos

                videoItemPlayPause = videoAdapter.onActivityStateChanged()
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
            myApplication.printLogE(e.message.toString(),TAG)
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
            myApplication.printLogE(e.message.toString(),TAG)
        }
        super.onPause()
    }

    override fun onResume() {
        try {
            val cPos =  viewBinding.videoViewpager2.currentItem
            val holder: VideoAdapter.VideoViewHolder = ( viewBinding.videoViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onResume(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG)
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
             myApplication.printLogE(e.message.toString(),TAG)
         }
         super.onStop()
     }

     private fun getContestVideo(userID: String,contestID:String) {
         val getUserVideoReq = apiInterface.getContestVideo(sessionManager.getToken(),userID,contestID)

         getUserVideoReq.enqueue( object : Callback<VideoResponse> {
             override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                 if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                     val videoData = response.body()!!.data
                     if (videoData.size>0){
                         val videoAdapter = VideoAdapter(this@CommanVideoPlayActivity,videoData)
                         viewBinding.videoViewpager2.adapter = videoAdapter
                         videoItemPlayPause = videoAdapter.onActivityStateChanged()
                     }else{
                         myApplication.printLogD("No Video Data",TAG)
                     }
                 }else{
                     myApplication.showToast(response.body()!!.message!!)
                     onBackPressed()
                     myApplication.printLogE("Get Video Response Failed ${response.code()}",TAG)
                 }
             }
             override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                 myApplication.printLogE("Get Video onFailure ${t.toString()}",TAG)
             }

         })
     }
}