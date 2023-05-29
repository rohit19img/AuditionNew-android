package com.img.audition.screens

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.GridLayoutManager
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
import java.util.ArrayList


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

    private lateinit var userID : String
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
            startActivity(
                Intent(this@OtherUserProfileActivity,MessageActivity::class.java)
                    .putExtra("name",viewBinding.userName.text.toString())
                    .putExtra("userid",userID)
                    .putExtra("image",userimage)
            )
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }


        viewBinding.copy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label",viewBinding.userID.text.toString())
            clipboard.setPrimaryClip(clip)
            myApplication.showToast("Id Copied..")
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
                val dialogBuilder = AlertDialog.Builder(this@OtherUserProfileActivity)
                dialogBuilder.setTitle("Block user")
                dialogBuilder.setMessage("Do you want block this user")
                    .setCancelable(false)
                    .setPositiveButton("Block", DialogInterface.OnClickListener {
                            _, _ -> blockUnblockUser(ConstValFile.Block)
                    })
                    .setNegativeButton("NO") { dialog, _ ->
                        dialog.cancel()
                    }
                val alert = dialogBuilder.create()
                alert.show()
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
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    override fun onStart() {
        userID = bundle!!.getString(ConstValFile.USER_IDFORIntent).toString()
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

                     myApplication.printLogD(videoResponse.message.toString(),"apiCall 1")
                     when(videoResponse.status){
                         Status.SUCCESS ->{
                             myApplication.printLogD(videoResponse.data!!.message.toString(),"apiCall 2")
                             if (videoResponse.data.success!!){
                                 val videoData = videoResponse.data.data
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

    private fun getUserData(userID: String) {
        myApplication.printLogD(userID.toString(),"Other User ID")

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
                    myApplication.printLogE("Get User Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<GetOtherUserResponse>, t: Throwable) {
                myApplication.printLogE("Get User onFailure ${t.toString()}",TAG)
            }

        })
    }

    fun sendToLoginScreen(){
        val intent = Intent(this@OtherUserProfileActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun followUserApi(userId: String?, status: String) {
        myApplication.printLogD("followUserApi: $userId $status",TAG)
        val ffReq = apiInterface.followFollowing(sessionManager.getToken(),userId,status)
        ffReq.enqueue(object : Callback<FollowFollowingResponse>{
            override fun onResponse(call: Call<FollowFollowingResponse>, response: Response<FollowFollowingResponse>) {
                if (response.isSuccessful){
                    myApplication.printLogD("onResponse: FollowFollowing ${response.toString()}",TAG)
                }else{
                    myApplication.printLogE("onResponse: FollowFollowing ${response.toString()}",TAG)
                }
            }
            override fun onFailure(call: Call<FollowFollowingResponse>, t: Throwable) {
                myApplication.printLogE("onFailure: FollowFollowing ${t.toString()}",TAG)

            }
        })
    }

    override fun onResume() {
        try {
            position = bundle!!.getInt(ConstValFile.UserPositionInList,0)
        }catch (e:Exception){
            e.printStackTrace()
        }
        viewBinding.followBtn.setOnClickListener {
            if (!(sessionManager.isUserLoggedIn())){
                myApplication.showToast(ConstValFile.LoginMsg)
                sendToLoginScreen()
            }else {
                try {
                    val list : ArrayList<VideoData> = bundle!!.getSerializable("list") as ArrayList<VideoData>

                    myApplication.printLogD("UserPro : $position","check 900")
                    if (!followStatus){
                        list[position].followStatus = true
                        followStatus = true
                        followUserApi(userID, "followed")
                        viewBinding.followBtn.text = ConstValFile.Following
                        viewBinding.followBtn.setTypeface( viewBinding.followBtn.typeface, Typeface.ITALIC)
                    }else{
                        list[position].followStatus = false
                        followStatus = false
                        followUserApi(userID,"unfollowed")
                        viewBinding.followBtn.text = ConstValFile.Follow
                        viewBinding.followBtn.setTypeface( viewBinding.followBtn.typeface, Typeface.NORMAL)
                    }
                }catch (e:Exception){
                    myApplication.printLogE(e.toString(),TAG)
                }
            }
        }
        super.onResume()
    }


     override fun onBackPressed() {
         myApplication.printLogD("Intent onBackPressed : $followStatus","onIntentReceived")
         myApplication.printLogD("Intent onBackPressed : $userID","onIntentReceived")
         myApplication.printLogD("Intent onBackPressed : $position","onIntentReceived")
//         followFollowingTrackIntent.onIntentReceived(followStatus,userID,position)
       /*
         val backToIntent = Intent()
         backToIntent.putExtra(ConstValFile.VideoPosition,position)
         backToIntent.putExtra(ConstValFile.VideoPosition,userID)
         backToIntent.putExtra(ConstValFile.VideoPosition,followStatus)
         setResult(RESULT_OK,backToIntent)*/
         super.onBackPressed()
     }

     private fun getUserByAuditionId(auditionID: String) {
         myApplication.printLogD(auditionID.toString(),"Other User ID")

         val getUserReq = apiInterface.getUserByAuditionId(sessionManager.getToken(),auditionID)
         getUserReq.enqueue(object : Callback<GetOtherUserResponse>{
             override fun onResponse(call: Call<GetOtherUserResponse>, response: Response<GetOtherUserResponse>) {
                 if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                     viewBinding.l1.stopShimmer()
                     viewBinding.l1.hideShimmer()
                     val userData = response.body()!!.data[0]
                     userID = userData.Id.toString()
                     getUserVideo(userID)
                     viewBinding.likeCount.text = userData.totalLike.toString()
                     viewBinding.followCount.text = userData.followersCount.toString()
                     viewBinding.followingCount.text = userData.followingCount.toString()
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
                     myApplication.printLogE("Get User Response Failed ${response.code()}",TAG)
                 }
             }

             override fun onFailure(call: Call<GetOtherUserResponse>, t: Throwable) {
                 myApplication.printLogE("Get User onFailure ${t.toString()}",TAG)
             }

         })
     }
}