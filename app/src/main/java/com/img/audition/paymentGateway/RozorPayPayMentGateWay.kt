package com.img.audition.paymentGateway

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.img.audition.R
import com.img.audition.network.SessionManager
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class RozorPayPayMentGateWay : Activity(), PaymentResultWithDataListener {
    var requestQueue: RequestQueue? = null
    var session: SessionManager? = null
    var price: String? = null
    var razorpay_order_id: String? = null
    var txnid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rozor_pay_pay_ment_gate_way)
        requestQueue = Volley.newRequestQueue(this)
        session = SessionManager(applicationContext)

        price = intent.extras!!.getString("price")
        razorpay_order_id = intent.getStringExtra("razorpayid")
        txnid = intent.getStringExtra("orderid")

        Checkout.preload(applicationContext)
        startPayment()
    }

    fun startPayment() {
        val activity: Activity = this
        val co = Checkout()
        try {
            val options = JSONObject()
            options.put("name", resources.getString(R.string.app_name))
            options.put("description", "Order id: $razorpay_order_id")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency", "INR")
            options.put("order_id", razorpay_order_id)
            options.put("amount", (price!!.toInt() * 100).toString())
            options.put("payment_capture", true)
            val preFill = JSONObject()
            val email = "testuser@biggee.in"
            val mobile =  "7777777777"
            preFill.put("email", email)
            preFill.put("contact", mobile)
            options.put("prefill", preFill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(this@RozorPayPayMentGateWay, "Error in payment...", Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
            finish()
        }
    }

    override fun onPaymentError(code: Int, response: String, paymentData: PaymentData) {
        try {
            Toast.makeText(this@RozorPayPayMentGateWay, "Payment Failed", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            finish()
        }
    }

    override fun onPaymentSuccess(s: String, paymentData: PaymentData) {
        try {
            Toast.makeText(this@RozorPayPayMentGateWay, "Payment Successful", Toast.LENGTH_SHORT)
                .show()
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    companion object {
        private val TAG = RozorPayPayMentGateWay::class.java.simpleName
    }
}