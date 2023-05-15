package com.img.audition.screens

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.img.audition.dataModel.TermAboutPrivacyResponse
import com.img.audition.databinding.ActivityAboutUsBinding
import com.img.audition.databinding.ActivityPrivacyPolicyBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AboutUsActivity : AppCompatActivity() {
    val TAG = "AboutUsActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityAboutUsBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@AboutUsActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@AboutUsActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    lateinit var progressDialog:ProgressDialog
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

        viewBinding.webView.loadUrl("   http://139.59.30.125/about-us.html")

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

    private fun showAboutUsPage() {
        val req = apiInterface.getAboutUs(sessionManager.getToken())

        req.enqueue(object : Callback<TermAboutPrivacyResponse>{
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
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<TermAboutPrivacyResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }
}