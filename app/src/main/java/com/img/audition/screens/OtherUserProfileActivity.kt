package com.img.audition.screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.GridLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.adapters.VideoAdapter
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.*
import com.img.audition.databinding.ActivityOtherUserProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.VideoFragment
import com.img.audition.screens.fragment.VideoReportDialog
import com.img.audition.videoWork.FollowFollowingTrack
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.ArrayList

@UnstableApi
class OtherUserProfileActivity : AppCompatActivity(),FollowFollowingTrack {

    private val TAG = "OtherUserProfileActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityOtherUserProfileBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@OtherUserProfileActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private var followCount = 0
    private var followingCount = 0

    private lateinit var userID : String
    private lateinit var auditionIID : String
    private lateinit var userimage : String
    private var followStatus: Boolean = false
    private var position  = 0

    private lateinit var mainViewModel: MainViewModel

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val manager = GridLayoutManager(this@OtherUserProfileActivity, 3)
        viewBinding.userVideoRecycle.layoutManager = manager

        viewBinding.l1.startShimmer()
        viewBinding.shimmerVideoView.startShimmer()

         mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

         viewBinding.messageBtn.setOnClickListener {
           if (!(sessionManager.isUserLoggedIn())){
               sendToLoginScreen()
           }else{
               startActivity(
                   Intent(this@OtherUserProfileActivity,MessageActivity::class.java)
                       .putExtra("name",viewBinding.userName.text.toString())
                       .putExtra("userid",userID)
                       .putExtra("image",userimage)
                       .putExtra("auditionID",auditionIID)
               )
           }
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }


