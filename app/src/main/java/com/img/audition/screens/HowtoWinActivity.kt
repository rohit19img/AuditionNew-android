package com.img.audition.screens

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.img.audition.R
import com.img.audition.databinding.ActivityHelpAndSupportBinding
import com.img.audition.databinding.ActivityHowtoWinBinding

class HowtoWinActivity : AppCompatActivity() {

    private val TAG = "HelpAndSupportActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityHowtoWinBinding.inflate(layoutInflater)
    }
    lateinit var progressDialog: ProgressDialog

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

        viewBinding.webView.loadUrl("https://biggee.in/how-to-win.html")

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