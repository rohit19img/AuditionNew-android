package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.img.audition.R
import com.img.audition.databinding.ActivityAddAmountBinding
import com.img.audition.databinding.ActivityWithdrawBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager

class WithdrawActivity : AppCompatActivity() {

    val TAG = "WithdrawActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityWithdrawBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@WithdrawActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@WithdrawActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.withdrawBtn.setOnClickListener {
            val amount = viewBinding.withdrawCashET.text.toString().trim()
            if (amount.isNullOrEmpty()){
                if (amount.toInt()>0){
                    myApplication.showToast("Enter Valid Amount")
                }else
                    myApplication.showToast("Enter Amount")
            }else{
                myApplication.showToast("Payment Gateway...")
            }
        }
    }
}