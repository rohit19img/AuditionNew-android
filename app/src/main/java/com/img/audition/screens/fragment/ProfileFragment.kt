package com.img.audition.screens.fragment

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.LanguageSelecteDialog
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.FragmentProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi class ProfileFragment(val contextFromActivity: Context) : Fragment() {
    val TAG = "ProfileFragment"

    private lateinit var _viewBinding : FragmentProfileBinding
    private val view get() = _viewBinding!!
    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    private val myApplication by lazy {
        MyApplication(contextFromActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    lateinit var userName:TextView
    lateinit var auditionID:TextView
    lateinit var followCount:TextView
    lateinit var likeCount:TextView
    lateinit var followingCount:TextView
    lateinit var userBio:TextView
    lateinit var userVideoRecycle:RecyclerView
    lateinit var userImageView: ImageView
    lateinit var noVideoImage: ImageView
    lateinit var menuButton: ImageView
    lateinit var drawerLayout:DrawerLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentProfileBinding.inflate(inflater,container,false)


        userName = view.userName
        userBio = view.userBio
        userVideoRecycle = view.userVideoRecycle
        auditionID = view.auditionID
        followingCount = view.followingCount
        followCount = view.followCount
        likeCount = view.likeCount
        userImageView = view.userImageView
        noVideoImage = view.noVideo
        menuButton = view.line3Menu
        drawerLayout = view.drawerLayout


        return view.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        view.shimmerVideoView.startShimmer()

        view.editProfileBtn.setOnClickListener {
            senToEditProfile()
        }
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        view.watchLaterBtn.setOnClickListener {
            sendToCollectionActivity()
        }

        view.changelanguage.setOnClickListener {
            showLanguageDialog()
        }

        view.verify.setOnClickListener {
            sendToVerificationActivity()
        }

        view.followListBtn.setOnClickListener {
            val userName = view.userName.text.toString()
            view.followListBtn.isSelected = false
            sendToFollowFollowingListActivity(0,userName)
        }

        view.followingListBtn.setOnClickListener {
            val userName = view.userName.text.toString()
            view.followingListBtn.isSelected = false
            sendToFollowFollowingListActivity(1,userName)
        }

        view.wallet.setOnClickListener {
            view.wallet.isSelected = false
            sendToWalletActivity()
        }
        view.reward.setOnClickListener {
            view.reward.isSelected = false
            sendToWalletActivity()
        }

        view.copy.setOnClickListener {
            val clipboard = activity?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label",view.auditionID.text.toString())
            clipboard.setPrimaryClip(clip)
            myApplication.showToast("Id Copied..")
        }

        view.logout.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(contextFromActivity)
            dialogBuilder.setTitle("Logout.")
            dialogBuilder.setMessage("Are you sure you want to Logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener {
                        dialog, id -> logoutUser()
                })
                .setNegativeButton("NO", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.show()

        }

    }
    private fun sendToVerificationActivity() {
        val intent = Intent(contextFromActivity,VerificationActivity::class.java)
        startActivity(intent)
    }

    private fun senToEditProfile() {
        val intent = Intent(contextFromActivity,EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun sendToFollowFollowingListActivity(pagePos:Int,userName:String) {
        val bundle = Bundle()
        bundle.putInt(ConstValFile.PagePosition,pagePos)
        bundle.putString(ConstValFile.UserName,userName)
        val intent = Intent(contextFromActivity,FollowFollowingListActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    private fun sendToWalletActivity() {
        val intent = Intent(contextFromActivity,WalletActivity::class.java)
        startActivity(intent)
    }

    private fun sendToCollectionActivity() {
        val intent = Intent(contextFromActivity.applicationContext,CollectionActivity::class.java)
        startActivity(intent)
    }

    private fun logoutUser() {
        val logoutReq = apiInterface.logoutUser(sessionManager.getToken())

        logoutReq.enqueue(object : Callback<CommonResponse>{
            override fun onResponse(
                call: Call<CommonResponse>,
                response: Response<CommonResponse>
            ) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                        sessionManager.clearLogoutSession()
                        startActivity(Intent(contextFromActivity, SplashActivity::class.java))
                        requireActivity().finishAffinity()


                }else{
                    myApplication.printLogE(response.toString(),TAG)
                    myApplication.showToast("Something went wrong...")

                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                myApplication.showToast("Something went wrong...")
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUserSelfDetails()

        getUserSelfVideo()
    }

    private fun getUserSelfVideo() {
        val userVideoReq = apiInterface.getUserSelfVideo(sessionManager.getToken())
        userVideoReq.enqueue(@UnstableApi object : Callback<VideoResponse>{
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val videoData = response.body()!!.data
                    if (videoData.size>0) {
                        val videoItemAdapter = VideoItemAdapter(contextFromActivity, videoData)
                        userVideoRecycle.adapter = videoItemAdapter
                        view.shimmerVideoView.stopShimmer()
                        view.shimmerVideoView.hideShimmer()
                        view.shimmerVideoView.visibility = View.GONE
                        userVideoRecycle.visibility = View.VISIBLE
                    }else{
                        myApplication.printLogD("No Video Data",TAG)
                        view.shimmerVideoView.stopShimmer()
                        view.shimmerVideoView.hideShimmer()
                        noVideoImage.visibility = View.VISIBLE
                    }
                }else{
                    myApplication.printLogE("Get Other User Self Video Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Self Video onFailure ${t.toString()}",TAG)
            }
        })
    }


    private fun getUserSelfDetails() {
        val userDetilsReq = apiInterface.getUserSelfDetails(sessionManager.getToken())

        userDetilsReq.enqueue(object : Callback<UserSelfProfileResponse>{
            override fun onResponse(call: Call<UserSelfProfileResponse>, response: Response<UserSelfProfileResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    myApplication.printLogD(response.toString(),TAG)
                    val userData = response.body()!!.data
                    if(userData!=null){
                        likeCount.text = userData!!.totalLike.toString()
                        followCount.text = userData!!.followersCount.toString()
                        followingCount.text = userData!!.followingCount.toString()
                        if (userData.image.toString().isNotEmpty()){
                            Glide.with(contextFromActivity).load(userData.image.toString())
                                .placeholder(R.drawable.person_ic).into(userImageView)
                            MyApplication.DownloadImageTask(userImageView).execute(userData.image.toString())
                        }else{
                            userImageView.setImageResource(R.drawable.person_ic)
                        }
                        if (userData.bio.toString().isNotEmpty()){
                            userBio.text = userData.bio.toString()
                        }else{
                            userBio.visibility = View.GONE
                        }
                        if (userData.name.toString().isNotEmpty()){
                            userName.text = userData.name.toString()
                        }else{
                            userName.text = userData.auditionId.toString()
                        }
                        auditionID.text = userData.auditionId.toString()
                    }else{
                        myApplication.printLogE("User Data Null",TAG)
                    }
                }else{
                    myApplication.printLogE("Get Other User Self Data Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<UserSelfProfileResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Self Data onFailure ${t.toString()}",TAG)
            }

        })
    }

    private fun showLanguageDialog() {
        val showLangDialog = LanguageSelecteDialog()
        showLangDialog.show(parentFragmentManager,showLangDialog.tag)
    }
}