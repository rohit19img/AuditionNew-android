package com.img.audition.screens

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.img.audition.adapters.AddCashOfferAdapter
import com.img.audition.dataModel.OfferData
import com.img.audition.dataModel.OfferDataResponse
import com.img.audition.databinding.ActivityAddAmountBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAmountActivity : AppCompatActivity() {
    val TAG = "AddAmountActivity"
    var ifOfferApplied = false
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityAddAmountBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@AddAmountActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@AddAmountActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    var offerId = ""
    var offerMinAmt = 0
    var offerMaxAmt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val amount = viewBinding.addMoney

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        getOfferDetails()
        viewBinding.add100.setOnClickListener {
            if (amount.text.toString() != "") {
                val amt = amount.text.toString()
                amount.setText((amt.toInt() + 100).toString())
            } else {
                amount.setText("100")
            }
        }
        viewBinding.add200.setOnClickListener {
            if (amount.text.toString() != "") {
                val amt = amount.text.toString()
                amount.setText((amt.toInt() + 200).toString())
            } else {
                amount.setText("200")
            }
        }
        viewBinding.add500.setOnClickListener {
            if (amount.text.toString() != "") {
                val amt = amount.text.toString()
                amount.setText((amt.toInt() + 500).toString())
            } else {
                amount.setText("500")
            }
        }

        viewBinding.btnAddCash.setOnClickListener {
            val amount =  viewBinding.addMoney.text.toString().trim()
            if (amount != "") {
                if (ifOfferApplied) {
                    val amt: Int = amount.toInt()
                    if (amt <= offerMinAmt) {
                        Toast.makeText(
                            this@AddAmountActivity,
                            "Add Cash More Then : $offerMinAmt",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (amt >= offerMaxAmt) {
                        Toast.makeText(
                            this@AddAmountActivity,
                            "Add Cash Less Then : $offerMaxAmt",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
//                        AddAmount(amount, "Cashfree")
                        myApplication.showToast("Payment Gateway")
                    }
                } else {
//                    AddAmount(amount, "Cashfree")
                    myApplication.showToast("Payment Gateway")
                }
            } else {
                Toast.makeText(this@AddAmountActivity, "Enter Amount", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun getOfferDetails() {
        val offerReq = apiInterface.getOfferDetails(sessionManager.getToken())

        offerReq.enqueue(object : Callback<OfferDataResponse>{
            override fun onResponse(call: Call<OfferDataResponse>, response: Response<OfferDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    val data  = response.body()!!.data
                  if (data.size>0){
                      viewBinding.offerRecycler.visibility = View.VISIBLE
                      viewBinding.offersText.visibility = View.VISIBLE
                      val adapter = AddCashOfferAdapter(this@AddAmountActivity,data)
                      viewBinding.offerRecycler.adapter = adapter
                  }else{
                      myApplication.printLogD("No Offers Available..",TAG)
                  }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<OfferDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }
}