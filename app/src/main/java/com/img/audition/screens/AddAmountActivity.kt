package com.img.audition.screens

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.img.audition.adapters.AddCashOfferAdapter
import com.img.audition.cashfree.PaymentActivity
import com.img.audition.dataModel.OfferDataResponse
import com.img.audition.databinding.ActivityAddAmountBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import org.json.JSONException
import org.json.JSONObject
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

    var requestQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val amount = viewBinding.addMoney

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        requestQueue = Volley.newRequestQueue(this)


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
                        AddAmount(amount, "Cashfree")
//                        myApplication.showToast("Payment Gateway")
                    }
                } else {
                    AddAmount(amount, "Cashfree")
//                    myApplication.showToast("Payment Gateway")
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
    fun AddAmount(Amount: String, from: String) {
        try {
            val url = APITags.APIBASEURL+ "requestAddCash"
            Log.i("url", url)
            val strRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                com.android.volley.Response.Listener<String> { response ->
                    try {
                        Log.i("Response is", response)
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getBoolean("success")) {
                            val data = jsonObject.getJSONObject("data")
                            val txnid = data.getString("txnid")

                            // add Payment gateway hare
                            val i = Intent(this@AddAmountActivity, PaymentActivity::class.java)
                            i.putExtra("price", Amount)
                            i.putExtra("orderid", txnid)
                            startActivity(i)
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddAmountActivity,
                                jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (je: JSONException) {
                        je.printStackTrace()
                    }
                },
                com.android.volley.Response.ErrorListener {
                    val d = AlertDialog.Builder(this@AddAmountActivity)
                    d.setTitle("Something went wrong")
                    d.setCancelable(false)
                    d.setMessage("Something went wrong, Please try again")
                    d.setPositiveButton(
                        "Retry"
                    ) { dialog, which -> AddAmount(Amount, from) }
                    d.setNegativeButton(
                        "Cancel"
                    ) { dialog, which -> finish() }
                }) {
                override fun getParams(): Map<String, String>? {
                    val map = HashMap<String, String>()
                    map["amount"] = Amount
                    map["paymentby"] = from
                    map["offerid"] = offerId
                    Log.i("Params", map.toString())
                    return map
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //                        params.put("Content-Type", "application/json; charset=UTF-8");
                    params["Authorization"] = sessionManager.getToken()!!
                    Log.i("Header", params.toString())
                    return params
                }
            }
            strRequest.setShouldCache(false)
            strRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue!!.add<String>(strRequest)
        } catch (e: Exception) {
            Log.i("Exception", e.message!!)
        }
    }
}