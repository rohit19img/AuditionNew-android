package com.img.audition.screens

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.img.audition.dataModel.TermAboutPrivacyResponse
import com.img.audition.databinding.ActivityAboutUsBinding
import com.img.audition.databinding.ActivityTermsAndConditionBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TermsAndConditionActivity : AppCompatActivity() {
    val TAG = "TermsAndConditionActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityTermsAndConditionBinding.inflate(layoutInflater)
    }
    lateinit var progressDialog: ProgressDialog
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        progressDialog =  ProgressDialog(this)
        progressDialog.setMessage("Loading Data...")
        progressDialog.setCancelable(false)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.webView.requestFocus();
        viewBinding.webView.settings.setLightTouchEnabled(true)
        viewBinding.webView.settings.javaScriptEnabled = true
        viewBinding.webView.settings.setGeolocationEnabled(true)
        viewBinding.webView.isSoundEffectsEnabled = true

        viewBinding.webView.loadUrl("https://biggee.in/terms-and-conditions.html")

        viewBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    progressDialog.show();
                }
                if (newProgress == 100) {
                    progressDialog.dismiss();
                }
            }
        }

    }

    private fun showTermsAndConditionPage() {
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val req = apiInterface.getTermsAndConditions(SessionManager(this).getToken())

        req.enqueue(object : Callback<TermAboutPrivacyResponse> {
            override fun onResponse(call: Call<TermAboutPrivacyResponse>, response: Response<TermAboutPrivacyResponse>) {
                if(response.isSuccessful && response.body()!!.success!!){
                    val content = response.body()!!.data!!.description.toString()

                    viewBinding.webView.loadData(content,"text/html","UTF-8")

                    viewBinding.webView.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            if (newProgress < 100) {
                                progressDialog.show();
                            }
                            if (newProgress == 100) {
                                progressDialog.dismiss();
                            }
                        }
                    }
                }else{
                    Log.d(TAG,response.toString())
                }
            }
            override fun onFailure(call: Call<TermAboutPrivacyResponse>, t: Throwable) {
               t.printStackTrace()
            }
        })
    }
}