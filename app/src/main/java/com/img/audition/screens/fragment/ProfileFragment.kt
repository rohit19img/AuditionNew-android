package com.img.audition.screens.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.adapters.LanguageSelecteDialog
import com.img.audition.adapters.VideoItemAdapter
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.dataModel.VideoData
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.FragmentProfileBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.*
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.ArrayList


@UnstableApi
class ProfileFragment(val contextFromActivity: Context) : Fragment() {
    private  var videoItemAdapter: VideoItemAdapter? = null
    private lateinit var videoData: ArrayList<VideoData>
    private val TAG = "ProfileFragment"

    private lateinit var _viewBinding: FragmentProfileBinding
    private val view get() = _viewBinding
    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    private val myApplication by lazy {
        MyApplication(contextFromActivity)
    }
    private val apiInterface by lazy {
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private var referCode = ""

    lateinit var userName: TextView
    lateinit var auditionID: TextView
    lateinit var followCount: TextView
    lateinit var likeCount: TextView
    lateinit var followingCount: TextView
    lateinit var userBio: TextView
    lateinit var userVideoRecycle: RecyclerView
    lateinit var userImageView: CircleImageView
    lateinit var noVideoImage: ImageView
    lateinit var menuButton: ImageView
    lateinit var drawerLayout: DrawerLayout

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding = FragmentProfileBinding.inflate(inflater, container, false)

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

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        view.shimmerVideoView.startShimmer()

        getUserSelfDetails()

        getUserVideo()

        view.userImageView.setOnClickListener {
            contextFromActivity.startActivity(
                Intent(contextFromActivity,UpdatesActivity::class.java)
            )
        }

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
            if (myApplication.isNetworkConnected()){
                sendToCollectionActivity()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.changelanguage.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                showLanguageDialog()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.refer.setOnClickListener {
            val referMessage = "Hey!  I Found a very exciting app  to create videos Biggee, Use my referral code $referCode to get started\nDownload app from https://play.google.com/store/apps/details?id=com.img.audition"
            shareDialog(referMessage)
        }

        view.shareProfile.setOnClickListener {
            val referMessage = "Hey! Follow my profile on biggee search ${sessionManager.getUserAuditionID()} or Click https://biggee.com/profile/${sessionManager.getUserAuditionID()} \n Download App from https://play.google.com/store/apps/details?id=com.img.audition"
            shareDialog(referMessage)
        }

        view.howToWin.setOnClickListener {
            //todo content needed
        }

        view.help.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val intent = Intent(contextFromActivity,HelpAndSupportActivity::class.java)
                startActivity(intent)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.verify.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                sendToVerificationActivity()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.blockedUsers.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                sendToBlockedUserActivity()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.chatUser.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                sendToChatUserActivity()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.privacysaftey.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val intent = Intent(contextFromActivity,PrivacyPolicyActivity::class.java)
                startActivity(intent)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }


           /* val url = "http://143.110.184.198/privacy-policy.html"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)*/
        }


