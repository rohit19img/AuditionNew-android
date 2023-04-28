package com.img.audition.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.SongVideoResponse
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityHashtagVideoBinding
import com.img.audition.databinding.ActivitySongsVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.snapCameraKit.SnapCameraActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongsVideoActivity : AppCompatActivity() {

    val TAG = "SongsVideoActivity"

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySongsVideoBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@SongsVideoActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@SongsVideoActivity)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    val mediaPlayer = MediaPlayer()
    val songHandler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getMusicVideo(musicId:String) {
        val userVideoReq = apiInterface.getMusicVideo(sessionManager.getToken(),musicId)
        userVideoReq.enqueue( object : Callback<SongVideoResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<SongVideoResponse>, response: Response<SongVideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val videoData = response.body()!!.data!!.videos
                    val creatorDetails = response.body()!!.data!!.musicDetails
                    val musicDetails = response.body()!!.data!!
                    if (videoData.size>0) {
                        val videoItemAdapter = VideoItemAdapter(this@SongsVideoActivity, videoData)
                        viewBinding.userVideoRecycle.adapter = videoItemAdapter
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.shimmerVideoView.visibility = View.GONE
                        viewBinding.userVideoRecycle.visibility = View.VISIBLE

                        val musicUrl = ConstValFile.BASEURL + musicDetails.trackAacFormat.toString()

                        sessionManager.setVideoSongUrl(musicUrl)
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(musicUrl)
                        mediaPlayer.setOnPreparedListener {
                            // Set max value for SeekBar based on audio file duration
                            viewBinding.seekBar.max = mediaPlayer.duration
                            // Start updating SeekBar progress
                            songHandler.postDelayed(updateSeekBar, 1000)
                        }

                        mediaPlayer.prepareAsync()
                        val creatorName = creatorDetails!!.auditionId.toString()

                        viewBinding.soundName.text = "Original-creator-$creatorName"
                    }else{
                        myApplication.printLogD("No Video Data",TAG)
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.noVideo.visibility = View.VISIBLE
                    }
                }else{
                    myApplication.printLogE("Get Music Video Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<SongVideoResponse>, t: Throwable) {
                myApplication.printLogE("Get Music Video onFailure ${t.toString()}",TAG)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewBinding.shimmerVideoView.startShimmer()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.reset()
        val musicId = bundle!!.getString(ConstValFile.SongID)
        sessionManager.setVideoSongID(musicId)
        myApplication.printLogD("${sessionManager.getVideoSongID().toString()} ","videoSongID")
        getMusicVideo(musicId!!)

        viewBinding.createVideo.setOnClickListener {
            sendForCreateVideo(musicId)
        }

        viewBinding.playPauseButton.setOnClickListener {
           if (mediaPlayer!=null){
              if (mediaPlayer.isPlaying){
                  mediaPlayer.pause()
                  viewBinding.playPauseButton.setImageResource(R.drawable.play_ic)
              }else{
                  mediaPlayer.start()
                  viewBinding.playPauseButton.setImageResource(R.drawable.pause_ic)
              }
           }
        }
    }

    private fun sendForCreateVideo(songID: String) {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest,false)
        bundle.putString(ConstValFile.SongID,songID)
        sessionManager.setVideoSongID(songID)
        val intent = Intent(this@SongsVideoActivity, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            viewBinding.seekBar.progress = mediaPlayer.currentPosition
            songHandler.postDelayed(this, 1000)
        }
    }
}