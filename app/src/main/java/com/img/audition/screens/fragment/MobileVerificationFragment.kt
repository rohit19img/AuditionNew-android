package com.img.audition.screens.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.dataModel.*
import com.img.audition.databinding.FragmentMobileVarificationBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.mukeshsolanki.OtpView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MobileVerificationFragment : Fragment() {
    val TAG = "MobileVerificationFragment"
    private lateinit var _viewBinding : FragmentMobileVarificationBinding
    private val view get() = _viewBinding!!
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }

    private val myApplication by lazy {
        MyApplication(requireContext())
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentMobileVarificationBinding.inflate(inflater,container,false)

        view.verifyMobile.setOnClickListener {
            val mobileN = view.mobileNumber.text.toString()
            if (mobileN.isNotEmpty() && mobileN.length==10){
                verifymobile(view.mobileNumber.text.toString())
            }else{
                view.mobileNumber.error = "Enter valid number"
            }


        }

        view.verifyEmail.setOnClickListener {
            val emailA =  view.email.text.toString().trim()
            if (isValidEmail(emailA)){
                verifyEmail(view.email.text.toString())
            }else{
                view.email.error = "Enter valid Email"
            }
        }
        return view.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AllVerify()
    }

    private fun verifymobile(mobile: String) {
        val verifyReq = apiInterface.verifyMobileNumber(sessionManager.getToken(), NumLoginRequest(mobile))

        verifyReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(
                call: Call<CommanResponse>,
                response: Response<CommanResponse>
            ) {
                if (response.isSuccessful){
                    if ( response.body()!!.success!!){
                        myApplication.showToast(response.body()!!.message.toString())
                            showVerifyOTPDialog(false,"Verify Mobile Number",mobile)
                    }else{
                        myApplication.showToast(response.body()!!.message.toString())
                    } 
                }else{
                    myApplication.printLogE(response.code().toString(),TAG)
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    private fun verifyEmail(email: String) {
        val verifyReq = apiInterface.verifyEmailAddress(sessionManager.getToken(), EmailLoginRequest(email))

        verifyReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(
                call: Call<CommanResponse>,
                response: Response<CommanResponse>
            ) {
                if (response.isSuccessful){
                    if ( response.body()!!.success!!){
                        myApplication.showToast(response.body()!!.message.toString())
                        showVerifyOTPDialog(true,"Verify Email Address",email)
                    }else{
                        myApplication.showToast(response.body()!!.message.toString())
                    }
                }else{
                    myApplication.printLogE(response.code().toString(),TAG)
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.getMobileVerified()) {
            view.mobileVerify.visibility = View.GONE
            view.mobileVerified.visibility = View.VISIBLE
        } else {
            view.mobileVerify.visibility = View.VISIBLE
            view.mobileVerified.visibility = View.GONE
        }
        if (sessionManager.getEmailVerified()) {
            view.emailVerify.visibility = View.GONE
            view.emailVerified.visibility = View.VISIBLE
        } else {
            view.emailVerify.visibility = View.VISIBLE
            view.emailVerified.visibility = View.GONE
        }
    }

    private fun AllVerify() {
        val allVerifyReq = apiInterface.getAllVerificationsData(sessionManager.getToken())
        allVerifyReq.enqueue(object : Callback<UserVerificationResponse>{
            override fun onResponse(
                call: Call<UserVerificationResponse>,
                response: Response<UserVerificationResponse>
            ) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val data = response.body()!!.data
                    if (data!=null){
                        val mobile_verify = data.mobileVerify
                        val bank_verify = data.bankVerify
                        val pan_verify = data.pan_verify

                        if (mobile_verify == 1) {
                            sessionManager.setMobileVerified(true)
                            view.mobileVerified.visibility = View.VISIBLE
                            view.mobileText.text = data.mobile.toString()
                            view.mobileVerify.visibility = View.GONE
                        } else {
                            sessionManager.setMobileVerified(false)
                            view.mobileVerified.visibility = View.GONE
                            view.mobileVerify.visibility = View.VISIBLE
                        }

                        if (data.emailVerify == 1) {
                            sessionManager.setEmailVerified(true)
                            view.emailVerified.visibility = View.VISIBLE
                            view.emailText.text = data.email.toString()
                            view.emailVerify.visibility = View.GONE
                        } else {
                            sessionManager.setEmailVerified(false)
                            view.emailVerified.visibility = View.GONE
                            view.emailVerify.visibility = View.VISIBLE
                        }
                        sessionManager.setBankVerified(bank_verify.toString())
                        sessionManager.setPANVerified(pan_verify.toString())
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<UserVerificationResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }


    private fun showVerifyOTPDialog(isEmail: Boolean,title:String,emailNumber:String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.verify_otp_dialog)

        val dialogTitle = dialog.findViewById<TextView>(R.id.title)
        val otpView = dialog.findViewById<OtpView>(R.id.otpView)
        val otpLoginBtn = dialog.findViewById<CardView>(R.id.otpLoginBtn)

        dialogTitle!!.text = title

        var otp = ""
        otpView!!.setOtpCompletionListener {
            otp = it
        }


        otpLoginBtn!!.setOnClickListener {
            if (otp.length<4){
                myApplication.showToast("Enter Valid OTP")
            }else{
                dialog.dismiss()
               verifyOTP(otp,emailNumber,isEmail)
            }
        }

//        dialog.window!!.setBackgroundDrawableResource(requireContext().getColor(R.color.float_transaparent))
        dialog.show()
    }

    private fun verifyOTP(otp: String, emailNumber: String,isEmail:Boolean) {
        val verifyOtpBody = JsonObject()
        if (isEmail){
            verifyOtpBody.addProperty("email",emailNumber)
        }else{
            verifyOtpBody.addProperty("mobile",emailNumber)
        }
        verifyOtpBody.addProperty("code",otp)
        val verifyOtpReq = apiInterface.verifyOTP(sessionManager.getToken(),verifyOtpBody)

        verifyOtpReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful) {
                    if(response.body()!!.success!!){
                        myApplication.showToast(response.body()!!.message.toString())
                        AllVerify()
                    }else{
                        myApplication.showToast(response.body()!!.message.toString())
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target!!).matches()
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }

}