        view.aboutUs.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val intent = Intent(contextFromActivity,AboutUsActivity::class.java)
                startActivity(intent)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }

        }

        view.termCondition.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val intent = Intent(contextFromActivity,TermsAndConditionActivity::class.java)
                startActivity(intent)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }


        view.followListBtn.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val userName = view.userName.text.toString()
                view.followListBtn.isSelected = false
                sendToFollowFollowingListActivity(0, userName)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.followingListBtn.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val userName = view.userName.text.toString()
                view.followingListBtn.isSelected = false
                sendToFollowFollowingListActivity(1, userName)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.wallet.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                view.wallet.isSelected = false
                sendToWalletActivity()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.reward.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                view.reward.isSelected = false
                sendToWalletActivity()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        view.copy.setOnClickListener {
            val clipboard = activity?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", view.auditionID.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(contextFromActivity,"Id Copied..", Toast.LENGTH_SHORT).show()
        }

        view.logout.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val dialogBuilder = AlertDialog.Builder(contextFromActivity)
                dialogBuilder.setTitle("Logout.")
                dialogBuilder.setMessage("Are you sure you want to Logout?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                        logoutUser()
                    })
                    .setNegativeButton("NO", DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                val alert = dialogBuilder.create()
                alert.show()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }


        }

    }

    private fun sendToBlockedUserActivity() {
        val intent = Intent(contextFromActivity, BlockedUsersActivity::class.java)
        startActivity(intent)
    }

    private fun sendToChatUserActivity() {
        val intent = Intent(contextFromActivity, ChatUserActivity::class.java)
        startActivity(intent)
    }

    private fun sendToVerificationActivity() {
        val intent = Intent(contextFromActivity, VerificationActivity::class.java)
        startActivity(intent)
    }

    private fun senToEditProfile() {
        val intent = Intent(contextFromActivity, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun sendToFollowFollowingListActivity(pagePos: Int, userName: String) {
        val bundle = Bundle()
        bundle.putInt(ConstValFile.PagePosition, pagePos)
        bundle.putString(ConstValFile.UserName, userName)
        bundle.putString(ConstValFile.USER_ID, sessionManager.getUserSelfID())
        val intent = Intent(contextFromActivity, FollowFollowingListActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        startActivity(intent)
    }

    private fun sendToWalletActivity() {
        val intent = Intent(contextFromActivity, WalletActivity::class.java)
        startActivity(intent)
    }


    private fun sendToSplashActivity() {
        val intent = Intent(contextFromActivity, SplashActivity::class.java)
        startActivity(intent)
    }

    private fun sendToCollectionActivity() {
        val intent = Intent(contextFromActivity.applicationContext, CollectionActivity::class.java)
        startActivity(intent)
    }

    private fun logoutUser() {
        val logoutReq = apiInterface.logoutUser(sessionManager.getToken())

        logoutReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(
                call: Call<CommanResponse>,
                response: Response<CommanResponse>
            ) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    sessionManager.clearLogoutSession()
                    startActivity(Intent(contextFromActivity, SplashActivity::class.java))
                    requireActivity().finishAffinity()
                }else{
                    Toast.makeText(contextFromActivity,"Something went wrong..", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Toast.makeText(contextFromActivity,"Something went wrong..", Toast.LENGTH_SHORT).show()

            }
        })
    }

    private fun getUserVideo(){
        mainViewModel.getUserVideo(sessionManager.getUserSelfID()!!)
            .observe(viewLifecycleOwner){
                it.let {videoResponse->
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            if (videoResponse.data?.success!!){
                                videoData = videoResponse.data.data
                                if (videoData.size > 0) {
                                    videoItemAdapter = VideoItemAdapter(contextFromActivity, videoData)
                                    userVideoRecycle.adapter = videoItemAdapter
                                    view.shimmerVideoView.stopShimmer()
                                    view.shimmerVideoView.hideShimmer()
                                    view.shimmerVideoView.visibility = View.GONE
                                    userVideoRecycle.visibility = View.VISIBLE
                                } else {
                                    Log.d(TAG, "No Video Data")
                                    view.shimmerVideoView.stopShimmer()
                                    view.shimmerVideoView.hideShimmer()
                                    view.shimmerVideoView.visibility = View.GONE
                                    noVideoImage.visibility = View.VISIBLE
                                }
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TAG, videoResponse.status.toString())
                        }
                        else->{
                            if (videoResponse.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(contextFromActivity, SplashActivity::class.java))
                                requireActivity().finishAffinity()
                            }
                            Log.d(TAG, videoResponse.status.toString())
                        }
                    }
                }
            }
    }

    private fun getUserSelfDetails(){
        mainViewModel.getUserSelfDetails()
            .observe(viewLifecycleOwner){
                it.let {resources->
                    when(resources.status){
                        Status.SUCCESS ->{
                            if (resources.data!!.success!!){
                                val userData = resources.data.data
                                if (userData != null) {
                                    likeCount.text =  formatCount(userData.totalLike!!)
                                    followCount.text = formatCount(userData.followersCount!!)
                                    followingCount.text = formatCount(userData.followingCount!!)

                                    if (userData.referCode!=null)
                                        referCode = userData.referCode.toString()

                                    if (userData.unreadMsgCount!!>0){
                                        view.msgCount.visibility = View.VISIBLE
                                        if (userData.unreadMsgCount!!<10){
                                            view.msgCount.text = userData.unreadMsgCount.toString()
                                        }else{
                                            view.msgCount.text = "10+"
                                        }
                                    }else{
                                        view.msgCount.visibility = View.GONE
                                    }

                                    if (userData.image.toString().isNotEmpty()) {
                                        Glide.with(contextFromActivity).load(userData.image.toString())
                                            .placeholder(R.drawable.person_ic).into(userImageView)
                                        sessionManager.setUserProfileImage(userData.image.toString())
                                    } else {
                                        userImageView.setImageResource(R.drawable.person_ic)
                                    }
                                    if (userData.bio.toString().isNotEmpty()) {
                                        userBio.text = userData.bio.toString()
                                    } else {
                                        userBio.visibility = View.GONE
                                    }
                                    if (userData.name.toString().isNotEmpty()) {
                                        userName.text = userData.name.toString()
                                        sessionManager.setUserName(userData.name.toString())
                                    } else {
                                        userName.text = userData.auditionId.toString()
                                        sessionManager.setUserName(userData.auditionId.toString())
                                    }
                                    auditionID.text = userData.auditionId.toString()
                                    sessionManager.setUserAuditionID(userData.auditionId.toString())
                                } else {
                                    Log.e(TAG, "User Data Null")
                                }
                            }else{
                                Toast.makeText(contextFromActivity,"Something went wrong..", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TAG, resources.status.toString())
                        }
                        else->{
                            if (resources.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(contextFromActivity, SplashActivity::class.java))
                                requireActivity().finishAffinity()
                            }
                            Log.e(TAG, "getUserSelfDetails: ${resources.message}" )
                            Log.d(TAG, resources.status.toString())
                        }
                    }
                }
            }
    }

    private fun showLanguageDialog() {
        val showLangDialog = LanguageSelecteDialog()
        showLangDialog.show(parentFragmentManager, showLangDialog.tag)
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            videoData.clear()
            videoItemAdapter = null
        }catch(e:Exception){
            e.printStackTrace()
        }

        getView()?.destroyDrawingCache()
        super.onDestroyView()
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

    private fun shareDialog(referUrl:String) {
        val intent = Intent(Intent.ACTION_SEND)
        val shareBody: String = referUrl
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee Refer Code"))
    }
}