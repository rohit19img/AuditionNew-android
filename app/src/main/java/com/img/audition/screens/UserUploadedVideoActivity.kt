package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.img.audition.adapters.UserUploadVideoItemAdapter
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.SongVideoData
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.ActivitySongsVideoBinding
import com.img.audition.databinding.ActivityUserUploadedVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory

@UnstableApi class UserUploadedVideoActivity : AppCompatActivity() {

    private  var videoItemAdapter: UserUploadVideoItemAdapter?= null
    private lateinit var videoData: ArrayList<VideoData>
    private val TAG = "UserUploadedVideoActivity"

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityUserUploadedVideoBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@UserUploadedVideoActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@UserUploadedVideoActivity)
    }
    private val apiInterface by lazy {
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        if (myApplication.isNetworkConnected()){
            getUserVideo()
        }else{
            checkInternetDialog()
        }

        viewBinding.uploadVideo.setOnClickListener {
            for (dd in videoData){
                if (dd.isSelected){
                    Log.d("video url", "onCreate ->onClick : ${dd.file}")
                    sessionManager.setCreateVideoPath(dd.file.toString())
                    sessionManager.setAppSongID(dd.songId.toString())
                    sessionManager.setAudioDuration(111)
                    val intent = Intent(this@UserUploadedVideoActivity,UploadVideoActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.d(TAG, "onCreate: Select Video First..")
//                    Toast.makeText(this,"Select Video First..",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun getUserVideo(){
        if (myApplication.isNetworkConnected()){
            mainViewModel.getUserVideo(sessionManager.getUserSelfID()!!)
                .observe(this){
                    it.let {videoResponse->
                        when(videoResponse.status){
                            Status.SUCCESS ->{
                                if (videoResponse.data?.success!!){
                                    videoData = videoResponse.data.data
                                    if (videoData.size > 0) {
                                        videoItemAdapter = UserUploadVideoItemAdapter(this@UserUploadedVideoActivity, videoData)
                                        viewBinding.userVideoRecycle.adapter = videoItemAdapter
                                        viewBinding.shimmerVideoView.stopShimmer()
                                        viewBinding.shimmerVideoView.hideShimmer()
                                        viewBinding.shimmerVideoView.visibility = View.GONE
                                        viewBinding.userVideoRecycle.visibility = View.VISIBLE
                                    } else {
                                        Log.d(TAG, "No Video Data")
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
                                if (videoResponse.message!!.contains("401")){
                                    sessionManager.clearLogoutSession()
                                    startActivity(Intent(this@UserUploadedVideoActivity, SplashActivity::class.java))
                                    finishAffinity()
                                }
                                Log.e(TAG, videoResponse.status.toString())
                                Log.e(TAG, videoResponse.message.toString())
                            }
                        }
                    }
                }
        }else{
            checkInternetDialog()
        }

    }

    private fun checkInternetDialog() {
        val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            getUserVideo()
        }
        sweetAlertDialog.show()
    }
}