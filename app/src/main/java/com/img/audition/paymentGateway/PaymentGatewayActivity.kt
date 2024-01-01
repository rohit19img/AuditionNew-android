package com.img.audition.paymentGateway

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.databinding.ActivityPaymentGatewayBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.SessionManager
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import java.nio.charset.Charset
import java.security.MessageDigest

class PaymentGatewayActivity : AppCompatActivity() {
    private val merchantID = "M1QITG824ENR"
    private val apiEndPoint = "/pg/v1/pay"
    private val saltKey = "43b754c0-3f96-40a1-aff1-9d5080760dcc"
    private val saltIndex = 2
    private val TAG = "PaymentGatewayActivity"
    private val binding by lazy {
        ActivityPaymentGatewayBinding.inflate(layoutInflater)
    }
    private val progressDialog by lazy {
        ProgressDialog(this@PaymentGatewayActivity)
    }
    private val session by lazy { SessionManager(this) }
    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    private lateinit var txnId:String
    private  var amount:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        PhonePe.init(this, PhonePeEnvironment.UAT,merchantID,null)

        progressDialog.setCancelable(false)
        progressDialog.setMessage("Processing..")

        txnId = bundle!!.getString(ConstValFile.TransactionId).toString()
        amount = bundle!!.getString(ConstValFile.Amount)!!.toInt()

        binding.topBarTitle.text = "Payment"
        binding.backPress.setOnClickListener {
            finish()
        }

        binding.orderId.text = "Txn Id: $txnId"
        binding.amount.text = "Amount: ₹ $amount"

        try {
            PhonePe.setFlowId(session.getUserSelfID())
            val upiAppsList = PhonePe.getUpiApps()
            val paymentAppList = ArrayList<PaymentAppModel>()
            upiAppsList.forEach { upiApps->
                Log.d(TAG, "Available UPI App -> ${upiApps.applicationName}")
                val paytm = "Paytm"
                val amazon = "Amazon"
                val googlepay = "GPay"
                val phonepay = "PhonePe"
                val bhim = "BHIM"
                when (upiApps.applicationName) {
                    paytm -> paymentAppList.add(PaymentAppModel(upiApps.applicationName, upiApps.packageName, R.drawable.ic_paytm))
                    amazon -> paymentAppList.add(PaymentAppModel(upiApps.applicationName, upiApps.packageName, R.drawable.ic_amazon_pay))
                    googlepay -> paymentAppList.add(PaymentAppModel(upiApps.applicationName, upiApps.packageName, R.drawable.ic_gpay))
                    phonepay -> paymentAppList.add(PaymentAppModel(upiApps.applicationName, upiApps.packageName, R.drawable.ic_phonepe))
                    bhim -> paymentAppList.add(PaymentAppModel(upiApps.applicationName, upiApps.packageName, R.drawable.ic_bhim))
                }
            }
            val adapter = PaymentAppListAdapter(paymentAppList) { paymentAppClick(it) }
            binding.paymentAppCycle.adapter = adapter
        } catch ( exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun paymentAppClick(paymentApp:PaymentAppModel){
        // adapter onItemClick
        Log.d(TAG, "paymentAppClick: ${paymentApp.packageName}")
        val paymentJson = JsonObject()
        val paymentInstrument = JsonObject()
        val deviceContext = JsonObject()
        paymentJson.addProperty("merchantId",merchantID)
        paymentJson.addProperty("merchantTransactionId",txnId)
        paymentJson.addProperty("merchantUserId",session.getUserSelfID())
        paymentJson.addProperty("amount",amount)
        paymentJson.addProperty("mobileNumber",session.getMobileNumber())
        paymentInstrument.addProperty("type","UPI_INTENT")
        paymentInstrument.addProperty("targetApp",paymentApp.packageName)
        paymentJson.add("paymentInstrument",paymentInstrument)
        deviceContext.addProperty("deviceOS","ANDROID")
        paymentJson.add("deviceContext",deviceContext)

        val base64Payload = generateBase64Payload(paymentJson.toString())
        val checkSum = sha256(base64Payload+apiEndPoint+saltKey) +"###"+"$saltIndex"
        Log.d(TAG, "paymentAppClick: paymentJson : $paymentJson")
        Log.d(TAG, "paymentAppClick: base64 : $base64Payload")
        Log.d(TAG, "paymentAppClick: checkSum : $checkSum")

        val b2BPGRequest = B2BPGRequestBuilder()
            .setData(base64Payload)
            .setChecksum(checkSum)
            .setUrl(apiEndPoint)
            .build()

        try {
            PhonePe.getImplicitIntent(this@PaymentGatewayActivity,b2BPGRequest,paymentApp.packageName)
                ?.let { startActivityForResult(it,777) }
        }catch (e:Exception){
            Log.e(TAG, "paymentAppClick: ${e.message}")
        }
    }


    private fun generateBase64Payload(payloadJson:String): String {
        val base64Payload = Base64.encodeToString(payloadJson.toByteArray(Charset.defaultCharset()), Base64.NO_WRAP)
        return base64Payload.replace("\n","")
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.fold(""){str,it->str+"%02x".format(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: $resultCode")
        if (requestCode == 777) {
            if (resultCode == Activity.RESULT_CANCELED){
                Log.e(TAG, "onActivityResult: Cancel")
                Log.e(TAG, "onActivityResult: ${data?.extras}")
            }else{
                if (data?.extras!=null && data.extras!!.keySet().size>0){
                    for (key in data.extras!!.keySet()){
                        Log.d(TAG, "onActivityResult: Key : $key")
                    }
                }
            }
        }
    }

}