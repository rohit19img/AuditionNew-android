package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.img.audition.adapters.ChatUserAdapter
import com.img.audition.databinding.ActivityChatUserBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory

@UnstableApi class ChatUserActivity : AppCompatActivity() {

    private val TAG = "ChatUserActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityChatUserBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@ChatUserActivity)
    }
    private val myApplication by lazy {
        MyApplication(this@ChatUserActivity)
    }
    private var page_no = 1
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        viewBinding.shimmerVideoView.visibility = View.VISIBLE
        viewBinding.shimmerVideoView.startShimmer()

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        if (myApplication.isNetworkConnected()){
            getChatUser()
        }else{
            checkInternetDialog()
        }
    }



    private fun getChatUser() {
        Log.i("canLoadData","Page_no $page_no")
        if (MyApplication(this).isNetworkConnected()){
            mainViewModel.getChatUser(page_no).observe(this){
                it.let {resources ->
                    when (resources.status){
                        Status.SUCCESS->{
                           val data = resources.data?.data
                            if (data!!.size>0){
                                val adapter = ChatUserAdapter(this,data)
                                viewBinding.userChatCycle.adapter  = adapter
                                viewBinding.noDataView.visibility = View.GONE
                                viewBinding.userChatCycle.visibility = View.VISIBLE
                                viewBinding.shimmerVideoView.stopShimmer()
                                viewBinding.shimmerVideoView.hideShimmer()
                                viewBinding.shimmerVideoView.visibility = View.GONE
                            }else{
                                viewBinding.noDataView.visibility = View.VISIBLE
                                viewBinding.userChatCycle.visibility = View.GONE
                                viewBinding.shimmerVideoView.stopShimmer()
                                viewBinding.shimmerVideoView.hideShimmer()
                                viewBinding.shimmerVideoView.visibility = View.GONE
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TAG, resources.status.toString())
                        }
                        else->{
                            if (resources.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this@ChatUserActivity, SplashActivity::class.java))
                                finishAffinity()
                            }
                            Log.e(TAG, resources.status.toString())
                            viewBinding.noDataView.visibility = View.VISIBLE
                            viewBinding.userChatCycle.visibility = View.GONE
                            viewBinding.shimmerVideoView.stopShimmer()
                            viewBinding.shimmerVideoView.hideShimmer()
                            viewBinding.shimmerVideoView.visibility = View.GONE
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
            getChatUser()
        }
        sweetAlertDialog.show()
    }
}