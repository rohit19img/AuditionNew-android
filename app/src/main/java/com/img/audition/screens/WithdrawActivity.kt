package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.databinding.ActivityAddAmountBinding
import com.img.audition.databinding.ActivityWithdrawBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WithdrawActivity : AppCompatActivity() {

    private val TAG = "WithdrawActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityWithdrawBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@WithdrawActivity)
    }
    private val myApplication by lazy {
        MyApplication(this@WithdrawActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.withdrawBtn.setOnClickListener {
            val amount = viewBinding.withdrawCashET.text.toString().trim()
            if (amount.isEmpty()){
                if (amount.isNotEmpty()){
                    if (amount.toInt()>0){
                        viewBinding.withdrawCashET.error = "Enter Valid Amount"
                    }else{
                        viewBinding.withdrawCashET.error = "Enter Amount"
                    }
                }else{
                    viewBinding.withdrawCashET.error = "Enter Valid Amount"
                }
            } else{
                viewBinding.withdrawCashET.text.clear()
                if (sessionManager.getBankVerified() == "1"){
                    withdrawRequest(amount)
                }else{
                    myApplication.showToast("Please verify KYC")
                   sendToVerficationActivity()
                }
            }
        }
    }
    private fun sendToVerficationActivity() {
        val intent = Intent(this@WithdrawActivity,VerificationActivity::class.java)
        startActivity(intent)
    }

    private fun withdrawRequest(amount: String) {
        val obj = JsonObject()
        obj.addProperty("amount",amount)
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val withdrawReq = apiInterface.withdrawRequest(sessionManager.getToken(),obj)

        withdrawReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                    if (response.isSuccessful){
                        if ( response.body()!!.success!!){
                            val sweetAlertDialog = SweetAlertDialog(this@WithdrawActivity, SweetAlertDialog.SUCCESS_TYPE)
                            sweetAlertDialog.titleText = "Withdraw"
                            sweetAlertDialog.contentText = "Request Send Successfully.."
                            sweetAlertDialog.show()
                            viewBinding.withdrawMsg.visibility = View.VISIBLE
                            viewBinding.withdrawMsg.text = response.body()!!.message.toString()
                        }else{
                            viewBinding.withdrawMsg.visibility = View.VISIBLE
                            viewBinding.withdrawMsg.text = response.body()!!.message.toString()
                        }
                    }else{
                        myApplication.printLogE(response.body().toString(),TAG)
                        myApplication.showToast("Something went wrong..")
                    }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }
}