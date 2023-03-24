package com.img.audition.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.GetOtherUserResponse
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityOtherUserProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL


class OtherUserProfileActivity : AppCompatActivity() {

    val TAG = "OtherUserProfileActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityOtherUserProfileBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@OtherUserProfileActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@OtherUserProfileActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val manager = GridLayoutManager(this@OtherUserProfileActivity, 3)
        viewBinding.userVideoRecycle.layoutManager = manager

        viewBinding.l1.startShimmer()
        viewBinding.shimmerVideoView.startShimmer()



        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onStart() {
        val userID =  bundle!!.getString(ConstValFile.USER_IDFORIntent)
        val followStatus =  bundle!!.getBoolean(ConstValFile.UserFollowStatus,false)
        getUserData(userID,followStatus)

        getUserVideo(userID)
        super.onStart()
    }

    private fun getUserVideo(userID: String?) {
        val getUserVideoReq = apiInterface.getOtherUserVideo(sessionManager.getToken(),userID)

        getUserVideoReq.enqueue(@UnstableApi object : Callback<VideoResponse>{
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val videoData = response.body()!!.data
                    if (videoData.size>0){
                        val videoItemAdapter = VideoItemAdapter(this@OtherUserProfileActivity,videoData)
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
                    myApplication.printLogE("Get Other User Video Response Failed ${response.code()}",TAG)
                }
            }
            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Video onFailure ${t.toString()}",TAG)
            }

        })
    }

    private fun getUserData(userID: String?, followStatus: Boolean) {
        myApplication.printLogD(userID.toString(),"Other User ID")
        myApplication.printLogD(followStatus.toString(),"Other Follow Status")

        val getUserReq = apiInterface.getOtherUser(sessionManager.getToken(),userID)
        getUserReq.enqueue(object : Callback<GetOtherUserResponse>{
            override fun onResponse(call: Call<GetOtherUserResponse>, response: Response<GetOtherUserResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    viewBinding.l1.stopShimmer()
                    viewBinding.l1.hideShimmer()
                    val userData = response.body()!!.data[0]
                    viewBinding.likeCount.text = userData.totalLike.toString()
                    viewBinding.followCount.text = userData.followersCount.toString()
                    viewBinding.followingCount.text = userData.followingCount.toString()
                    if (userData.image.toString().isNotEmpty()){
                        Glide.with(this@OtherUserProfileActivity).load(userData.image.toString()).placeholder(R.drawable.person_ic).into(viewBinding.userImageView)
                    }else{
                      viewBinding.userImageView.setImageResource(R.drawable.person_ic)
                    }

                    if (userData.bio.toString().isNotEmpty()){
                        viewBinding.userBio.text = userData.bio.toString()
                    }else{
                        viewBinding.userBio.visibility = View.GONE
                    }

                    if (userData.name.toString().isNotEmpty()){
                        viewBinding.userName.text = userData.name.toString()
                    }else{
                        viewBinding.userName.text = userData.auditionId.toString()
                    }

                    viewBinding.userID.text = userData.auditionId.toString()

                    if (userData.followStatus!!){
                        viewBinding.followBtn.text = ConstValFile.Following
                    }else{
                        viewBinding.followBtn.text = ConstValFile.Follow
                    }
                }else{
                    myApplication.printLogE("Get User Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<GetOtherUserResponse>, t: Throwable) {
                myApplication.printLogE("Get User onFailure ${t.toString()}",TAG)
            }

        })
    }


}