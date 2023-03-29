package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.util.UnstableApi
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (bundle!=null){

            val videoList = bundle!!.getSerializable(ConstValFile.VideoList) as ArrayList<VideoData>
            val videoPos = bundle!!.getInt(ConstValFile.VideoPosition)
            val videoAdapter = VideoAdapter(this@CommanVideoPlayActivity,videoList)
            viewBinding.videoViewpager2.adapter = videoAdapter
            viewBinding.videoViewpager2.currentItem = videoPos
            videoItemPlayPause = videoAdapter.onActivityStateChanged()
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