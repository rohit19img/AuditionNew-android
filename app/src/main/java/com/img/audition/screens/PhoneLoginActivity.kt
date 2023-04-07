package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.img.audition.dataModel.LoginResponse
import com.img.audition.dataModel.NumLoginRequest
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.OTPRequest
import com.img.audition.databinding.ActivityPhoneLoginBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi class PhoneLoginActivity : AppCompatActivity() {

    val TAG = "PhoneLoginActivity"
    var number = ""
    var otp = ""
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPhoneLoginBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@PhoneLoginActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@PhoneLoginActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.closeActivityButton.setOnClickListener {
            onBackPressed()
        }
        viewBinding.otpView.setOtpCompletionListener {
            otp = it
        }

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
                val fcmToken = sessionManager.getNotificationToken()
                loginWithOTP(number,otp,fcmToken)
            }
        }

    }

    private fun loginWithOTP(number: String, otp: String, fcmToken: String?) {
//        myApplication.showToast(otp)
        val  otpRequest = OTPRequest(number,otp.toInt(),fcmToken)
        val apiOTPRequest = apiInterface.OTP_Login(otpRequest)
        apiOTPRequest.enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    sessionManager.clearLogoutSession()
                    val userToken = response.body()!!.data!!.token.toString()
                    val userID = response.body()!!.data!!.id.toString()
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
                }else{
                    myApplication.printLogE("User OTP Login Failed ${response.code()}",TAG)

                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                myApplication.printLogE(t.message.toString(),TAG)
            }

        })
    }

    private fun getOTP(number: String) {
        val numLoginRequest = NumLoginRequest(number)
        myApplication.printLogD(number,TAG)

        val loginReq = apiInterface.Login(numLoginRequest)
        loginReq.enqueue(object:Callback<CommonResponse>{
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                if (response.isSuccessful){
                    viewBinding.phoneNumberET.isEnabled = false
                    viewBinding.otpLayout.visibility = View.VISIBLE
                    viewBinding.getOtpBtn.visibility = View.GONE
                    myApplication.showToast("OTP Sent..")

                }else{
                    myApplication.showToast("Something went wrong..")
                    viewBinding.otpLayout.visibility = View.GONE
                    viewBinding.getOtpBtn.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }
        })
    }

    fun sendToHomeActivity(){
        val homeIntent = Intent(this@PhoneLoginActivity,HomeActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(homeIntent)
        finish()
    }
}