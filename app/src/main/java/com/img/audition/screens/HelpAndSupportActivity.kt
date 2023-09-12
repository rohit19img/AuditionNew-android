package com.img.audition.screens

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.img.audition.R
import com.img.audition.databinding.ActivityAboutUsBinding
import com.img.audition.databinding.ActivityHelpAndSupportBinding

class HelpAndSupportActivity : AppCompatActivity() {
    private val TAG = "HelpAndSupportActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityHelpAndSupportBinding.inflate(layoutInflater)
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
        viewBinding.webView.requestFocus()
        viewBinding.webView.settings.setLightTouchEnabled(true)
        viewBinding.webView.settings.javaScriptEnabled = true
        viewBinding.webView.settings.setGeolocationEnabled(true)
        viewBinding.webView.isSoundEffectsEnabled = true

        viewBinding.webView.loadUrl("https://biggee.in/help-and-support.html")

        viewBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    progressDialog.show()
                }
                if (newProgress == 100) {
                    progressDialog.dismiss()
                }
            }
        }
    }
}