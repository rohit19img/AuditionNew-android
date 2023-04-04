package com.img.audition.screens

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.FollowFollowingResponse
import com.img.audition.dataModel.GetOtherUserResponse
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.ActivityOtherUserProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.VideoReportDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi class OtherUserProfileActivity : AppCompatActivity() {

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

    lateinit var userID : String
    lateinit var userimage : String
    var followStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val manager = GridLayoutManager(this@OtherUserProfileActivity, 3)
        viewBinding.userVideoRecycle.layoutManager = manager

        viewBinding.l1.startShimmer()
        viewBinding.shimmerVideoView.startShimmer()

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
                val showReportDialog = VideoReportDialog(userID,ConstValFile.ReportUserView)
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

        blockUnblockReq.enqueue(object :Callback<CommonResponse>{
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    startActivity(Intent(this@OtherUserProfileActivity, HomeActivity::class.java))
                    finish()
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    override fun onStart() {
        userID = bundle!!.getString(ConstValFile.USER_IDFORIntent).toString()
        followStatus =  bundle!!.getBoolean(ConstValFile.UserFollowStatus,false)
        getUserData(userID,followStatus)

        if (followStatus){
            viewBinding.followBtn.text = ConstValFile.Following
        }else{
            viewBinding.followBtn.text = ConstValFile.Follow
        }
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
        super.onResume()
        viewBinding.followBtn.setOnClickListener {
            viewBinding.followBtn.isSelected = false
            if (!(sessionManager.isUserLoggedIn())){
                myApplication.showToast(ConstValFile.LoginMsg)
                sendToLoginScreen()
            }else {
               try {
                   /*val list = (VideoFragment).videoList2
                   val adapter = (VideoFragment).videoAdapter
                   val position = bundle!!.getInt(ConstValFile.UserPositionInList)
                   if (!(list[position].followStatus!!)){
                       list[position].followStatus = true
                       adapter.notifyDataSetChanged()
                        Thread { followUserApi(userID, "followed") }
                       viewBinding.followBtn.text = ConstValFile.Following
                       viewBinding.followBtn.setTypeface( viewBinding.followBtn.typeface, Typeface.ITALIC)
                   }else{
                       list[position].followStatus = false
                       adapter.notifyDataSetChanged()
                       followUserApi(userID,"unfollowed")
                       viewBinding.followBtn.text = ConstValFile.Follow
                       viewBinding.followBtn.setTypeface( viewBinding.followBtn.typeface, Typeface.NORMAL)
                   }*/
               }catch (e:Exception){
                   myApplication.printLogE(e.toString(),TAG)
               }
            }
        }
    }
}