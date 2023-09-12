package com.img.audition.screens


import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.SongVideoData
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.ActivitySongsVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.snapCameraKit.SnapCameraActivity
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory

@UnstableApi
class TryAudioActivity : AppCompatActivity() {

    private  var videoItemAdapter: VideoItemAdapter?= null
    private lateinit var videoData: ArrayList<VideoData>
    private  var musicDetails: SongVideoData? = null
    private val TAG = "TryAudioActivity"

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

    private val mediaPlayer = MediaPlayer()
    private val songHandler = Handler()
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]


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
        mediaPlayer.setDataSource(APITags.ADMINBASEURL+songUrl)
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
        bundle.putBoolean(ConstValFile.isFromDuet, false)
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

    private fun getMusicVideo(musicID:String){
        mainViewModel.getMusicVideo(musicID)
            .observe(this){
                it.let {videoResponse->
                    myApplication.printLogD(videoResponse.message.toString(),"apiCall 1")
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            myApplication.printLogD(videoResponse.data!!.message.toString(),"apiCall 2")
                            if (videoResponse.data.success!!){
                                 videoData = videoResponse.data.data!!.videos
                                 musicDetails = videoResponse.data.data!!
                                if (videoData.size>0) {
                                     videoItemAdapter = VideoItemAdapter(this@TryAudioActivity, videoData)
                                    viewBinding.userVideoRecycle.adapter = videoItemAdapter
                                    viewBinding.shimmerVideoView.stopShimmer()
                                    viewBinding.shimmerVideoView.hideShimmer()
                                    viewBinding.shimmerVideoView.visibility = View.GONE
                                    viewBinding.userVideoRecycle.visibility = View.VISIBLE

                                    val audioImage = APITags.ADMINBASEURL+musicDetails!!.Image.toString()
                                    val title = musicDetails?.title.toString()
                                    val subTitle = musicDetails?.subtitle.toString()
                                    Glide.with(this@TryAudioActivity).load(audioImage).placeholder(R.drawable.music_ic).into(viewBinding.audiImage)
                                    viewBinding.title.text = title
                                    viewBinding.subTitle.text = subTitle
                                }else{
                                    myApplication.printLogD("No Video Data",TAG)
                                    viewBinding.shimmerVideoView.stopShimmer()
                                    viewBinding.shimmerVideoView.hideShimmer()
                                    viewBinding.shimmerVideoView.visibility = View.GONE
                                    viewBinding.noVideo.visibility = View.VISIBLE
                                }
                            }
                        }
                        Status.LOADING ->{
                            myApplication.printLogD(videoResponse.status.toString(),"apiCall 3")
                        }
                        else->{
                            if (videoResponse.message!!.contains("401")){
                                myApplication.printLogD(videoResponse.message.toString(),"apiCall 4")
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this, SplashActivity::class.java))
                                finishAffinity()
                            }
                            myApplication.printLogD(videoResponse.status.toString(),"apiCall 5")
                        }
                    }
                }
            }
    }

    override fun onPause() {
        mediaPlayer.pause()
        viewBinding.playPauseButton.setImageResource(R.drawable.play_ic)
        songHandler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onStop() {
        try {
            videoItemAdapter = null
            videoData.clear()
            musicDetails = null
            songHandler.removeCallbacksAndMessages(null)
        }catch (e:Exception){
            e.printStackTrace()
        }

        super.onStop()
    }
}