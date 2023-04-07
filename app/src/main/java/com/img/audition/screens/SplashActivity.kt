package com.img.audition.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.img.audition.dataModel.GuestLoginRequest
import com.img.audition.dataModel.LoginResponse
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.SessionManager
import com.img.audition.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var sessionManager : SessionManager
    lateinit var myApplication : MyApplication
    val TAG = "SplashActivity"
    var deviceID = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this@SplashActivity)
        myApplication  = MyApplication(this@SplashActivity)

        if (myApplication.isNetworkConnected()){
            FirebaseMessaging.getInstance().subscribeToTopic("All")
            if (!(sessionManager.isUserLoggedIn())){
                myApplication.printLogD("Not Login in",TAG)
                if (!(sessionManager.isGuestLoggedIn())){
                    guestUserLogin()
                }else{
                    sendToHomeActivity()
                }
            }else{
                myApplication.printLogD("Login in",TAG)
                sendToHomeActivity()
            }
        }else{
            myApplication.showToast(ConstValFile.Check_Connection)
        }
    }

    @SuppressLint("HardwareIds")
    private fun guestUserLogin() {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        onTokenRefresh()
        deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        if (deviceID!=""){
            val guestRequestModel = GuestLoginRequest(deviceId =deviceID,
                fcmToken = sessionManager.getNotificationToken())
            myApplication.printLogI(guestRequestModel.deviceId.toString(),"guestLoginRequest deviceId:")
            myApplication.printLogI(guestRequestModel.fcmToken.toString(),"guestLoginRequest appId:")

            val guestLoginRequest = apiInterface.guestLogin(guestRequestModel)
            guestLoginRequest.enqueue(object : Callback<LoginResponse>{
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                        val guestToken = response.body()!!.data!!.token.toString()
                        val guestUserID = response.body()!!.data!!.id.toString()
                        sessionManager.clearLogoutSession()
                        myApplication.printLogD(guestToken,ConstValFile.TOKEN)
                        myApplication.printLogD(guestUserID,ConstValFile.USER_ID)
                        sessionManager.setGuestLogin(true)
                        sessionManager.setUserSelfID(guestUserID)
                        sessionManager.setToken(guestToken)
                        Thread.sleep(500)
                        sendToHomeActivity()
                    }else{
                        myApplication.printLogE("Guest User Response Failed ${response.code()}",TAG)
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    myApplication.printLogE(t.message.toString(),TAG)
                }
            })
        }else{
            myApplication.showToast("Try Again..")//Device id Null
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
                }
            })
        FirebaseMessaging.getInstance().subscribeToTopic("All-user")
    }


    fun sendToHomeActivity(){
        val homeIntent = Intent(this@SplashActivity,HomeActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(homeIntent)
        finish()
    }
}