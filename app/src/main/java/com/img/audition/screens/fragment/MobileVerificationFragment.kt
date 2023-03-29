package com.img.audition.screens.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.NumLoginRequest
import com.img.audition.dataModel.UserVerificationResponse
import com.img.audition.databinding.FragmentMobileVarificationBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
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


        AllVerify()


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
                            view.mobileVerify.visibility = View.GONE
                        } else {
                            sessionManager.setMobileVerified(false)
                            view.mobileVerified.visibility = View.GONE
                            view.mobileVerify.visibility = View.VISIBLE
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentMobileVarificationBinding.inflate(inflater,container,false)

        view.verifyMobile.setOnClickListener {
            verifymobile(view.mobileNumber.text.toString())
        }
        return view.root
    }

    private fun verifymobile(mobile: String) {
        val verifyReq = apiInterface.verifyMobileNumber(sessionManager.getToken(), NumLoginRequest(mobile))

        verifyReq.enqueue(object : Callback<CommonResponse>{
            override fun onResponse(
                call: Call<CommonResponse>,
                response: Response<CommonResponse>
            ) {
                if (response.isSuccessful){
                    if ( response.body()!!.success!!){
                        myApplication.showToast(response.body()!!.message.toString())
                    }else{
                        myApplication.showToast(response.body()!!.message.toString())
                    } 
                }else{
                    myApplication.printLogE(response.code().toString(),TAG)
                }
               
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
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
    }

}