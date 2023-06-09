package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.img.audition.dataModel.LoginResponse
import com.img.audition.dataModel.NumLoginRequest
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.OTPRequest
import com.img.audition.databinding.ActivityPhoneLoginBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

 @UnstableApi
 class PhoneLoginActivity : AppCompatActivity() {

    private val TAG = "PhoneLoginActivity"
    private var number = ""
    private var otp = ""
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPhoneLoginBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@PhoneLoginActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@PhoneLoginActivity)
    }


     private lateinit var mainViewModel: MainViewModel

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.closeActivityButton.setOnClickListener {
            onBackPressed()
        }
        viewBinding.otpView.setOtpCompletionListener {
            otp = it
        }

         val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
         mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]


         viewBinding.getOtpBtn.setOnClickListener {
            val ccp = "+"+viewBinding.countyCodePicker.selectedCountryCode
            number = viewBinding.phoneNumberET.text.toString().trim()
            if (number.isNotEmpty() && number.length==10){
                getOTP(number)
            }else{
                viewBinding.phoneNumberET.error = "Enter valid number"
                myApplication.showToast("Enter valid number")
            }
        }

        viewBinding.otpLoginBtn.setOnClickListener {
            val otp = otp
            if (otp.length<4){
                myApplication.showToast("Enter Valid OTP")
            }else{
                val number = number
                onTokenRefresh(number)

            }
        }

    }

     fun loginWithOTP(number: String, otp: String, fcmToken: String?){
         val  otpRequest = OTPRequest(number,otp.toInt(),fcmToken)
         myApplication.printLogD(number,TAG)
         mainViewModel.userOtpVerify(otpRequest)
             .observe(this){
                 it.let {response->
                     myApplication.printLogD(response.message.toString(),"apiCall 1")
                     when(response.status){
                         Status.SUCCESS ->{
                             sessionManager.clearLogoutSession()
                             myApplication.printLogI("${response.data!!.data!!.id}","login userID")
                             val userToken = response.data.data!!.token.toString()
                             val userID = response.data.data!!.id.toString()
                             myApplication.printLogD(userToken, ConstValFile.TOKEN)
                             myApplication.printLogD(userID, ConstValFile.USER_ID)
                             sessionManager.setGuestLogin(false)
                             sessionManager.setMobileVerified(true)
                             sessionManager.createUserLoginSession(true,userToken,userID)
                             sessionManager.setUserSelfID(userID)
                             sessionManager.setUserName(number)
                             sessionManager.setToken(userToken)
                             Thread.sleep(500)
                             myApplication.showToast("Login Successfully..")

                             sendToHomeActivity()
                         }
                         Status.LOADING ->{
                             myApplication.printLogD(response.status.toString(),"apiCall 3")
                         }
                         else->{
                             if (response.message!!.contains("401")){
                                 myApplication.printLogD(response.message.toString(),"apiCall 4")
                                 sessionManager.clearLogoutSession()
                                 startActivity(Intent(this, SplashActivity::class.java))
                                 finishAffinity()
                             }
                             myApplication.printLogD(response.status.toString(),"apiCall 5")

                         }
                     }
                 }
             }
     }




     fun getOTP(number: String){
         val numLoginRequest = NumLoginRequest(number)
         myApplication.printLogD(number,TAG)
         mainViewModel.userLogin(numLoginRequest)
             .observe(this){
                 it.let {response->
                     myApplication.printLogD(response.message.toString(),"apiCall 1")
                     when(response.status){
                         Status.SUCCESS ->{
                             myApplication.printLogD(response.data!!.message.toString(),"apiCall 2")
                             if (response.data.success!!){
                                 viewBinding.phoneNumberET.isEnabled = false
                                 viewBinding.otpLayout.visibility = View.VISIBLE
                                 viewBinding.getOtpBtn.visibility = View.GONE
                                 myApplication.showToast("OTP Sent..")
                             }else {
                                 myApplication.showToast("Something went wrong..")
                                 viewBinding.otpLayout.visibility = View.GONE
                                 viewBinding.getOtpBtn.visibility = View.VISIBLE
                             }
                         }
                         Status.LOADING ->{
                             myApplication.printLogD(response.status.toString(),"apiCall 3")
                         }
                         else->{
                             if (response.message!!.contains("401")){
                                 myApplication.printLogD(response.message.toString(),"apiCall 4")
                                 sessionManager.clearLogoutSession()
                                 startActivity(Intent(this, SplashActivity::class.java))
                                 finishAffinity()
                             }
                             myApplication.printLogD(response.status.toString(),"apiCall 5")

                         }
                     }
                 }
             }
     }

    fun sendToHomeActivity(){
        val homeIntent = Intent(this@PhoneLoginActivity,HomeActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(homeIntent)
        finish()
    }

    fun onTokenRefresh(number: String) {
        val refreshedToken = arrayOf("")
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(object :
            OnCompleteListener<String?> {
            override fun onComplete(task: Task<String?>) {
                if (!task.isSuccessful()) {
                    myApplication.printLogE(task.exception.toString(),TAG);
                    return
                }
                val token: String? = task.getResult()
                myApplication.printLogD(token.toString(),"Firebase Token")
                refreshedToken[0] = token.toString()
                sessionManager.setNotificationToken(refreshedToken[0])
                loginWithOTP(number,otp,sessionManager.getNotificationToken())
            }
        })
        FirebaseMessaging.getInstance().subscribeToTopic("All-user")
    }
}