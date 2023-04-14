package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext

import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.ActivityCommanVideoPlayBinding
import com.img.audition.databinding.ActivityOtherUserProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoItemPlayPause

 class CommanVideoPlayActivity : AppCompatActivity() {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (bundle!=null){

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
}