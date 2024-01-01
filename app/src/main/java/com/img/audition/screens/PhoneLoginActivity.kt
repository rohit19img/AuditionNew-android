package com.img.audition.screens

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.LoginResponse
import com.img.audition.dataModel.NumLoginRequest
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

     var referCode:String? = null


     private lateinit var mainViewModel: MainViewModel

     private val progressDialog by lazy {
         ProgressDialog(this@PhoneLoginActivity)
     }

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)


         progressDialog.setMessage("Please wait.")
         progressDialog.setCancelable(false)

        viewBinding.closeActivityButton.setOnClickListener {
            onBackPressed()
        }
        viewBinding.otpView.setOtpCompletionListener {
            otp = it
        }

         val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
         mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]


         referCode = intent.getStringExtra("ReferCode")

         viewBinding.getOtpBtn.setOnClickListener {
            val ccp = "+"+viewBinding.countyCodePicker.selectedCountryCode
            number = viewBinding.phoneNumberET.text.toString().trim()
            if (number.isNotEmpty() && number.length==10){
                getOTP(number,referCode)
//                defaultLogin(number,referCode)
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
                             progressDialog.dismiss()
                             sessionManager.clearLogoutSession()
                             val data = response.data as LoginResponse
                             myApplication.printLogI("${data.data!!.id}","login userID")
                             if (response.data.success!!){
                                     val userToken = response.data.data!!.token.toString()
                                     val userID = response.data.data!!.id.toString()
                                     val auditionID = response.data.data!!.auditionID.toString()
                                     myApplication.printLogD(userToken, ConstValFile.TOKEN)
                                     myApplication.printLogD(userID, ConstValFile.USER_ID)
                                     sessionManager.setGuestLogin(false)
                                     sessionManager.setMobileVerified(true)
                                     sessionManager.createUserLoginSession(true,userToken,userID)
                                     sessionManager.setMobileNumber(number)
                                     sessionManager.setUserSelfID(userID)
                                     sessionManager.setUserAuditionID(auditionID)
                                     sessionManager.setUserName(auditionID)
                                     sessionManager.setToken(userToken)
                                     Thread.sleep(500)
                                     myApplication.showToast("Login Successfully..")
                                    sendToHomeActivity()
                             }else{
                                 myApplication.showToast(response.data.message.toString())
                             }
                         }
                         Status.LOADING ->{
                             progressDialog.show()
                             myApplication.printLogD(response.status.toString(),"apiCall 3")
                         }
                         else->{
                             progressDialog.dismiss()
                             myApplication.printLogD(response.message.toString(),"apiCall 4")
                             if (response.message!!.contains("401")){
                                 sessionManager.clearLogoutSession()
                                 startActivity(Intent(this, SplashActivity::class.java))
                                 finishAffinity()
                             }
                             if (response.message.contains("400")){
                                 val data = response.data as JsonObject
                                 val msg = data.get("message")?.asString
                                 Toast.makeText(this, msg!!, Toast.LENGTH_SHORT).show()
                             }
                         }
                     }
                 }
             }
     }


     private fun getOTP(number: String, referCode: String?){
         progressDialog.show()
         val numLoginRequest = NumLoginRequest(number,referCode)
         Log.d("check200", "userLogin: $numLoginRequest")

         myApplication.printLogD(number,TAG)
         mainViewModel.userLogin(numLoginRequest)
             .observe(this){
                 it.let {response->
                     myApplication.printLogD(response.message.toString(),"apiCall 1")
                     when(response.status){
                         Status.SUCCESS ->{
                             progressDialog.dismiss()
                             val data = response.data as CommanResponse
                             myApplication.printLogD(data.message.toString(),"apiCall 2")
                             if (data.success!!){
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
                             progressDialog.dismiss()
                             myApplication.printLogD(response.message.toString(),"apiCall 4")
                             if (response.message!!.contains("401")){
                                 sessionManager.clearLogoutSession()
                                 startActivity(Intent(this, SplashActivity::class.java))
                                 finishAffinity()
                             }
                             if (response.message.contains("400")){
                                 val data = response.data as JsonObject
                                 val msg = data.get("message")?.asString
                                 Toast.makeText(this, msg!!, Toast.LENGTH_SHORT).show()
                             }

                         }
                     }
                 }
             }
     }

    private fun defaultLogin(number: String, referCode: String?){
        progressDialog.show()
        val numLoginRequest = NumLoginRequest(number,referCode)
        Log.d("check200", "userLogin: $numLoginRequest")

        myApplication.printLogD(number,TAG)
        mainViewModel.defaultLogin(numLoginRequest)
            .observe(this){
                it.let {response->
                    myApplication.printLogD(response.message.toString(),"apiCall 1")
                    when(response.status){
                        Status.SUCCESS ->{
                            progressDialog.dismiss()
                            sessionManager.clearLogoutSession()
                            val data = response.data as LoginResponse
                            myApplication.printLogI("${data.data!!.id}","login userID")
                            if (response.data.success!!){
                                val userToken = response.data.data!!.token.toString()
                                val userID = response.data.data!!.id.toString()
                                val auditionID = response.data.data!!.auditionID.toString()
                                myApplication.printLogD(userToken, ConstValFile.TOKEN)
                                myApplication.printLogD(userID, ConstValFile.USER_ID)
                                sessionManager.setGuestLogin(false)
                                sessionManager.setMobileVerified(true)
                                sessionManager.createUserLoginSession(true,userToken,userID)
                                sessionManager.setUserSelfID(userID)
                                sessionManager.setUserAuditionID(auditionID)
                                sessionManager.setUserName(auditionID)
                                sessionManager.setToken(userToken)
                                Thread.sleep(500)
                                myApplication.showToast("Login Successfully..")
                                sendToHomeActivity()
                            }else{
                                myApplication.showToast(response.data.message.toString())
                            }
                        }
                        Status.LOADING ->{
                            myApplication.printLogD(response.status.toString(),"apiCall 3")
                        }
                        else->{
                            progressDialog.dismiss()
                            myApplication.printLogD(response.message.toString(),"apiCall 4")
                            if (response.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this, SplashActivity::class.java))
                                finishAffinity()
                            }
                            if (response.message.contains("400")){
                                val data = response.data as JsonObject
                                val msg = data.get("message")?.asString
                                Toast.makeText(this, msg!!, Toast.LENGTH_SHORT).show()
                            }

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