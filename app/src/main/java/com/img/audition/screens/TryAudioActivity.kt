package com.img.audition.screens

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.SongVideoResponse
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
import java.io.File
import java.io.IOException

class TryAudioActivity : AppCompatActivity() {

    val TAG = "TryAudioActivity"

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivitySongsVideoBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@TryAudioActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@TryAudioActivity)
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

    override fun onStart() {
        super.onStart()
        viewBinding.shimmerVideoView.startShimmer()
    }

    override fun onResume() {
        super.onResume()
        val musicId = bundle!!.getString(ConstValFile.SongID)
        val songUrl = bundle!!.getString(ConstValFile.SongUrl)


        getMusicVideo(musicId!!)

        viewBinding.createVideo.setOnClickListener {
            if (mediaPlayer!=null && mediaPlayer.isPlaying){

                mediaPlayer.seekTo(0)
                mediaPlayer.stop()
            }
            sendForCreateVideo(musicId,songUrl!!)
        }

        mediaPlayer.reset()
        mediaPlayer.setDataSource(ConstValFile.BASEURL+songUrl)
        mediaPlayer.setOnPreparedListener {
            // Set max value for SeekBar based on audio file duration
            viewBinding.seekBar.max = mediaPlayer.duration

            sessionManager.setAudioDuration(mediaPlayer.duration)
            myApplication.printLogD("audioDuration ${mediaPlayer.duration}","audioDuration")
            viewBinding.totalDuration.text = getTimeString(mediaPlayer.duration.toLong())

            // Start updating SeekBar progress
            songHandler.postDelayed(updateSeekBar, 1000)

        }

        mediaPlayer.prepareAsync()

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

    private fun sendForCreateVideo(songID: String,songUrl:String) {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest,false)
        bundle.putString(ConstValFile.SongID,songID)
        sessionManager.setVideoSongID(songID)
        sessionManager.setVideoSongUrl(songUrl)
        sessionManager.setIsFromTryAudio(true)
        val intent = Intent(this@TryAudioActivity, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            viewBinding.seekBar.progress = mediaPlayer.currentPosition
            songHandler.postDelayed(this, 1000)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getTimeString(millis: Long): String? {
        val buf = StringBuffer()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        buf
            .append(String.format("%02d", minutes))
            .append(".")
            .append(String.format("%02d", seconds))
        return buf.toString()
    }

    private fun getMusicVideo(musicId:String) {
        val userVideoReq = apiInterface.getMusicVideo(sessionManager.getToken(),musicId)
        userVideoReq.enqueue( object : Callback<SongVideoResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<SongVideoResponse>, response: Response<SongVideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val videoData = response.body()!!.data!!.videos
                    val musicDetails = response.body()!!.data!!
                    if (videoData.size>0) {
                        val videoItemAdapter = VideoItemAdapter(this@TryAudioActivity, videoData)
                        viewBinding.userVideoRecycle.adapter = videoItemAdapter
                        viewBinding.shimmerVideoView.stopShimmer()
                        viewBinding.shimmerVideoView.hideShimmer()
                        viewBinding.shimmerVideoView.visibility = View.GONE
                        viewBinding.userVideoRecycle.visibility = View.VISIBLE

                        val audioImage = ConstValFile.BASEURL+musicDetails.Image.toString()
                        val title = musicDetails.title.toString()
                        val subTitle = musicDetails.subtitle.toString()
                        Glide.with(this@TryAudioActivity).load(audioImage).placeholder(R.drawable.music_ic).into(viewBinding.audiImage)
                        viewBinding.title.text = title
                        viewBinding.subTitle.text = subTitle
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

    override fun onPause() {
        mediaPlayer.pause()
        viewBinding.playPauseButton.setImageResource(R.drawable.play_ic)
        super.onPause()
    }
}