        viewBinding.copy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label",viewBinding.userID.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this,"Id Copied..", Toast.LENGTH_SHORT).show()

        }

        viewBinding.menuIcBtn.setOnClickListener {
            showReportAndBlockDialog()
        }
    }


    private fun showReportAndBlockDialog() {
        val dialog1 = BottomSheetDialog(this@OtherUserProfileActivity,R.style.CustomBottomSheetDialogTheme)
        dialog1.setContentView(R.layout.report_and_block_dialog)

        val report = dialog1.findViewById<TextView>(R.id.report)
        val block = dialog1.findViewById<TextView>(R.id.block)

        report?.setOnClickListener {
            if (!sessionManager.isUserLoggedIn()){
                sendToLoginScreen()
            }else{
                val showReportDialog = VideoReportDialog(this@OtherUserProfileActivity,userID,ConstValFile.ReportUserView)
                showReportDialog.show(supportFragmentManager,showReportDialog.tag)
            }
            dialog1.dismiss()
        }

        block?.setOnClickListener {
            if (!sessionManager.isUserLoggedIn()){
                sendToLoginScreen()
            }else{
                val sweetAlertDialog = SweetAlertDialog(this@OtherUserProfileActivity, SweetAlertDialog.WARNING_TYPE)
                sweetAlertDialog.titleText = "Block user"
                sweetAlertDialog.contentText = "Do you want block this user"

                sweetAlertDialog.confirmText = "Yes"
                sweetAlertDialog.setConfirmClickListener {
                    sweetAlertDialog.dismiss()
                    blockUnblockUser(ConstValFile.Block)
                }
                sweetAlertDialog.cancelText = "No"
                sweetAlertDialog.setCancelClickListener {
                    sweetAlertDialog.dismiss()
                }
                sweetAlertDialog.show()
            }
            dialog1.dismiss()
        }
        dialog1.show()

    }

    private fun blockUnblockUser(status: String) {
        val blockUnblockReq = apiInterface.blockUnblockUser(sessionManager.getToken(),
        userID,status)

        blockUnblockReq.enqueue(@UnstableApi object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    startActivity(Intent(this@OtherUserProfileActivity, HomeActivity::class.java))
                    finish()
                }else{
                    Log.d(TAG, "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    override fun onStart() {
        userID = bundle!!.getString(ConstValFile.USER_IDFORIntent).toString()
        auditionIID = bundle!!.getString("auditionID").toString()
        followStatus =  bundle!!.getBoolean(ConstValFile.UserFollowStatus,false)

        if (followStatus){
            viewBinding.followBtn.text = ConstValFile.Following
        }else{
            viewBinding.followBtn.text = ConstValFile.Follow
        }

        if (bundle!!.getBoolean(ConstValFile.isSearchAuditionID)){
            getUserByAuditionId(bundle!!.getString(ConstValFile.AuditionID)!!)
        }else{
            getUserData(userID)
            getUserVideo(userID)
        }


        viewBinding.followListBtn.setOnClickListener {
            val userName = viewBinding.userName.text.toString()
            sendToFollowFollowingListActivity(0, userName,userID)
        }

        viewBinding.followingListBtn.setOnClickListener {
            val userName = viewBinding.userName.text.toString()
            sendToFollowFollowingListActivity(1, userName,userID)
        }

        super.onStart()
    }


     private fun sendToFollowFollowingListActivity(pagePos: Int, userName: String,userId: String?) {
         val bundle = Bundle()
         bundle.putInt(ConstValFile.PagePosition, pagePos)
         bundle.putString(ConstValFile.UserName, userName)
         bundle.putString(ConstValFile.USER_ID,userId)
         val intent = Intent(this@OtherUserProfileActivity, FollowFollowingListActivity::class.java)
         intent.putExtra(ConstValFile.Bundle, bundle)
         startActivity(intent)
     }


     fun getUserVideo(userID: String){
         mainViewModel.getUserVideo(userID)
             .observe(this){
                 it.let {videoResponse->
                     when(videoResponse.status){
                         Status.SUCCESS ->{
                             if (videoResponse.data?.success!!){
                                 val videoData = videoResponse.data.data
                                 if (videoData.size>0){
                                     val videoItemAdapter = VideoItemAdapter(this@OtherUserProfileActivity,videoData)
                                     viewBinding.userVideoRecycle.adapter = videoItemAdapter
                                     viewBinding.shimmerVideoView.stopShimmer()
                                     viewBinding.shimmerVideoView.hideShimmer()
                                     viewBinding.shimmerVideoView.visibility = View.GONE
                                     viewBinding.userVideoRecycle.visibility = View.VISIBLE

                                 }else{
                                     viewBinding.shimmerVideoView.stopShimmer()
                                     viewBinding.shimmerVideoView.hideShimmer()
                                     viewBinding.noVideo.visibility = View.VISIBLE
                                 }
                             }
                         }
                         Status.LOADING ->{
                             Log.d(TAG, "onResponse: ${videoResponse.status.toString()}")
                         }
                         else->{
                             Log.e(TAG, " ${videoResponse.status.toString()}")
                         }
                     }
                 }
             }

     }

    private fun getUserData(userID: String) {
        val getUserReq = apiInterface.getOtherUser(sessionManager.getToken(),userID)
        getUserReq.enqueue(object : Callback<GetOtherUserResponse>{
            override fun onResponse(call: Call<GetOtherUserResponse>, response: Response<GetOtherUserResponse>) {
                if (response.isSuccessful){
                    if (response.body()!!.success!! && response.body()!=null){
                        viewBinding.l1.stopShimmer()
                        viewBinding.l1.hideShimmer()
                        val userData = response.body()!!.data[0]

                        followCount = userData.followersCount!!
                        followingCount = userData.followingCount!!
                        viewBinding.likeCount.text = formatCount(userData.totalLike!!)
                        viewBinding.followCount.text = formatCount(followCount)
                        viewBinding.followingCount.text = formatCount(followingCount)

                        if (userData.image.toString().isNotEmpty()){
                            userimage = userData.image.toString()
                            Glide.with(this@OtherUserProfileActivity).load(userData.image.toString()).placeholder(R.drawable.person_ic).into(viewBinding.userImageView)
                        }else{
                            userimage = ""
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

                        followStatus = userData.followStatus!!
                        if (followStatus)
                            viewBinding.followBtn.text = ConstValFile.Following
                        else
                            viewBinding.followBtn.text = ConstValFile.Follow

                    }else{
                        Toast.makeText(this@OtherUserProfileActivity,"User Not Found",Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }

                }else{
                    Log.e(TAG,"Get User Response Failed ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GetOtherUserResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    private fun sendToLoginScreen(){
        val intent = Intent(this@OtherUserProfileActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun followUserApi(userId: String?, status: String) {
        val ffReq = apiInterface.followFollowing(sessionManager.getToken(),userId,status)
        ffReq.enqueue(object : Callback<FollowFollowingResponse>{
            override fun onResponse(call: Call<FollowFollowingResponse>, response: Response<FollowFollowingResponse>) {
                if (response.isSuccessful){
                    Log.d(TAG,"onResponse: FollowFollowing ${response.toString()}")
                }else{
                  Log.e(TAG,"onResponse: FollowFollowing ${response.toString()}")
                }
            }
            override fun onFailure(call: Call<FollowFollowingResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        try {
            position = bundle!!.getInt(ConstValFile.UserPositionInList,0)
        }catch (e:Exception){
            e.printStackTrace()
        }
        viewBinding.followBtn.setOnClickListener {
            if (!(sessionManager.isUserLoggedIn())){
                Toast.makeText(this,ConstValFile.LoginMsg,Toast.LENGTH_SHORT).show()
                sendToLoginScreen()
            }else {
                try {
                    if (!followStatus){
                        followStatus = true
                        followUserApi(userID, "followed")
                        followCount += 1
                        viewBinding.followCount.text = followCount.toString()
                        viewBinding.followBtn.text = ConstValFile.Following
                        viewBinding.followBtn.setTypeface(viewBinding.followBtn.typeface, Typeface.ITALIC)
                    }else{
                        followStatus = false
                        if (followCount>0){
                            followCount -= 1
                            viewBinding.followCount.text = followCount.toString()
                        }else{
                            viewBinding.followCount.text = "0"
                        }
                        followUserApi(userID,"unfollowed")
                        viewBinding.followBtn.text = ConstValFile.Follow
                        viewBinding.followBtn.setTypeface(viewBinding.followBtn.typeface, Typeface.NORMAL)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
        super.onResume()
    }


     override fun onBackPressed() {

       /* Log.d("onIntentReceived","Intent onBackPressed : $followStatus")
         Log.d("onIntentReceived","Intent onBackPressed : $userID")
         Log.d("onIntentReceived","Intent onBackPressed : $position")
         this.onIntentReceived(followStatus,userID,position)*/

        /* val backToIntent = Intent()
         backToIntent.putExtra(ConstValFile.VideoPosition,position)
         backToIntent.putExtra(ConstValFile.VideoPosition,userID)
         backToIntent.putExtra(ConstValFile.VideoPosition,followStatus)
         setResult(RESULT_OK,backToIntent)*/
         super.onBackPressed()
     }

     private fun getUserByAuditionId(auditionID: String) {

         val getUserReq = apiInterface.getUserByAuditionId(sessionManager.getToken(),auditionID)
         getUserReq.enqueue(object : Callback<GetOtherUserResponse>{
             override fun onResponse(call: Call<GetOtherUserResponse>, response: Response<GetOtherUserResponse>) {
                 if (response.isSuccessful){
                     if (response.body()!!.success!! && response.body()!=null){
                         viewBinding.l1.stopShimmer()
                         viewBinding.l1.hideShimmer()
                         val userData = response.body()!!.data[0]
                         userID = userData.Id.toString()
                         auditionIID = userData.auditionId.toString()
                         getUserVideo(userID)
                         followCount = userData.followersCount!!
                         followingCount = userData.followingCount!!
                         viewBinding.likeCount.text = formatCount(userData.totalLike!!)
                         viewBinding.followCount.text = formatCount(followCount)
                         viewBinding.followingCount.text = formatCount(followingCount)

                         if (userData.image.toString().isNotEmpty()){
                             userimage = userData.image.toString()
                             Glide.with(this@OtherUserProfileActivity).load(userData.image.toString()).placeholder(R.drawable.person_ic).into(viewBinding.userImageView)
                         }else{
                             userimage = ""
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

                         followStatus = userData.followStatus!!
                         if (followStatus)
                             viewBinding.followBtn.text = ConstValFile.Following
                         else
                             viewBinding.followBtn.text = ConstValFile.Follow
                     }else{
                         Toast.makeText(this@OtherUserProfileActivity,"User Not Found",Toast.LENGTH_SHORT).show()
                         onBackPressed()
                     }
                 }else{

                     Log.e(TAG,"Get User Response Failed ${response.code()}")
                 }
             }
             override fun onFailure(call: Call<GetOtherUserResponse>, t: Throwable) {
                t.printStackTrace()
             }
         })
     }

    private fun formatCount(count: Int): String {
        val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
        val numValue: Long = count.toLong()
        val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
        /*return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format("%.1fk", Math.floor(count / 100.0) / 10)
            else -> (count / 1000).toString() + "k"
        }*/
    }
}