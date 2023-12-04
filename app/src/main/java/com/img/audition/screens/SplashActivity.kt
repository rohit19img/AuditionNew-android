package com.img.audition.screens

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import com.img.audition.BuildConfig
import cn.pedant.SweetAlert.SweetAlertDialog

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.img.audition.dataModel.GuestLoginRequest
import com.img.audition.dataModel.LoginResponse
import com.img.audition.dataModel.RootResponse
import com.img.audition.dataModel.VersionInfo
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoCacheWork
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

 @UnstableApi @SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var sessionManager : SessionManager
    private lateinit var myApplication : MyApplication
    private val TAG = "SplashActivity"
    private var deviceID = ""

     companion object{
         var isPopupBannerShow = false
     }

     private lateinit var mainViewModel: MainViewModel

     private val progressDialog by lazy {
         ProgressDialog(this@SplashActivity)
     }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading..")
        sessionManager = SessionManager(this@SplashActivity)
        myApplication  = MyApplication(this@SplashActivity)

        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        if (myApplication.isNetworkConnected()){
//            decideWhereSend()
            getAppVersion()

        }else{
            showRetryDialog()
        }
    }

     private fun showRetryDialog() {
         val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
         sweetAlertDialog.titleText = "Internet"
         sweetAlertDialog.contentText = ConstValFile.Check_Connection
         sweetAlertDialog.confirmText = "Retry"
         sweetAlertDialog.setConfirmClickListener {
             sweetAlertDialog.dismiss()
             decideWhereSend()
         }
         sweetAlertDialog.show()
     }

     private fun updateAppDialog() {
         val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
         sweetAlertDialog.titleText = "App Update"
         sweetAlertDialog.contentText = "Please update app with latest version"
         sweetAlertDialog.confirmText = "Update"
         sweetAlertDialog.cancelText = "Cancel"
         sweetAlertDialog.setCancelable(false)
         sweetAlertDialog.setConfirmClickListener {
             sweetAlertDialog.dismiss()
             //send to playStore
             val intent = Intent(Intent.ACTION_VIEW)
             intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.img.audition")
             startActivity(intent)
         }
         sweetAlertDialog.setCancelClickListener {
             sweetAlertDialog.dismiss()
             finish()
         }
         sweetAlertDialog.show()
     }

     private fun decideWhereSend() {
         if (myApplication.isNetworkConnected()){
             FirebaseMessaging.getInstance().subscribeToTopic("Audition-All")
             if (!(sessionManager.isUserLoggedIn())){
                 myApplication.printLogD("Not Login in",TAG)
                 if (!(sessionManager.isGuestLoggedIn())){
                     onTokenRefresh()
                 }else{
                     sendToHomeActivity()
                 }
             }else{
                 myApplication.printLogD("Login in",TAG)
                 sendToHomeActivity()
             }
         }else{
             showRetryDialog()
         }

     }


     fun onTokenRefresh() {
        val refreshedToken = arrayOf("")
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(object : OnCompleteListener<String?> {
                override fun onComplete(task: Task<String?>) {
                    if (!task.isSuccessful()) {
                        myApplication.printLogE(task.exception.toString(),TAG);
                        return
                    }
                    val token: String? = task.getResult()
                    myApplication.printLogD(token.toString(),"Firebase Token")
                    refreshedToken[0] = token.toString()
                    sessionManager.setNotificationToken(refreshedToken[0])

                    guestUserLogin()
                }
            })
        FirebaseMessaging.getInstance().subscribeToTopic("All-user")
    }


    fun sendToHomeActivity(){
        try {
            if (File(sessionManager.getCreateVideoPath().toString()).exists()){
                File(sessionManager.getCreateVideoPath().toString()).delete()
            }

            if (File(sessionManager.getCreateDuetVideoUrl().toString()).exists()){
                File(sessionManager.getCreateDuetVideoUrl().toString()).delete()
            }

            if (File(sessionManager.getTrimAudioPath().toString()).exists()){
                File(sessionManager.getTrimAudioPath().toString()).delete()
            }

        }catch (e :java.lang.Exception){
            e.printStackTrace()
        }
        sessionManager.clearVideoSession()
        sessionManager.clearContestSession()
        sessionManager.clearDuetSession()
        val homeIntent = Intent(this@SplashActivity,HomeActivity::class.java)
        startActivity(homeIntent)
        finish()
    }

    override fun onDestroy() {
        myApplication.printLogD("onDestroy ",TAG)

        super.onDestroy()
    }

    fun deleteCache() {
        try {
            val dir: File = cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

     @SuppressLint("HardwareIds")
     private fun guestUserLogin(){

         deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
         if (deviceID!=""){

             val guestRequestModel = GuestLoginRequest(deviceID, sessionManager.getNotificationToken())
             myApplication.printLogI(guestRequestModel.deviceId.toString(),"guestLoginRequest deviceId:")
             myApplication.printLogI(guestRequestModel.fcmToken.toString(),"guestLoginRequest appId:")

             mainViewModel.guestLogin(guestRequestModel)
                 .observe(this){
                     it.let {resource ->
                         when(resource.status){
                             Status.SUCCESS ->{
                                 val guestToken = resource.data!!.data!!.token.toString()
                                 val guestUserID = resource.data!!.data!!.id.toString()
                                 sessionManager.clearLogoutSession()
                                 myApplication.printLogD(guestToken,ConstValFile.TOKEN)
                                 myApplication.printLogD(guestUserID,ConstValFile.USER_ID)
                                 sessionManager.setGuestLogin(true)
                                 sessionManager.setUserSelfID(guestUserID)
                                 sessionManager.setToken(guestToken)
                                 Thread.sleep(500)
                                 sendToHomeActivity()
                             }
                             Status.LOADING->{

                             }
                             Status.ERROR ->{
                                 myApplication.showToast("Try Again..")
                             }
                         }
                     }
                 }
         }else{
             myApplication.showToast("Try Again..")
         }
     }

     private fun getAppVersion(){
         val appVersion = BuildConfig.VERSION_CODE
         mainViewModel.getVersion()
             .observe(this){
                 it.let {resource ->
                     when(resource.status){
                         Status.SUCCESS ->{
                             progressDialog.dismiss()
                             val data = resource.data as VersionInfo
                             Log.d(TAG, "getAppVersion: web Version - ${data.version}")
                             Log.d(TAG, "getAppVersion: app Version - $appVersion")
                             val webVersion = data.version
                             if (appVersion==webVersion){
                                 decideWhereSend()
                             }else{
                                 updateAppDialog()
                             }
                         }
                         Status.LOADING->{
                             progressDialog.show()
                             Log.d(TAG, "getAppVersion: loading")
                         }
                         Status.ERROR ->{
                             progressDialog.dismiss()
                             Log.d(TAG, "getAppVersion: error")
                             myApplication.printLogD(resource.message.toString(),"apiCall 4")
                             if (resource.message!!.contains("401")){
                                 sessionManager.clearLogoutSession()
                                 startActivity(Intent(this, SplashActivity::class.java))
                                 finishAffinity()
                             }
                             if (resource.message.contains("400")){
                                 val data = resource.data as JsonObject
                                 val msg = data.get("message")?.asString
                                 Toast.makeText(this, msg!!, Toast.LENGTH_SHORT).show()
                             }
                         }
                     }
                 }
             }
     }
}